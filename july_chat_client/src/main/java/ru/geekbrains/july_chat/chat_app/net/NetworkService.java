package ru.geekbrains.july_chat.chat_app.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public class NetworkService {
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 8089;
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private ChatMessageService chatMessageService;

    public NetworkService(ChatMessageService chatMessageService) throws IOException {
        this.chatMessageService = chatMessageService;
        this.socket = new Socket(HOST, PORT);
        this.out = new DataOutputStream(socket.getOutputStream());
        this.in = new DataInputStream(socket.getInputStream());
    }

    public void readMessages() {
        Thread t = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String message = in.readUTF();
                    chatMessageService.receive(message);

                }catch (EOFException e) {
                    System.err.println("Disconnected form server");
                    return;
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (SocketException e){
            System.err.println("Can't send message with out connected server");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    public Socket getSocket() {
        return socket;
    }
}