package me.sirmonkeyboy.loan.Commands;

import me.sirmonkeyboy.loan.Loan;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import static org.bukkit.ChatColor.translateAlternateColorCodes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LoanCommand implements TabExecutor {
    private final Loan plugin;
    public LoanCommand(Loan plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        Economy eco = Loan.getEconomy();
        if (sender instanceof Player p){
            if (p.hasPermission("Loan.commands.loan")) {
                if (args.length <= 2) {
                    p.sendMessage("Please use");
                    p.sendMessage("/loan (PlayerLoaning) (PlayerLoanedTo) (LoanAmount) (AmountToPayBack)");
                }else {
                    Player Loaning = Bukkit.getServer().getPlayer(args[0]);
                    if (Loaning != null){
                        Player LoanedTo = Bukkit.getServer().getPlayer(args[1]);
                        if (LoanedTo != null){
                            try {
                                // Attempt to parse the first argument as an integer
                                p.sendMessage(Loaning.getName());
                                p.sendMessage(LoanedTo.getName());
                                int LoanAmount = Integer.parseInt(args[2]);
                                // If parsing is successful, 'number' contains the integer
                                // You can now use 'number' in your logic
                                p.sendMessage("" + LoanAmount);
                            } catch (NumberFormatException e) {
                                // If parsing fails, it's not an integer
                                p.sendMessage("Please Use a Number");
                            }
                        }else {
                            p.sendMessage("Loaned to Player isn't their");
                        }
                    }else {
                        p.sendMessage("Loaning Player isn't their");
                    }
                }
            }
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        return null;
    }
}
