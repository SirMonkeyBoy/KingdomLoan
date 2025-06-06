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
                    return true;

                case "accept":
                    return true;

                case "pay":
                    return true;

                case "list":
                    return true;

            }
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete (@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args){
        return List.of();
    }
}
