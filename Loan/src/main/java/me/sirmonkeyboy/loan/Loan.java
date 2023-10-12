package me.sirmonkeyboy.loan;

import me.sirmonkeyboy.loan.Commands.LoanCommand;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class Loan extends JavaPlugin {

    private static Economy econ = null;

    @Override
    public void onEnable() {
        // Plugin startup logic
        if (!setupEconomy() ) {
            getLogger().info("Disabled due to no Vault dependency found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        Objects.requireNonNull(getCommand("Loan")).setExecutor(new LoanCommand(this));
        getLogger().info("Loan Plugin has started");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }

        econ = rsp.getProvider();
        //noinspection ConstantValue
        return econ != null;
    }

    public static Economy getEconomy() {
        return econ;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("Loan Plugin has stopped");
    }
}
