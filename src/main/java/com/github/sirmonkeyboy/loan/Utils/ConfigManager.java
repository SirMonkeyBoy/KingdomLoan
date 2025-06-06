package com.github.sirmonkeyboy.loan.Utils;

import com.github.sirmonkeyboy.loan.Loan;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class ConfigManager {

    private final Loan plugin;

    private String host;
    private String port;
    private String database;
    private String username;
    private String password;
    private int setMaximumPoolSize;
    private int setMinimumIdle;
    private int requestTimeout;
    private int cooldown;
    private String cooldownMessage;
    private String invalidAmountMessage;
    // This is set here so it can't get removed in config
    @SuppressWarnings("FieldCanBeLocal")
    private final String missingMessage = "Contact Server Admin missing message in config";

    public ConfigManager(Loan plugin) {
        this.plugin = plugin;
        load();
    }

    public void reloadConfigManager(CommandSender sender) {
        plugin.reloadConfig();
        load();
        if (!validate()) {
            sender.sendMessage(Component.text("Config validation failed. Check your config.yml for missing values.").color(NamedTextColor.DARK_RED));
            sender.sendMessage(Component.text("Using default config options if database config info is missing plugin will not work.").color(NamedTextColor.DARK_RED));
            sender.sendMessage(Component.text("Check console for what is missing.").color(NamedTextColor.DARK_RED));
            return;
        }
        sender.sendMessage(Component.text("Config successfully reloaded").color(NamedTextColor.GREEN));
    }

    public void load() {
        host = plugin.getConfig().getString("MariaDB.host", "localhost");
        port = plugin.getConfig().getString("MariaDB.port", "3306");
        database = plugin.getConfig().getString("MariaDB.database", "Loan");
        username = plugin.getConfig().getString("MariaDB.username", "Loan");
        password = plugin.getConfig().getString("MariaDB.password", "password");
        setMaximumPoolSize = plugin.getConfig().getInt("MariaDB.Set-Maximum-Pool-Size", 15);
        setMinimumIdle = plugin.getConfig().getInt("MariaDB.Set-Minimum-Idle", 2);
        requestTimeout = plugin.getConfig().getInt("Loan-Request.Timeout", 120);
        cooldown = plugin.getConfig().getInt("Cooldown.Cooldown", 20);
        cooldownMessage = plugin.getConfig().getString("Cooldown.Cooldown-Message", "You must wait %Seconds% seconds before using /loan again.");
        invalidAmountMessage = plugin.getConfig().getString("Invalid-Amount", "Invalid amount. Please enter a number greater than zero.");
    }

    /**
     * I know this is not needed, but I would like to keep the validation messages.
     * This is only for the Database info as everything else can be the default.
     */
    @SuppressWarnings("DataFlowIssue")
    public boolean validate() {
        List<String> nullFields = new ArrayList<>();

        if (plugin.getConfig().getString("MariaDB.host") == null || plugin.getConfig().getString("MariaDB.host").isEmpty()) nullFields.add("MariaDB.host");
        if (plugin.getConfig().getString("MariaDB.port") == null || plugin.getConfig().getString("MariaDB.port").isEmpty()) nullFields.add("MariaDB.port");
        if (plugin.getConfig().getString("MariaDB.database") == null || plugin.getConfig().getString("MariaDB.database").isEmpty()) nullFields.add("MariaDB.database");
        if (plugin.getConfig().getString("MariaDB.username") == null || plugin.getConfig().getString("MariaDB.username").isEmpty()) nullFields.add("MariaDB.username");
        if (plugin.getConfig().getString("MariaDB.password") == null || plugin.getConfig().getString("MariaDB.password").isEmpty()) nullFields.add("MariaDB.password");
        if (plugin.getConfig().getInt("MariaDB.Set-Maximum-Pool-Size") <= 0) nullFields.add("MariaDB.Set-Maximum-Pool-Size");
        if (plugin.getConfig().getInt("MariaDB.Set-Minimum-Idle") <= 0) nullFields.add("MariaDB.Set-Minimum-Idle");

        if (!nullFields.isEmpty()) {
            plugin.getLogger().warning("Missing or empty config entries: " + String.join(", ", nullFields));
            return false;
        }

        return true;
    }

    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }

    public String getDatabase() {
        return database;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getSetMaximumPoolSize() {
        return setMaximumPoolSize;
    }

    public int getSetMinimumIdle() {
        return setMinimumIdle;
    }

    public int getRequestTimeout() {
        return requestTimeout;
    }

    public int getTimeoutTicks() {
        return requestTimeout * 20;
    }

    public int getCooldown() {
        return cooldown;
    }

    public String getCooldownMessage() {
        return cooldownMessage;
    }

    public String getInvalidAmountMessage() {
        return invalidAmountMessage;
    }
}