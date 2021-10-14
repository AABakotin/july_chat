package ru.geekbrains.july_chat.chat_server;


import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.management.timer.Timer;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.*;


public class ChatClientHandler {
    public static final String REGEX = "%&%";
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private JulyChatServer server;
    private String currentUser;
    private Timer timer = new Timer();
    private static final int TIME_WAIT = 120;
    private final ScheduledExecutorService scheduledExecutor = new ScheduledThreadPoolExecutor(1);
    private static final Logger logger = LogManager.getLogger(ChatClientHandler.class.getName());


    public ChatClientHandler(Socket socket, JulyChatServer server) {
        try {
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            logger.info("Handler created");
            this.server = server;
            scheduledExecutor.schedule(this::closeConnection, TIME_WAIT, TimeUnit.SECONDS);
        } catch (IOException e) {
            logger.info("Client has been disconnected");


        }
    }

    private void closeConnection() {
        try {
            this.out.close();
            this.in.close();
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        server.removeAuthorizedClientFromList(this);

    }


    public void handle() {
        server.getExecuteService().execute(() -> {
            authorize();
            try {
                while (!Thread.currentThread().isInterrupted() && socket.isConnected()) {
                    String message = in.readUTF();
                    handleMessage(message);
                }
            } catch (IOException e) {
                logger.warn("Client has been disconnected");
            } finally {
                closeConnection();
                server.getExecuteService().shutdown();
            }
        });
    }


    private void authorize() {
        while (true) {
            try {
                String message = in.readUTF();
                if (message.startsWith("/auth") || message.startsWith("/register")) {
                    if (handleMessage(message)) {
                        break;
                    }
                }
            } catch (IOException e) {
                return;
            }
        }
    }

    private boolean handleMessage(String message) {
        try {
            String[] parsed = message.split(REGEX);
            switch (parsed[0]) {
                case "/w":
                    server.sendPrivateMessage(this.currentUser, parsed[1], parsed[2], this);
                    break;
                case "/ALL":
                    server.broadcastMessage(this.currentUser, parsed[1]);
                    break;
                case "/change_nick":
                    String nick = server.getAuthService().changeNickname(this.currentUser, parsed[1]);
                    server.removeAuthorizedClientFromList(this);
                    this.currentUser = nick;
                    server.addAuthorizedClientToList(this);
                    sendMessage("/change_nick_ok");
                    break;
                case "/change_pass":
                    server.getAuthService().changePassword(this.currentUser, parsed[1], parsed[2]);
                    sendMessage("/change_pass_ok");
                    break;
                case "/remove":
                    server.getAuthService().deleteUser(this.currentUser);
                    this.socket.close();
                    break;
                case "/exit":
                    this.socket.close();
                    break;
                case "/register":
                    server.getAuthService().createNewUser(parsed[1], parsed[2], parsed[3]);
                    sendMessage("register_ok:");
                    scheduledExecutor.shutdownNow();
                    break;
                case "/auth":
                    this.currentUser = server.getAuthService().getNicknameByLoginAndPassword(parsed[1], parsed[2]);
                    if (server.isNicknameBusy(currentUser)) {
                        sendMessage("ERROR:" + REGEX + "U're clone!");
                    } else {
                        this.server.addAuthorizedClientToList(this);
                        scheduledExecutor.shutdownNow();
                        sendMessage("authok:" + REGEX + this.currentUser);
                        logger.info("Client passed authorization");
                        return true;
                    }
                    break;
                default:
                    sendMessage("ERROR:" + REGEX + "command not found!");
            }
        } catch (Exception e) {
            sendMessage("ERROR:" + REGEX + "Login & Password uncorrected");
        }
        return false;
    }

    public String getCurrentUser() {
        return currentUser;
    }

    public void sendMessage(String message) {
        try {
            this.out.writeUTF(message);
            logger.info(message);
        } catch (IOException e) {
           logger.throwing(Level.ERROR, e);
        }
    }
}