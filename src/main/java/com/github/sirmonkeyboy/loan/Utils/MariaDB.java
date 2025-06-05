package com.github.sirmonkeyboy.loan.Utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MariaDB {

    private final ConfigManager configManager;

    private HikariDataSource dataSource;

    public MariaDB(ConfigManager configManager) {
        this.configManager = configManager;
    }


    public void connect() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + configManager.getHost() + ":" + configManager.getPort() + "/" + configManager.getDatabase());
        config.setUsername(configManager.getUsername());
        config.setPassword(configManager.getPassword());
        config.setMaximumPoolSize(configManager.getSetMaximumPoolSize());
        config.setMinimumIdle(configManager.getSetMinimumIdle());
        config.setIdleTimeout(30000);
        config.setConnectionTimeout(30000);
        config.setLeakDetectionThreshold(10000);

        dataSource = new HikariDataSource(config);
    }

    // Checks if the database is connected
    public boolean isConnected() {
        return (dataSource != null && !dataSource.isClosed());
    }

    // Disconnects for the database if connected
    public void disconnect() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    //Gets database connection
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void createTable() throws SQLException {
        try (Connection conn = getConnection()) {
                PreparedStatement pstmt = conn.prepareStatement("""
                        CREATE TABLE IF NOT EXISTS Loan (
                        loanId INTEGER NOT NULL AUTO_INCREMENT UNIQUE,
                        uuidOfLoaner VARCHAR(255),
                        nameOfLoaner VARCHAR(255),
                        uuidOfLoaned VARCHAR(255),
                        nameOfLoaned VARCHAR(255),
                        amountLoaned DOUBLE,
                        amountToPayBack DOUBLE,
                        amountPaid DOUBLE,
                        amountPaidOut DOUBLE,
                        PRIMARY KEY(id)
                        )""");
                pstmt.executeUpdate();

                PreparedStatement pstmt2 = conn.prepareStatement("CREATE INDEX IF NOT EXISTS Loan_index_0 ON Loan (uuidOfLoaner, uuidOfLoaned);");
                pstmt2.executeUpdate();

        }
    }
}