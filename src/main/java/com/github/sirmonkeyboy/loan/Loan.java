package com.github.sirmonkeyboy.loan;

import com.github.sirmonkeyboy.loan.Commands.LoanCommand;
import com.github.sirmonkeyboy.loan.Utils.MariaDB;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.Objects;

public final class Loan extends JavaPlugin {

    public MariaDB data;

    private static Economy econ = null;

    @Override
    public void onEnable() {
        // Plugin startup logic

        this.saveDefaultConfig();

        if (!setupEconomy() ) {
            getLogger().info("Disabled due to no Vault dependency found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.data = new MariaDB(this);

        try {
            data.connect();
        } catch (ClassNotFoundException | SQLException e) {
            getLogger().info("Database not connected");
            getLogger().info("Disabled due to no Database found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        try {
            data.createTable();
        } catch (SQLException e) {
            throw new RuntimeException(e);
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
        if (data.isConnected()) {
            data.disconnect();
        }
    }
}
