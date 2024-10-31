package com.github.sirmonkeyboy.loan.Utill;

import com.github.sirmonkeyboy.loan.Loan;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MariaDB {
    private final Loan plugin;

    public MariaDB(Loan plugin) {
        this.plugin = plugin;
        host = plugin.getConfig().getString("mariaDB.host");
        port = plugin.getConfig().getString("mariaDB.port");
        database = plugin.getConfig().getString("mariaDB.database");
        username = plugin.getConfig().getString("mariaDB.username");
        password = plugin.getConfig().getString("mariaDB.password");
    }

    private final String host;
    private final String port;
    private final String database;
    private final String username;
    private final String password;

    private Connection connection;

    public boolean isConnected() {
        return (connection != null);
    }

    public void connect() throws ClassNotFoundException, SQLException {
        if (!isConnected()) {
            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false", username, password);
        }
    }

    public void disconnect() {
        if (isConnected()) {
            try {
                connection.close();
            }catch (SQLException e){
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
            }
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void createTable() throws SQLException {
        // Connect to the database
        Connection conn = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false", username, password);

        try {
            conn.setAutoCommit(false);
            PreparedStatement pstmt = conn.prepareStatement("CREATE TABLE IF NOT EXISTS Loan (id INTEGER NOT NULL AUTO_INCREMENT UNIQUE,\n" +
                    "uuidOfLoaner VARCHAR(255),\n" +
                    "nameOfLoaner VARCHAR(255),\n" +
                    "uuidOfLoaned VARCHAR(255),\n" +
                    "nameOfLoaned VARCHAR(255),\n" +
                    "amountLoaned DOUBLE,\n" +
                    "amountToPayBack\n" +
                    "amountPaid DOUBLE,\n" +
                    "amountPaidOut DOUBLE,\n" +
                    "PRIMARY KEY(id))");
            pstmt.executeUpdate();

            PreparedStatement pstmt2 = conn.prepareStatement("CREATE INDEX IF NOT EXISTS Loan_index_0 ON Loan (uuidOfLoaner, uuidOfLoaned);");
            pstmt2.executeUpdate();
        } catch (SQLException e) {
            // Roll back the transaction if an exception occurs
            conn.rollback();
            throw e;
        } finally {
            conn.close();
        }
    }

}
