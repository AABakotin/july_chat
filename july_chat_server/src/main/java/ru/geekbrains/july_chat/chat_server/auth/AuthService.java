package ru.geekbrains.july_chat.chat_server.auth;


public interface AuthService {
    void start();

    void stop();

    String getNicknameByLoginAndPassword(String login, String password);

    String changeNickname(String oldNick, String newNick);

    void createNewUser(String login, String password, String username);

    void changePassword(String nickname,  String newPassword,String oldPassword);

    void deleteUser(String nickname);
}