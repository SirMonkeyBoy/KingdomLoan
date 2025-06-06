package com.github.sirmonkeyboy.loan;

import com.github.sirmonkeyboy.loan.Commands.LoanCommand;
import com.github.sirmonkeyboy.loan.Listeners.PlayerJoinListener;
import com.github.sirmonkeyboy.loan.Utils.*;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.Objects;

public final class Loan extends JavaPlugin {

    private MariaDB data;

    private static Economy econ = null;

    @Override
    public void onEnable() {

        this.saveDefaultConfig();

        ConfigManager configManager = new ConfigManager(this);
        CooldownManager cooldownManager = new CooldownManager(configManager.getCooldown());
        this.data = new MariaDB(configManager);

        if (!setupEconomy() ) {
            Utils.getErrorLogger("Disabled due to no Vault dependency found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        try {
            data.connect();
        } catch (Exception e) {
            Utils.getErrorLogger("Database not connected");
            Utils.getErrorLogger("Disabled due to no Database found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        try {
            data.createTable();
        } catch (SQLException e) {
            Utils.getErrorLogger("Disable Loan due to error in Database tables");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }

        Objects.requireNonNull(getCommand("Loan")).setExecutor(new LoanCommand(this));

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(data), this);

        getLogger().info("Loan has started");
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

        if (data.isConnected()) {
            data.disconnect();
            getLogger().info("Disconnected successfully from Database");
        }

        getLogger().info("Loan has stopped");
    }
}
