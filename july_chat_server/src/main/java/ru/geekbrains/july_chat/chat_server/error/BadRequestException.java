package ru.geekbrains.july_chat.chat_server.error;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}