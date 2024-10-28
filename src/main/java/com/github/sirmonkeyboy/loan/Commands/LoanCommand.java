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
            if (p.hasPermission("Loan.commands.loan")) {

            } else {
                p.sendMessage(Component.text("You Don't have permission to use /loan").color(NamedTextColor.RED));
            }
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete (@NotNull CommandSender commandSender, @NotNull Command
            command, @NotNull String s, @NotNull String[]strings){
        return List.of();
    }
}
