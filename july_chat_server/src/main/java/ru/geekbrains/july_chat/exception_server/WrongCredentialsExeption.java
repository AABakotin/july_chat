package ru.geekbrains.july_chat.exception_server;

public class WrongCredentialsExeption extends RuntimeException{

    public WrongCredentialsExeption(String message) {
        super(message);
    }
}
