package ru.gb.storage.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

public class AuthService {

    private static final String DB_URL = "jdbc:sqlite:userlist.db";
    private static final Logger LOGGER = LogManager.getLogger(ServerHandler.class);
    private static Connection connection;
    private static PreparedStatement preparedStatement;
    private static ResultSet resultSet;

    public void connectToDatabase() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            LOGGER.info("SQL connected");
        } catch (SQLException e) {
            LOGGER.error("SQL Exception while connecting to database.");
            e.printStackTrace();
        }
    }

    public void disconnectFromDatabase() {
        try {
            if (connection != null) {
                connection.close();
                LOGGER.info("SQL connection closed");
            }
        } catch (SQLException e) {
            LOGGER.error("SQL Exception while closing connection to database.");
            e.printStackTrace();
        }
    }

    public boolean checkLogin(String login) {
        try {
            preparedStatement = connection.prepareStatement("SELECT login FROM userlist");
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                if (resultSet.getString("login").equals(login)) {
                    return true;
                }
            }
        } catch (SQLException e) {
            LOGGER.error("SQL Exception during login verification.");
            e.printStackTrace();
        }
        return false;
    }

    public boolean checkPassword(String login, String password) {
        try {
            preparedStatement = connection.prepareStatement("SELECT password FROM userlist WHERE login = ?");
            preparedStatement.setString(1, login);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.getString("password").equals(password)) {
                return true;
            }
        } catch (SQLException e) {
            LOGGER.error("SQL Exception during password verification.");
            e.printStackTrace();
        }
        return false;
    }

    public boolean registerUser(String login, String password) {
        try {
            preparedStatement = connection.prepareStatement("INSERT INTO userlist (login, password) VALUES (?, ?)");
            preparedStatement.setString(1, login);
            preparedStatement.setString(2, password);
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            LOGGER.error("SQL Exception during user registration.");
            e.printStackTrace();
        }
        LOGGER.error("User registration failed.");
        return false;
    }

}
