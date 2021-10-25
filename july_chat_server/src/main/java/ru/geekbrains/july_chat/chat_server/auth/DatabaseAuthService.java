package ru.geekbrains.july_chat.chat_server.auth;

import ru.geekbrains.july_chat.chat_server.db.ClientsDatabaseService;

import java.sql.PreparedStatement;
import java.sql.SQLException;


public class DatabaseAuthService implements AuthService {
    private ClientsDatabaseService dbService;

    @Override
    public void start() {
        dbService = ClientsDatabaseService.getInstance();
    }

    @Override
    public void stop() {
        dbService.closeConnection();
    }

    @Override
    public String getNicknameByLoginAndPassword(String login, String pass) {
        return dbService.getClientNameByLoginPass(login, pass);
    }

    @Override
    public String changeNickname(String oldName, String newName) {
        try {
            return dbService.changeUsername(oldName, newName);
        } catch (SQLException e) {
            throw new RuntimeException("Nickname change unsuccessful");
        }
    }

    @Override
    public void changePassword(String username, String oldPassword, String newPassword) {
    dbService.changePassword(username, oldPassword, newPassword);

    }

    @Override
    public void createNewUser(String login, String password, String username) {
        try {
            dbService.createNewUser(login, password, username);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteUser(String nickname) {

    }
}