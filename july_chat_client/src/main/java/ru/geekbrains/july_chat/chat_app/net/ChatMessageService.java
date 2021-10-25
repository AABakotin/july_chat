package ru.geekbrains.july_chat.chat_app.net;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.EOFException;
import java.io.IOException;


public class ChatMessageService {
    private MessageProcessor messageProcessor;
    private NetworkService networkService;
    private static final Logger logger = LogManager.getLogger(ChatMessageService.class.getName());

    public ChatMessageService(MessageProcessor messageProcessor) {
        this.messageProcessor = messageProcessor;
    }


    public void connect() {
        if (isConnected()) return;
        try {
            this.networkService = new NetworkService(this);
            networkService.readMessages();
        } catch (EOFException e) {
            logger.warn("\"Disconnected form server\"");
            return;
        } catch (IOException e) {
            logger.throwing(Level.ERROR, e);
        }

    }

    public boolean isConnected() {
        return this.networkService != null && this.networkService.getSocket().isConnected();
    }

    public void send(String message) {
        try {
            this.networkService.sendMessage(message);
        }catch (RuntimeException e) {
            logger.throwing(Level.WARN, e);
        }
    }

    public void receive(String message) {
        messageProcessor.processMessage(message);
    }
}