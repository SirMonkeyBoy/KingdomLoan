package com.github.sirmonkeyboy.kingdomloan.Utils;

import com.github.sirmonkeyboy.kingdomloan.KingdomLoan;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class LoanManager {

    private final KingdomLoan plugin;

    private final ConfigManager configManager;

    private final MariaDB data;

    private final CooldownManager cooldownManager;

    private final HashMap<UUID, LoanData> loanRequests = new HashMap<>();

    private final HashMap<UUID, BukkitTask> requestTimeout = new HashMap<>();

    public LoanManager(KingdomLoan plugin, ConfigManager configManager, MariaDB data, CooldownManager cooldownManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.data = data;
        this.cooldownManager = cooldownManager;
    }

    public void loanCreate(Player player, String[] args) {

        if (args.length < 4) {
            player.sendMessage(Component.text("Usage /loan create (user your lending to) (loan Amount) (pay back Amount)").color(NamedTextColor.RED));
            return;
        }


        try {
            double loanAmount = Double.parseDouble(args[2]);
            double payBackAmount = Double.parseDouble(args[3]);

            Player target = Bukkit.getPlayer(args[1]);

            UUID playerUUID = player.getUniqueId();
            String playerName = player.getName();

            if (cooldownManager.isOnCooldown(playerUUID)) {
                long seconds = cooldownManager.getRemainingTime(playerUUID) / 1000;
                String CooldownMessage = configManager.getCooldownMessage().replace("%Seconds%", String.valueOf(seconds));
                player.sendMessage(CooldownMessage);
                return;
            }

            if (target == null || !target.isOnline()) {
                player.sendMessage(Component.text("Player not found or is offline.").color(NamedTextColor.RED));
                return;
            }

            UUID targetUUID = target.getUniqueId();
            String targetName = target.getName();

            if (targetUUID.equals(playerUUID)) {
                player.sendMessage(Component.text("You cannot loan to yourself.").color(NamedTextColor.RED));
                return;
            }

            String nameOfLender = data.checkIfHasALoan(targetUUID);
            if (nameOfLender != null) {
                player.sendMessage(Component.text(targetName + " already has a loan from " + nameOfLender).color(NamedTextColor.RED));
                cooldownManager.startCooldown(playerUUID);
                return;
            }

            if (requestTimeout.containsKey(targetUUID)) {
                player.sendMessage(targetName + " already has a loan request wait for it to timeout this will take at max " + configManager.getRequestTimeout() + " seconds.");
                return;
            }

            Economy eco = KingdomLoan.getEconomy();

            if (loanAmount > eco.getBalance(player)) {
                player.sendMessage(Component.text("You don't have $" + loanAmount + " in your balance.").color(NamedTextColor.RED));
                return;
            }

            if (!(loanAmount > configManager.getMinimumLoanSize())) {
                player.sendMessage(Component.text("Minimum loan size is $" + configManager.getMinimumLoanSize()).color(NamedTextColor.RED));
                return;
            }

            if (!(loanAmount <= payBackAmount)) {
                player.sendMessage(Component.text("Pay back amount must be equal to or larger then the loan amount.").color(NamedTextColor.RED));
                return;
            }

            loanRequests.put(targetUUID, new LoanData(playerUUID, loanAmount, payBackAmount));

            BukkitTask Timeout = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                LoanData loanData = loanRequests.get(targetUUID);
                if (loanData != null && loanRequests.containsKey(targetUUID) &&
                        loanRequests.get(targetUUID).getRequesterUUID().equals(playerUUID)) {
                    loanRequests.remove(targetUUID);
                    requestTimeout.remove(targetUUID);
                    player.sendMessage(Component.text("Your loan request to " + targetName + " has timed out.").color(NamedTextColor.RED));
                    target.sendMessage(Component.text("Loan request from " + playerName + " has timed out.").color(NamedTextColor.RED));
                }
            }, configManager.getTimeoutTicks());

            requestTimeout.put(targetUUID, Timeout);

            player.sendMessage(Component.text("Loan request sent to " + playerName));
            player.sendMessage(Component.text("They have " + configManager.getRequestTimeout() + " seconds to accept."));
            target.sendMessage(Component.text(playerName + " is offering to loan you $" + loanAmount + " you will have to pay them back $" + payBackAmount));
            target.sendMessage(Component.text("Type /loan accept " + playerName + " to accept, request will timeout in " + configManager.getRequestTimeout() +" seconds."));

            cooldownManager.startCooldown(playerUUID);
        } catch (NumberFormatException | SQLException e) {
            player.sendMessage(Component.text(configManager.getInvalidAmountMessage()).color(NamedTextColor.RED));
            throw new RuntimeException(e);
        }
    }

    public void loanAccept(Player player, String[] args) throws SQLException {

        if (args.length < 2) {
            player.sendMessage(Component.text("Usage /loan accept (User lending you money)").color(NamedTextColor.RED));
            return;
        }

        Player target  = Bukkit.getPlayer(args[1]);

        UUID playerUUID = player.getUniqueId();
        String playerName = player.getName();

        if (cooldownManager.isOnCooldown(playerUUID)) {
            long seconds = cooldownManager.getRemainingTime(playerUUID) / 1000;
            String CooldownMessage = configManager.getCooldownMessage().replace("%Seconds%", String.valueOf(seconds));
            player.sendMessage(CooldownMessage);
            return;
        }

        String nameOfLender = data.checkIfHasALoan(playerUUID);
        if (nameOfLender != null) {
            player.sendMessage(Component.text( "You already has a loan from " + nameOfLender).color(NamedTextColor.RED));
            cooldownManager.startCooldown(playerUUID);
            return;
        }


        if (target == null || !target.isOnline()) {
            player.sendMessage(Component.text("Player not found or is offline.").color(NamedTextColor.RED));
            return;
        }

        UUID targetUUID = target.getUniqueId();
        String targetName = target.getName();

        LoanData loanData = loanRequests.get(playerUUID);
        if (loanData == null) {
            player.sendMessage(Component.text("You don't have any loan requests.").color(NamedTextColor.RED));
            return;
        }

        UUID requesterUUID = loanData.getRequesterUUID();
        double loanAmount = loanData.getLoanAmount();
        double payBackAmount = loanData.getPayBackAmount();

        if (!requesterUUID.equals(targetUUID)) {
            player.sendMessage(Component.text("No loan request from that player").color(NamedTextColor.RED));
            return;
        }

        Economy eco = KingdomLoan.getEconomy();

        if (loanAmount > eco.getBalance(target)) {
            player.sendMessage(Component.text(targetName + " doesn't have $" + loanAmount + " in their balance.").color(NamedTextColor.RED));
            return;
        }

        boolean success = data.loanAccept(targetUUID, targetName, playerUUID, playerName, loanAmount, payBackAmount);

        if (!success) {
            player.sendMessage(Component.text("Error in creating the loan try again or contact staff.").color(NamedTextColor.RED));
            return;
        }
        eco.withdrawPlayer(target, loanAmount);
        eco.depositPlayer(player, loanAmount);
        player.sendMessage(Component.text("Accepted loan from " + targetName + " for $" + loanAmount + " and pay back amount $" + payBackAmount).color(NamedTextColor.GREEN));
        target.sendMessage(Component.text(playerName + " accepted your loan for $" + loanAmount + " and pay back amount $" + payBackAmount).color(NamedTextColor.GREEN));

        loanRequests.remove(playerUUID);
        requestTimeout.remove(playerUUID);
        cooldownManager.startCooldown(playerUUID);
    }

    public void loanPay(Player player, String[] args) throws SQLException {
        UUID playerUUID = player.getUniqueId();

        if (args.length == 1) {

            if (cooldownManager.isOnCooldown(playerUUID)) {
                long seconds = cooldownManager.getRemainingTime(playerUUID) / 1000;
                String CooldownMessage = configManager.getCooldownMessage().replace("%Seconds%", String.valueOf(seconds));
                player.sendMessage(CooldownMessage);
                return;
            }

            String nameOfLender = data.checkIfHasALoan(playerUUID);
            if (nameOfLender == null) {
                player.sendMessage(Component.text( "You don't have a loan.").color(NamedTextColor.YELLOW));
                cooldownManager.startCooldown(playerUUID);
                return;
            }

            double amountLeftOnLoan = data.getAmountLeftOnLoan(player.getUniqueId());
            if (amountLeftOnLoan != 0) {
                player.sendMessage(Component.text( "You have $" + amountLeftOnLoan + " on your loan.").color(NamedTextColor.GREEN));
                cooldownManager.startCooldown(playerUUID);
                return;
            }

            player.sendMessage(Component.text( "Error getting amount left").color(NamedTextColor.RED));

            return;
        }

        if (args.length < 2) {
            player.sendMessage(Component.text("Usage /loan pay (Amount)").color(NamedTextColor.RED));
            return;
        }

        try {
            double payAmount = Double.parseDouble(args[1]);

            String nameOfLender = data.checkIfHasALoan(playerUUID);
            if (nameOfLender == null) {
                player.sendMessage(Component.text( "You don't have a loan.").color(NamedTextColor.YELLOW));
                cooldownManager.startCooldown(playerUUID);
                return;
            }

            Economy eco = KingdomLoan.getEconomy();

            if (payAmount > eco.getBalance(player)) {
                player.sendMessage(Component.text("You don't have $" + payAmount + " in your balance.").color(NamedTextColor.RED));
                return;
            }

            boolean success = data.loanPay(player, playerUUID, payAmount);

            if (!success) {
                return;
            }

            eco.withdrawPlayer(player, payAmount);

            player.sendMessage(Component.text("Paid down loan by $" + payAmount).color(NamedTextColor.GREEN));

            cooldownManager.startCooldown(playerUUID);
        } catch (NumberFormatException e) {
            player.sendMessage(Component.text(configManager.getInvalidAmountMessage()).color(NamedTextColor.RED));
        }
    }

    public void loanList(Player player) throws SQLException {
        UUID playerUUID = player.getUniqueId();

        if (cooldownManager.isOnCooldown(playerUUID)) {
            long seconds = cooldownManager.getRemainingTime(playerUUID) / 1000;
            String CooldownMessage = configManager.getCooldownMessage().replace("%Seconds%", String.valueOf(seconds));
            player.sendMessage(CooldownMessage);
            return;
        }

        String nameOfLender = data.checkIfHasALoan(playerUUID);
        if (nameOfLender != null) {
            boolean success = data.loanListBorrowed(player);
            if (!success) {
                player.sendMessage(Component.text("Error getting your loan info try again or contact staff.").color(NamedTextColor.RED));
            }
            cooldownManager.startCooldown(playerUUID);
            return;
        }

        String nameOfBorrower = data.checkIfPlayerHasLent(playerUUID);
        if (nameOfBorrower != null) {
            boolean success = data.loanListLent(player);
            if (!success) {
                player.sendMessage(Component.text("Error getting your loan info try again or contact staff.").color(NamedTextColor.RED));
            }
            cooldownManager.startCooldown(playerUUID);
            return;
        }

        player.sendMessage(Component.text("You don't have any active loans.").color(NamedTextColor.YELLOW));
        cooldownManager.startCooldown(playerUUID);
    }

    public void loanHistory(Player player, String[] args) throws SQLException {
        if (args.length < 2) {
            player.sendMessage(Component.text("Usage /loan history borrowed/lent (page)").color(NamedTextColor.RED));
            return;
        }

        String input = args[1].toLowerCase();

        int page = 1;

        if (args.length == 2) {
            switch (input) {
                case "lent":
                    try {
                        List<LoanHistoryData> loanHistory = data.loanHistoryLent(player, page);
                        player.sendMessage(Component.text(" -----").color(NamedTextColor.YELLOW)
                                .append(Component.text(" Loan History Lent ").color(NamedTextColor.GOLD))
                                .append(Component.text("-----").color(NamedTextColor.YELLOW)));
                        for (LoanHistoryData entry : loanHistory) {
                            player.sendMessage(Component.text("Lent " + entry.getNameOfLenderOrBorrower() + " $" + entry.getLoanAmount()));
                        }
                        if (loanHistory.isEmpty()) {
                            player.sendMessage(Component.text("No loans on this page you haven't lent this much.").color(NamedTextColor.YELLOW));
                            return;
                        }
                        if (loanHistory.size() < 10){
                            player.sendMessage("Last page");
                            return;
                        }
                        player.sendMessage(Component.text("Next page is " + page+1));
                        return;
                    } catch (NumberFormatException e) {
                        player.sendMessage(Component.text(configManager.getInvalidAmountMessage()).color(NamedTextColor.RED));
                    }

                case "borrowed":
                    try {
                        List<LoanHistoryData> loanHistory = data.loanHistoryBorrowed(player, page);
                        player.sendMessage(Component.text(" -----").color(NamedTextColor.YELLOW)
                                .append(Component.text(" Loan History Borrowed ").color(NamedTextColor.GOLD))
                                .append(Component.text("-----").color(NamedTextColor.YELLOW)));
                        for (LoanHistoryData entry : loanHistory) {
                            player.sendMessage(Component.text("Loan from " + entry.getNameOfLenderOrBorrower() + " for $" + entry.getLoanAmount()));
                        }
                        if (loanHistory.isEmpty()) {
                            player.sendMessage(Component.text("No loans on this page you haven't borrowed this much.").color(NamedTextColor.YELLOW));
                            return;
                        }
                        if (loanHistory.size() < 10){
                            player.sendMessage("Last page.");
                            return;
                        }

                        player.sendMessage(Component.text("Next page is " + page+1));
                        return;
                    } catch (NumberFormatException e) {
                        player.sendMessage(Component.text(configManager.getInvalidAmountMessage()).color(NamedTextColor.RED));
                    }
            }
        }
        page = Integer.parseInt(args[2]);

        switch (input) {
            case "lent":
                try {
                    List<LoanHistoryData> loanHistory = data.loanHistoryLent(player, page);
                    player.sendMessage(Component.text(" -----").color(NamedTextColor.YELLOW)
                            .append(Component.text(" Loan History Lent ").color(NamedTextColor.GOLD))
                            .append(Component.text("-----").color(NamedTextColor.YELLOW)));
                    for (LoanHistoryData entry : loanHistory) {
                        player.sendMessage(Component.text("Lent " + entry.getNameOfLenderOrBorrower() + " $" + entry.getLoanAmount()));
                    }
                    if (loanHistory.isEmpty()) {
                        player.sendMessage(Component.text("No loans on this page you haven't lent this much.").color(NamedTextColor.YELLOW));
                        return;
                    }
                    if (loanHistory.size() < 10){
                        player.sendMessage("Last page");
                        return;
                    }
                    player.sendMessage(Component.text("Next page is " + page+1));
                    return;
                } catch (NumberFormatException e) {
                    player.sendMessage(Component.text(configManager.getInvalidAmountMessage()).color(NamedTextColor.RED));
                }

            case "borrowed":
                try {
                    List<LoanHistoryData> loanHistory = data.loanHistoryBorrowed(player, page);
                    player.sendMessage(Component.text(" -----").color(NamedTextColor.YELLOW)
                            .append(Component.text(" Loan History Borrowed ").color(NamedTextColor.GOLD))
                            .append(Component.text("-----").color(NamedTextColor.YELLOW)));
                    for (LoanHistoryData entry : loanHistory) {
                        player.sendMessage(Component.text("Loan from " + entry.getNameOfLenderOrBorrower() + " for $" + entry.getLoanAmount()));
                    }
                    if (loanHistory.isEmpty()) {
                        player.sendMessage(Component.text("No loans on this page you haven't borrowed this much.").color(NamedTextColor.YELLOW));
                        return;
                    }
                    if (loanHistory.size() < 10){
                        player.sendMessage("Last page.");
                        return;
                    }
                    player.sendMessage(Component.text("Next page is " + page+1));
                    return;
                } catch (NumberFormatException e) {
                    player.sendMessage(Component.text(configManager.getInvalidAmountMessage()).color(NamedTextColor.RED));
                }
        }
    }

    public void clearLoanRequests() {
        requestTimeout.clear();
        loanRequests.clear();
    }
}