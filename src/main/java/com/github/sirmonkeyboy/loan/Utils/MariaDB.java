package com.github.sirmonkeyboy.loan.Utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

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
            conn.setAutoCommit(true);

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
                        PRIMARY KEY(loanId, uuidOfLoaned)
                        )""");
                pstmt.executeUpdate();

                PreparedStatement pstmt2 = conn.prepareStatement("CREATE INDEX IF NOT EXISTS Loan_index_0 ON Loan (uuidOfLoaner, uuidOfLoaned);");
                pstmt2.executeUpdate();

            PreparedStatement pstmt3 = conn.prepareStatement("""
                        CREATE TABLE IF NOT EXISTS LoanHistory (
                        loanHistoryId INTEGER NOT NULL AUTO_INCREMENT UNIQUE,
                        uuidOfLoaner VARCHAR(255),
                        nameOfLoaner VARCHAR(255),
                        uuidOfLoaned VARCHAR(255),
                        nameOfLoaned VARCHAR(255),
                        amountLoaned DOUBLE,
                        amountToPayBack DOUBLE,
                        loanStartDate TIMESTAMP,
                        loanEndDate TIMESTAMP,
                        PRIMARY KEY(loanHistoryId)
                        )""");
            pstmt3.executeUpdate();

            PreparedStatement pstmt4 = conn.prepareStatement("CREATE INDEX IF NOT EXISTS LoanHistory_index_0 ON Loan (uuidOfLoaner, uuidOfLoaned);");
            pstmt4.executeUpdate();
        }
    }

    public void updatePlayerName(Player p) throws SQLException {
        UUID uuid = p.getUniqueId();
        String name = p.getName();

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement("SELECT NAME FROM Loan WHERE UUID = ?")) {
                pstmt.setString(1, uuid.toString());

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        String storedName = rs.getString("NAME");

                        if (storedName == null || !storedName.equalsIgnoreCase(name)) {
                            try (PreparedStatement updatePNameBank = conn.prepareStatement("UPDATE Loan SET NAME = ? WHERE UUID = ?")) {
                                updatePNameBank.setString(1, name);
                                updatePNameBank.setString(2, uuid.toString());
                                updatePNameBank.executeUpdate();
                            }

                            try (PreparedStatement updatePNameTransactions = conn.prepareStatement("UPDATE LoanHistory SET NAME = ? WHERE UUID = ?")) {
                                updatePNameTransactions.setString(1, name);
                                updatePNameTransactions.setString(2, uuid.toString());
                                updatePNameTransactions.executeUpdate();
                            }
                        }
                    }
                }

                conn.commit();
            }catch (SQLException e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.addSuppressed(e);
                    Utils.getErrorLogger("Error updating player name in Loan database tables: " + rollbackEx.getMessage());
                    throw rollbackEx;
                }
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
}