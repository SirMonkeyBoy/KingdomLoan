package com.github.sirmonkeyboy.loan.Commands;

import com.github.sirmonkeyboy.loan.Loan;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LoanCommand implements TabExecutor {

    private final Loan plugin;

    public LoanCommand(Loan plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand (@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args){
        Economy eco = Loan.getEconomy();
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
                p.sendMessage(Component.text("/loan list - ").append(Component.text("Shows you the loan you have or loans have given out.").color(NamedTextColor.GOLD)));
                return true;
            }

            String input = args[0].toLowerCase();

            switch (input){

                case "help":
                    p.sendMessage(Component.text("Loan usages"));
                    p.sendMessage(Component.text("/loan create (user your loaning to) (loan Amount) (pay back Amount) - ").append(Component.text("Create's a loan.").color(NamedTextColor.GOLD)));
                    p.sendMessage(Component.text("/loan accept (Username) - ").append(Component.text("Accepts the loan from that user 120 second time out.").color(NamedTextColor.GOLD)));
                    p.sendMessage(Component.text("/loan pay (Amount) - ").append(Component.text("Pays back your loan.").color(NamedTextColor.GOLD)));
                    p.sendMessage(Component.text("/loan list - ").append(Component.text("Shows you the loan you have or loans have given out.").color(NamedTextColor.GOLD)));
                    return true;

                case "create":
                    if (!p.hasPermission("Loan.commands.loan.create")) {
                        p.sendMessage(Component.text("You Don't have permission to use /loan create").color(NamedTextColor.RED));
                        return true;
                    }
                    p.sendMessage("test");
                    return true;

                case "accept":
                    p.sendMessage("test");
                    return true;

                case "pay":
                    p.sendMessage("test");
                    return true;

                case "list":
                    p.sendMessage("test");
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
            return List.of("help", "create", "accept", "pay", "list");
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
