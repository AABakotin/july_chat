package ru.geekbrains.july_chat.chat_server.error;

public class WrongCredentialsException extends RuntimeException {
    public WrongCredentialsException(String message) {
        super(message);
    }
}