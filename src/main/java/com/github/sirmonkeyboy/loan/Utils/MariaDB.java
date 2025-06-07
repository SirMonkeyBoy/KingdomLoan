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
                        loanAmount DOUBLE,
                        payBackAmount DOUBLE,
                        amountPaid DOUBLE,
                        amountPaidOut DOUBLE,
                        PRIMARY KEY(loanId)
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
                        loanAmount DOUBLE,
                        payBackAmount DOUBLE,
                        loanStartDate TIMESTAMP,
                        loanEndDate TIMESTAMP,
                        PRIMARY KEY(loanHistoryId)
                        )""");
            pstmt3.executeUpdate();

            PreparedStatement pstmt4 = conn.prepareStatement("CREATE INDEX IF NOT EXISTS LoanHistory_index_0 ON LoanHistory (uuidOfLoaner, uuidOfLoaned);");
            pstmt4.executeUpdate();
        }
    }

    public void updatePlayerName(Player p) throws SQLException {
        UUID uuid = p.getUniqueId();
        String uuidStr = String.valueOf(uuid);
        String name = p.getName();

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement("SELECT nameOfLoaner, nameOfLoaned, uuidOfLoaner, uuidOfLoaned FROM Loan WHERE uuidOfLoaner = ? or uuidOfLoaned = ?")) {
                pstmt.setString(1, uuidStr);
                pstmt.setString(2, uuidStr);

                try (ResultSet rs = pstmt.executeQuery()) {

                    boolean updateLoanerName = false;
                    boolean updateLoanedName = false;

                    while (rs.next()) {
                        if (uuidStr.equals(rs.getString("uuidOfLoaner")) && !name.equals(rs.getString("nameOfLoaner"))) {
                            updateLoanerName = true;
                        }

                        if (uuidStr.equals(rs.getString("uuidOfLoaned")) && !name.equals(rs.getString("nameOfLoaned"))) {
                            updateLoanedName = true;
                        }
                    }

                    if (updateLoanerName) {
                        try (PreparedStatement pstmt2 = conn.prepareStatement("UPDATE Loan SET nameOfLoaner = ? WHERE uuidOfLoaner = ?")) {
                            pstmt2.setString(1, name);
                            pstmt2.setString(2, uuidStr);
                            pstmt2.executeUpdate();
                        }

                        try (PreparedStatement pstmt3 = conn.prepareStatement("UPDATE LoanHistory SET nameOfLoaner = ? WHERE uuidOfLoaner = ?")) {
                            pstmt3.setString(1, name);
                            pstmt3.setString(2, uuidStr);
                            pstmt3.executeUpdate();
                        }
                    }

                    if (updateLoanedName) {
                        try (PreparedStatement pstmt4 = conn.prepareStatement("UPDATE Loan SET nameOfLoaned = ? WHERE uuidOfLoaned = ?")) {
                            pstmt4.setString(1, name);
                            pstmt4.setString(2, uuidStr);
                            pstmt4.executeUpdate();
                        }

                        try (PreparedStatement pstmt5 = conn.prepareStatement("UPDATE LoanHistory SET nameOfLoaned = ? WHERE uuidOfLoaned = ?")) {
                            pstmt5.setString(1, name);
                            pstmt5.setString(2, uuidStr);
                            pstmt5.executeUpdate();
                        }
                    }
                }

                conn.commit();
            } catch (SQLException e) {
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

    public String checkIfHaveLoan(UUID uuid) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT nameOfLoaner FROM Loan WHERE uuidOfLoaned = ?")) {

            pstmt.setString(1, uuid.toString());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("nameOfLoaner");
                }
            }
        }
        return null;
    }

    public boolean loanAccept(UUID uuidOfLoaner, String nameOfLoaner, UUID uuidOfLoaned, String nameOfLoaned, double loanAmount, double payBackAmount) throws SQLException {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            try {
                try (PreparedStatement pstmt = conn.prepareStatement("INSERT IGNORE INTO Loan " +
                        "(uuidOfLoaner, nameOfLoaner, uuidOfLoaned, nameOfLoaned, loanAmount, payBackAmount)" +
                        " VALUES (?, ?, ?, ?, ?, ?)")) {
                    pstmt.setString(1, uuidOfLoaner.toString());
                    pstmt.setString(2, nameOfLoaner);
                    pstmt.setString(3, uuidOfLoaned.toString());
                    pstmt.setString(4, nameOfLoaned);
                    pstmt.setDouble(5, loanAmount);
                    pstmt.setDouble(6, payBackAmount);
                    pstmt.executeUpdate();
                }

                try (PreparedStatement pstmt = conn.prepareStatement("INSERT IGNORE INTO LoanHistory " +
                        "(uuidOfLoaner, nameOfLoaner, uuidOfLoaned, nameOfLoaned, loanAmount, payBackAmount, loanStartDate)" +
                        " VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                    long currentTimeMillis = System.currentTimeMillis();
                    java.sql.Timestamp timestamp = new java.sql.Timestamp(currentTimeMillis);
                    pstmt.setString(1, uuidOfLoaner.toString());
                    pstmt.setString(2, nameOfLoaner);
                    pstmt.setString(3, uuidOfLoaned.toString());
                    pstmt.setString(4, nameOfLoaned);
                    pstmt.setDouble(5, loanAmount);
                    pstmt.setDouble(6, payBackAmount);
                    pstmt.setTimestamp(7, timestamp);
                    pstmt.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                try {
                    conn.rollback();
                    return false;
                } catch (SQLException rollbackEx) {
                    rollbackEx.addSuppressed(e);
                    Utils.getErrorLogger("Error creating loan: " + rollbackEx.getMessage());
                    throw rollbackEx;
                }
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
}