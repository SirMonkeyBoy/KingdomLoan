package com.github.sirmonkeyboy.loan.Commands;

import com.github.sirmonkeyboy.loan.Loan;
import com.github.sirmonkeyboy.loan.Utils.LoanManager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;


import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.List;

public class LoanCommand implements TabExecutor {

    private final Loan plugin;

    private final LoanManager loanManager;

    public LoanCommand(Loan plugin, LoanManager loanManager) {
        this.plugin = plugin;
        this.loanManager = loanManager;
    }

    @Override
    public boolean onCommand (@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (sender instanceof Player p){
            if (!p.hasPermission("Loan.commands.loan")) {
                p.sendMessage(Component.text("You Don't have permission to use /loan").color(NamedTextColor.RED));
                return true;
            }

            if (args.length == 0) {
                p.sendMessage(Component.text("Loan usages"));
                p.sendMessage(Component.text("/loan create (user your loaning to) (loan Amount) (pay back Amount) - ").append(Component.text("Create's a loan.").color(NamedTextColor.GOLD)));
                p.sendMessage(Component.text("/loan accept (Username) - ").append(Component.text("Accepts the loan from that user 120 second time out.").color(NamedTextColor.GOLD)));
                p.sendMessage(Component.text("/loan pay (Amount) - ").append(Component.text("Pays back your loan.").color(NamedTextColor.GOLD)));
                p.sendMessage(Component.text("/loan list - ").append(Component.text("Shows you the active loan have or a list of all active loans you have given out.").color(NamedTextColor.GOLD)));
                p.sendMessage(Component.text("/loan history (had/given) (page) - ").append(Component.text("List of all loans have had or list of all loans you have given out.").color(NamedTextColor.GOLD)));
                return true;
            }

            String input = args[0].toLowerCase();

            switch (input){

                case "help":
                    p.sendMessage(Component.text("Loan usages"));
                    p.sendMessage(Component.text("/loan create (user your loaning to) (loan Amount) (pay back Amount) - ").append(Component.text("Create's a loan.").color(NamedTextColor.GOLD)));
                    p.sendMessage(Component.text("/loan accept (Username) - ").append(Component.text("Accepts the loan from that user 120 second time out.").color(NamedTextColor.GOLD)));
                    p.sendMessage(Component.text("/loan pay (Amount) - ").append(Component.text("Pays back your loan.").color(NamedTextColor.GOLD)));
                    p.sendMessage(Component.text("/loan list - ").append(Component.text("Shows you the active loan have or a list of all active loans you have given out.").color(NamedTextColor.GOLD)));
                    p.sendMessage(Component.text("/loan history (had/given) (page) - ").append(Component.text("List of all loans have had or list of all loans you have given out.").color(NamedTextColor.GOLD)));
                    return true;

                case "create":
                    if (!p.hasPermission("Loan.commands.loan.create")) {
                        p.sendMessage(Component.text("You Don't have permission to use /loan create").color(NamedTextColor.RED));
                        return true;
                    }
                    loanManager.loanRequest(p, args);
                    return true;

                case "accept":
                    try {
                        loanManager.loanAccept(p, args);
                    } catch (SQLException e) {
                        p.sendMessage(Component.text("Error in creating the loan try again or contact staff.").color(NamedTextColor.RED));
                        return true;
                    }
                    return true;

                case "pay":
                    try {
                        loanManager.loanPay(p, args);
                    } catch (SQLException e) {
                        p.sendMessage(Component.text("Error in paying down the loan try again or contact staff.").color(NamedTextColor.RED));
                        return true;
                    }
                    return true;

                case "list":
                    try {
                        loanManager.loanList(p);
                    } catch (SQLException e) {
                        p.sendMessage(Component.text("Error in getting loan list try again or contact staff.").color(NamedTextColor.RED));
                        return true;
                    }
                    return true;

                case "history":
                    p.sendMessage("test");
                    return true;
            }
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete (@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args){
        if (args.length == 1) {
            return List.of("help", "create", "accept", "pay", "list", "history");
        }

        if (args[0].equalsIgnoreCase("create")) {
            if (args.length == 2) {
                return plugin.getServer().getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .toList();
            }

            if (args.length == 3) {
                return List.of("Amount");
            }

            if (args.length == 4) {
                return List.of("PayBackAmount");
            }
        }

        if (args[0].equalsIgnoreCase("accept")) {
            if (args.length == 2) {
                return plugin.getServer().getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .toList();
            }
        }

        if (args[0].equalsIgnoreCase("pay")) {
            if (args.length == 2) {
                return List.of("Amount");
            }
        }

        if (args[0].equalsIgnoreCase("history")) {
            if (args.length == 2) {
                return List.of("had", "given");
            }
        }
        return List.of();
    }
}
