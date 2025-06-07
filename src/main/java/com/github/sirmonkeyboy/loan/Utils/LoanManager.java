package com.github.sirmonkeyboy.loan.Utils;

import com.github.sirmonkeyboy.loan.Loan;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.UUID;

public class LoanManager {

    private final Loan plugin;

    private final ConfigManager configManager;

    private final CooldownManager cooldownManager;

    private final HashMap<UUID, LoanData> loanRequests = new HashMap<>();

    private final HashMap<UUID, BukkitTask> requestTimeout = new HashMap<>();

    public LoanManager(Loan plugin, ConfigManager configManager, CooldownManager cooldownManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.cooldownManager = cooldownManager;
    }

    public boolean loanRequest(Player player, String[] args) {

        if (args.length < 4) {
            player.sendMessage(Component.text("Usage /loan create (user your loaning to) (loan Amount) (pay back Amount)").color(NamedTextColor.RED));
            return true;
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
                return true;
            }

            if (target == null || !target.isOnline()) {
                player.sendMessage(Component.text("Player not found or is offline.").color(NamedTextColor.RED));
                return true;
            }

            UUID targetUUID = target.getUniqueId();
            String targetName = target.getName();

            if (targetUUID.equals(playerUUID)) {
                player.sendMessage(Component.text("You cannot loan to yourself.").color(NamedTextColor.RED));
                return true;
            }

            if (requestTimeout.containsKey(targetUUID)) {
                player.sendMessage(targetName + " already has a loan request wait seconds for it to timeout.");
                return true;
            }

            Economy eco = Loan.getEconomy();

            if (loanAmount > eco.getBalance(player)) {
                player.sendMessage(Component.text("You don't have " + loanAmount + " in your balance.").color(NamedTextColor.RED));
                return true;
            }

            if (!(loanAmount > configManager.getMinimumLoanSize())) {
                player.sendMessage(Component.text("Minimum loan size is 100000.").color(NamedTextColor.RED));
                return true;
            }

            if (!(loanAmount <= payBackAmount)) {
                player.sendMessage(Component.text("Pay back amount must be equal to or larger then the loan amount.").color(NamedTextColor.RED));
                return true;
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
            player.sendMessage(Component.text("They have " + configManager.getRequestTimeout() + " seconds to accept"));
            target.sendMessage(Component.text(playerName + " is offering to loan you $" + loanAmount + " you will have to pay them back $" + payBackAmount));
            target.sendMessage(Component.text("Type /loan accept " + playerName + " to accept request will timeout in " + configManager.getRequestTimeout() +" seconds."));

            cooldownManager.startCooldown(playerUUID);
            return true;
        } catch (NumberFormatException e) {
            player.sendMessage(Component.text(configManager.getInvalidAmountMessage()).color(NamedTextColor.RED));
            throw new RuntimeException(e);
        }
    }

    public boolean loanAccept(Player player, String[] args) {
        return false;
    }

    public void clearLoanRequests() {
        requestTimeout.clear();
        loanRequests.clear();
    }
}