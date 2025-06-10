package com.github.sirmonkeyboy.loan.Utils;

import com.github.sirmonkeyboy.loan.Loan;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.entity.Player;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
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
        config.setIdleTimeout(60000);
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
                        CREATE TABLE IF NOT EXISTS active_loans (
                        loan_id BIGINT NOT NULL AUTO_INCREMENT UNIQUE,
                        uuid_of_lender VARCHAR(255),
                        name_of_lender VARCHAR(255),
                        uuid_of_borrower VARCHAR(255),
                        name_of_borrower VARCHAR(255),
                        loan_amount DOUBLE,
                        pay_back_amount DOUBLE,
                        amount_paid DOUBLE DEFAULT 0,
                        amount_paid_out DOUBLE DEFAULT 0,
                        PRIMARY KEY(loan_id)
                        )""");
                pstmt.executeUpdate();

                PreparedStatement pstmt2 = conn.prepareStatement("CREATE INDEX IF NOT EXISTS active_loans_index ON active_loans (uuid_of_lender, uuid_of_borrower);");
                pstmt2.executeUpdate();

            PreparedStatement pstmt3 = conn.prepareStatement("""
                        CREATE TABLE IF NOT EXISTS loan_history (
                        loan_history_id BIGINT NOT NULL AUTO_INCREMENT UNIQUE,
                        uuid_of_lender VARCHAR(255),
                        name_of_lender VARCHAR(255),
                        uuid_of_borrower VARCHAR(255),
                        name_of_borrower VARCHAR(255),
                        loan_amount DOUBLE,
                        pay_back_amount DOUBLE,
                        loan_start_date TIMESTAMP,
                        loan_end_date TIMESTAMP,
                        PRIMARY KEY(loan_history_id)
                        )""");
            pstmt3.executeUpdate();

            PreparedStatement pstmt4 = conn.prepareStatement("CREATE INDEX IF NOT EXISTS loan_history_index_0 ON loan_history (uuid_of_lender, uuid_of_borrower);");
            pstmt4.executeUpdate();
        }
    }

    public void updatePlayerName(Player p) throws SQLException {
        UUID uuid = p.getUniqueId();
        String uuidStr = String.valueOf(uuid);
        String name = p.getName();

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement("SELECT name_of_lender, name_of_borrower, uuid_of_lender, uuid_of_borrower FROM active_loans WHERE uuid_of_lender = ? or uuid_of_borrower = ?")) {
                pstmt.setString(1, uuidStr);
                pstmt.setString(2, uuidStr);

                try (ResultSet rs = pstmt.executeQuery()) {

                    boolean updateLenderName = false;
                    boolean updateBorrowerName = false;

                    while (rs.next()) {
                        if (uuidStr.equals(rs.getString("uuid_of_lender")) && !name.equals(rs.getString("name_of_lender"))) {
                            updateLenderName = true;
                        }

                        if (uuidStr.equals(rs.getString("uuid_of_borrower")) && !name.equals(rs.getString("name_of_borrower"))) {
                            updateBorrowerName = true;
                        }
                    }

                    if (updateLenderName) {
                        try (PreparedStatement pstmt2 = conn.prepareStatement("UPDATE active_loans SET name_of_lender = ? WHERE uuid_of_lender = ?")) {
                            pstmt2.setString(1, name);
                            pstmt2.setString(2, uuidStr);
                            pstmt2.executeUpdate();
                        }

                        try (PreparedStatement pstmt3 = conn.prepareStatement("UPDATE loan_history SET name_of_lender = ? WHERE uuid_of_lender = ?")) {
                            pstmt3.setString(1, name);
                            pstmt3.setString(2, uuidStr);
                            pstmt3.executeUpdate();
                        }
                    }

                    if (updateBorrowerName) {
                        try (PreparedStatement pstmt4 = conn.prepareStatement("UPDATE active_loans SET name_of_borrower = ? WHERE uuid_of_borrower = ?")) {
                            pstmt4.setString(1, name);
                            pstmt4.setString(2, uuidStr);
                            pstmt4.executeUpdate();
                        }

                        try (PreparedStatement pstmt5 = conn.prepareStatement("UPDATE loan_history SET name_of_borrower = ? WHERE uuid_of_borrower = ?")) {
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

    public String checkIfHasALoan(UUID uuid) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT name_of_lender FROM active_loans WHERE uuid_of_borrower = ?")) {

            pstmt.setString(1, uuid.toString());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("name_of_lender");
                }
            }
        }
        return null;
    }

    public boolean loanAccept(UUID uuid_of_lender, String name_of_lender, UUID uuid_of_borrower, String name_of_borrower, double loan_amount, double pay_back_amount) throws SQLException {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            try {
                try (PreparedStatement pstmt = conn.prepareStatement("INSERT IGNORE INTO active_loans " +
                        "(uuid_of_lender, name_of_lender, uuid_of_borrower, name_of_borrower, loan_amount, pay_back_amount)" +
                        " VALUES (?, ?, ?, ?, ?, ?)")) {
                    pstmt.setString(1, uuid_of_lender.toString());
                    pstmt.setString(2, name_of_lender);
                    pstmt.setString(3, uuid_of_borrower.toString());
                    pstmt.setString(4, name_of_borrower);
                    pstmt.setDouble(5, loan_amount);
                    pstmt.setDouble(6, pay_back_amount);
                    pstmt.executeUpdate();
                }

                try (PreparedStatement pstmt = conn.prepareStatement("INSERT IGNORE INTO loan_history " +
                        "(uuid_of_lender, name_of_lender, uuid_of_borrower, name_of_borrower, loan_amount, pay_back_amount, loan_start_date)" +
                        " VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                    long currentTimeMillis = System.currentTimeMillis();
                    Timestamp timestamp = new Timestamp(currentTimeMillis);
                    pstmt.setString(1, uuid_of_lender.toString());
                    pstmt.setString(2, name_of_lender);
                    pstmt.setString(3, uuid_of_borrower.toString());
                    pstmt.setString(4, name_of_borrower);
                    pstmt.setDouble(5, loan_amount);
                    pstmt.setDouble(6, pay_back_amount);
                    pstmt.setTimestamp(7, timestamp);
                    pstmt.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
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

    public double getAmountLeftOnLoan(UUID uuid_of_borrower) throws SQLException {
        try (Connection conn = getConnection()) {
            double currentAmountPaid;
            double payBackAmount;
            double stillNeedToPayBack;

            try (PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT amount_paid, pay_back_amount FROM active_loans WHERE uuid_of_borrower = ?")) {
                pstmt.setString(1, uuid_of_borrower.toString());
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        currentAmountPaid = rs.getDouble("amount_paid");
                        payBackAmount = rs.getDouble("pay_back_amount");
                         return stillNeedToPayBack = payBackAmount - currentAmountPaid;
                    }
                }
            }
        }
        return 0;
    }

    public boolean loanPay(Player player, UUID uuid_of_borrower, double payAmount) throws SQLException {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            try {
                double currentAmountPaid;
                double payBackAmount;
                double stillNeedToPayBack;

                try (PreparedStatement pstmt = conn.prepareStatement(
                        "SELECT amount_paid, pay_back_amount FROM active_loans WHERE uuid_of_borrower = ?")) {
                    pstmt.setString(1, uuid_of_borrower.toString());
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            currentAmountPaid = rs.getDouble("amount_paid");
                            payBackAmount = rs.getDouble("pay_back_amount");
                            stillNeedToPayBack = payBackAmount - currentAmountPaid;
                        } else {
                            player.sendMessage(Component.text("You don't have a loan.").color(NamedTextColor.RED));
                            return false;
                        }
                    }
                }

                if (payAmount > stillNeedToPayBack) {
                    player.sendMessage(Component.text("You are trying to pay more then you owe you only owe $" + stillNeedToPayBack).color(NamedTextColor.RED));
                    return false;
                }

                if (Math.abs(payAmount - stillNeedToPayBack) < 0.01) {
                    try (PreparedStatement pstmt = conn.prepareStatement(
                            "UPDATE active_loans SET uuid_of_borrower = null, amount_paid = amount_paid + ? WHERE uuid_of_borrower = ?")) {
                        pstmt.setDouble(1, payAmount);
                        pstmt.setString(2, uuid_of_borrower.toString());

                        pstmt.executeUpdate();
                    }

                    try (PreparedStatement pstmt = conn.prepareStatement(
                            "UPDATE loan_history " +
                                    "SET loan_end_date = ? " +
                                    "WHERE loan_history_id = (" +
                                    "   SELECT loan_history_id FROM (" +
                                    "       SELECT loan_history_id FROM loan_history " +
                                    "       WHERE uuid_of_borrower = ? AND loan_end_date IS NULL " +
                                    "       ORDER BY loan_start_date DESC LIMIT 1" +
                                    "   ) AS sub" +
                                    ")")) {
                        long currentTimeMillis = System.currentTimeMillis();
                        Timestamp timestamp = new Timestamp(currentTimeMillis);
                        pstmt.setTimestamp(1, timestamp);
                        pstmt.setString(2, uuid_of_borrower.toString());

                        pstmt.executeUpdate();
                    }

                    player.sendMessage(Component.text("Loan has been paid off.").color(NamedTextColor.GREEN));

                    conn.commit();
                    return true;
                }

                try (PreparedStatement pstmt = conn.prepareStatement(
                        "UPDATE active_loans SET amount_paid = amount_paid + ? WHERE uuid_of_borrower = ?")) {
                    pstmt.setDouble(1, payAmount);
                    pstmt.setString(2, uuid_of_borrower.toString());

                    pstmt.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                try {
                    conn.rollback();
                    return false;
                } catch (SQLException rollbackEx) {
                    rollbackEx.addSuppressed(e);
                    Utils.getErrorLogger("Error paying down loan: " + rollbackEx.getMessage());
                    throw rollbackEx;
                }
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public void loanPayOutToLoaner(Player player) throws SQLException {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            List<String> namesOfLoaned = new ArrayList<>();

            double totalPaidOut = 0;

            try {

                Economy eco = Loan.getEconomy();

                try ( PreparedStatement batchUpdate = conn.prepareStatement("UPDATE active_loans SET amount_paid_out = ? WHERE loan_id = ?");
                      PreparedStatement pstmt = conn.prepareStatement(
                        "SELECT loan_id, name_of_borrower, pay_back_amount, amount_paid, amount_paid_out FROM active_loans WHERE uuid_of_lender = ?")) {
                    pstmt.setString(1, String.valueOf(player.getUniqueId()));
                    try (ResultSet rs = pstmt.executeQuery()) {
                        while (rs.next()) {
                            long loan_id = rs.getLong("loan_id");
                            String nameOfBorrower = rs.getString("name_of_borrower");
                            double payBackAmount = rs.getDouble("pay_back_amount");
                            double amountPaid = rs.getDouble("amount_paid");
                            double amountPaidOut = rs.getDouble("amount_paid_out");

                            double amountToPayOut = amountPaid - amountPaidOut;

                            if (amountToPayOut > 0 && player.isOnline()) {
                                eco.depositPlayer(player, amountToPayOut);
                                namesOfLoaned.add(nameOfBorrower);
                                totalPaidOut += amountToPayOut;

                                if (payBackAmount == amountPaidOut + amountToPayOut) {
                                    try (PreparedStatement pstmt2 = conn.prepareStatement(
                                            "DELETE FROM active_loans WHERE loan_id = ?")) {
                                        pstmt2.setLong(1, loan_id);
                                        pstmt2.executeUpdate();

                                        player.sendMessage(Component.text(nameOfBorrower + " successfully paid off there loan.").color(NamedTextColor.GREEN));
                                    }
                                } else {
                                    batchUpdate.setDouble(1, amountPaid);
                                    batchUpdate.setLong(2, loan_id);
                                    batchUpdate.addBatch();
                                }
                            }
                            else if (amountToPayOut < 0) {
                                Utils.getErrorLogger("Loan [" + loan_id + "] has invalid state: amount_paid");
                                return;
                            }
                        }
                    }

                    batchUpdate.executeBatch();
                }

                if (!namesOfLoaned.isEmpty()) {
                    player.sendMessage(Component.text("You have been paid $" + totalPaidOut + " for loans from " + String.join(", ", namesOfLoaned)).color(NamedTextColor.GREEN));
                }

                conn.commit();
            } catch (SQLException e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.addSuppressed(e);
                    Utils.getErrorLogger("Error paying loaner: " + rollbackEx.getMessage());
                    throw rollbackEx;
                }
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    // lists the one active loan
    public boolean loanListBorrowed(Player player) throws SQLException {
        try (Connection conn = getConnection()) {
            try (PreparedStatement pstmt = conn.prepareStatement("SELECT name_of_lender, loan_amount, pay_back_amount, amount_paid FROM active_loans WHERE uuid_of_borrower = ?")) {
                pstmt.setString(1, player.getUniqueId().toString());
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        String nameOfLender = rs.getString("name_of_lender");
                        double loanAmount = rs.getDouble("loan_amount");
                        double payBackAmount = rs.getDouble("pay_back_amount");
                        double amountPaid = rs.getDouble("amount_paid");
                        double amountLeft = payBackAmount - amountPaid;

                        if (nameOfLender == null) {
                            player.sendMessage(Component.text("You don't have a loan.").color(NamedTextColor.YELLOW));
                            return true;
                        }
                        player.sendMessage(Component.text("You have a loan from " + nameOfLender + " for $" + loanAmount + " you have left to pay $" + amountLeft).color(NamedTextColor.GREEN));
                    }
                }
                return true;
            } catch (SQLException e) {
                Utils.getErrorLogger("Error getting players loan: " + e.getMessage());
                return false;
            }
        }
    }

    public String checkIfPlayerHasLent(UUID uuid) throws SQLException {
        try (Connection conn = getConnection()) {
            try (PreparedStatement pstmt = conn.prepareStatement("SELECT name_of_borrower FROM active_loans WHERE uuid_of_lender = ?")) {
                pstmt.setString(1, uuid.toString());
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("name_of_borrower");
                    }
                }
            }
        }
        return null;
    }

    public boolean loanListLent(Player player) throws SQLException {
        try (Connection conn = getConnection()) {
            try (PreparedStatement pstmt = conn.prepareStatement("SELECT name_of_borrower, loan_amount, pay_back_amount, amount_paid FROM active_loans WHERE uuid_of_lender = ?")) {
                pstmt.setString(1, player.getUniqueId().toString());
                try (ResultSet rs = pstmt.executeQuery()) {
                    player.sendMessage(Component.text("List of active loans you have lent out").color(NamedTextColor.GOLD));
                    while (rs.next()) {
                        String nameOfBorrower = rs.getString("name_of_borrower");
                        double loanAmount = rs.getDouble("loan_amount");
                        double payBackAmount = rs.getDouble("pay_back_amount");
                        double amountPaid = rs.getDouble("amount_paid");
                        double amountLeft = payBackAmount - amountPaid;

                        player.sendMessage(Component.text("You have lent " + nameOfBorrower + " $" + loanAmount + " they still have to pay back $" + amountLeft));
                    }
                }
            }
            return true;
        } catch (SQLException e) {
            Utils.getErrorLogger("Error getting players loan: " + e.getMessage());
            return false;
        }
    }
}