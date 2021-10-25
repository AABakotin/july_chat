package ru.geekbrains.july_chat.chat_server;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.geekbrains.july_chat.chat_server.auth.AuthService;
import ru.geekbrains.july_chat.chat_server.auth.DatabaseAuthService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class JulyChatServer {
    private static final int PORT = 8089;
    private AuthService authService;
    private Map<String, ChatClientHandler> handlers;
    private ExecutorService es;
    private static final Logger logger = LogManager.getLogger(JulyChatServer.class.getName());

    public JulyChatServer() {
        this.authService = new DatabaseAuthService();
        this.handlers = new HashMap<>();
        this.es = Executors.newCachedThreadPool();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            logger.info("Server start!");
            while (true) {
                authService.start();
                logger.info("Waiting for connection......");
                Socket socket = serverSocket.accept();
                logger.info("Client connected");
                new ChatClientHandler(socket, this).handle();

            }
        } catch (IOException e) {
            logger.throwing(Level.ERROR, e);
        }
    }


    public void broadcastMessage(String from, String message) {
        message = String.format("[%s]: %s", from, message);
        for (ChatClientHandler handler : handlers.values()) {
            handler.sendMessage(message);
        }
    }

    public synchronized void removeAuthorizedClientFromList(ChatClientHandler handler) {
        this.handlers.remove(handler.getCurrentUser());
        sendClientsOnline();
    }

    public synchronized void addAuthorizedClientToList(ChatClientHandler handler) {
        this.handlers.put(handler.getCurrentUser(), handler);
        sendClientsOnline();
    }

    public AuthService getAuthService() {
        return authService;
    }

    public void sendClientsOnline() {
        StringBuilder sb = new StringBuilder("/list:").append(ChatClientHandler.REGEX);
        for (ChatClientHandler handler : handlers.values()) {
            sb.append(handler.getCurrentUser()).append(ChatClientHandler.REGEX);
        }
        String message = sb.toString();
        for (ChatClientHandler handler : handlers.values()) {
            handler.sendMessage(message);
        }
    }

    public void sendPrivateMessage(String sender, String recipient, String message, ChatClientHandler senderHandler) {
        ChatClientHandler handler = handlers.get(recipient);
        if (handler == null) {
            senderHandler.sendMessage(String.format("ERROR:%s recipient not found: %s", ChatClientHandler.REGEX, recipient));
            return;
        }
        message = String.format("[%s] -> [%s]: %s", sender, recipient, message);
        handler.sendMessage(message);
        senderHandler.sendMessage(message);
    }

    public boolean isNicknameBusy(String nickname) {
        return this.handlers.containsKey(nickname);
    }

    public ExecutorService getExecuteService() {return es;}
    }



