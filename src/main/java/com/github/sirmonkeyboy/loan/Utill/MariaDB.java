package com.github.sirmonkeyboy.loan.Utill;

import com.github.sirmonkeyboy.loan.Loan;

import java.sql.Connection;
import java.sql.DriverManager;
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
}
