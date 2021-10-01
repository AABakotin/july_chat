package ru.geekbrains.july_chat.chat_server.auth;

import java.sql.SQLException;

public interface AuthService {
    void start();
    void stop();
    String getNicknameByLoginAndPassword(String login, String password);
    String changeNickname(String oldNick, String newNick);
    void changePassword(String nickname, String oldPassword, String newPassword);
    String createNewUser(String login, String password, String nickname) throws SQLException;
    void deleteUser(String nickname);
}