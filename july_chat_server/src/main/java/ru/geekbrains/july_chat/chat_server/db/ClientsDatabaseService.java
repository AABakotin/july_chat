package ru.geekbrains.july_chat.chat_server.db;


import java.sql.*;

import com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.geekbrains.july_chat.chat_server.ChatClientHandler;
import ru.geekbrains.july_chat.chat_server.error.UserNotFoundException;

public class ClientsDatabaseService {

    private static final String DRIVER = "org.sqlite.JDBC";
    private static final String CONNECTION = "jdbc:sqlite:db/clients.db";
    private static final String GET_USERNAME = "select username from clients where login = ? and password = ?;";
    private static final String CHANGE_USERNAME = "update clients set username = ? where username = ?;";
    private static final String ADD_NEW_USER = "insert into clients (login, password, username) values (?, ?, ?);";
    private static final String CHANGE_PASSWORD = "update clients set password = ? where username = ? and password = ?";
    private static ClientsDatabaseService instance;
    private Statement statement;
    private Connection connection;
    private static final Logger logger = LogManager.getLogger(ClientsDatabaseService.class.getName());


    public ClientsDatabaseService() {

        try {
            connect();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public static ClientsDatabaseService getInstance() {
        if (instance != null) return instance;
        instance = new ClientsDatabaseService();
        return instance;
    }

    public String changeUsername(String oldName, String newName) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(CHANGE_USERNAME)) {
            ps.setString(1, newName);
            ps.setString(2, oldName);
            if (ps.executeUpdate() > 0) return newName;
        }
        return oldName;
    }

    public void changePassword(String nickname, String oldPassword, String newPassword) {
        try (PreparedStatement ps = connection.prepareStatement(CHANGE_PASSWORD)) {

            ps.setString(1, newPassword);
            ps.setString(2, nickname);
            ps.setString(3, oldPassword);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.throwing(Level.ERROR, e);
        }


    }

    public void createNewUser(String login, String password, String username) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(ADD_NEW_USER)) {
            ps.setString(1, login);
            ps.setString(2, password);
            ps.setString(3, username);
            ps.executeUpdate();
        }

    }

    public String getClientNameByLoginPass(String login, String pass) {
        try (PreparedStatement ps = connection.prepareStatement(GET_USERNAME)) {
            ps.setString(1, login);
            ps.setString(2, pass);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String result = rs.getString("username");
                rs.close();
                logger.info("login is: " + result);
                return result;
            }
        } catch (SQLException | UserNotFoundException e) {
            logger.warn("User not found");
        }
        return null;
    }


//    private void createDb() {
//        try (Statement st = connection.createStatement()) {
//            st.execute(CREATE_DB);
//            st.execute(INIT_DB);
//        } catch (SQLException throwables) {
//            throwables.printStackTrace();
//        }
//    }

    private void connect() throws ClassNotFoundException, SQLException {
        Class.forName(DRIVER);
        connection = DriverManager.getConnection(CONNECTION);
        logger.info("Connected to db!");
        statement = connection.createStatement();

    }

    public void closeConnection() {
        try {
            if (connection != null) connection.close();
            logger.warn("Disconnected from db!");

        } catch (SQLException e) {
            logger.throwing(Level.ERROR, e);
        }
    }

}
