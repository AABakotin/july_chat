package ru.geekbrains.july_chat.exception_server;

public class BadRequestException extends RuntimeException{
    public BadRequestException(String message) {
        super(message);
    }
}
