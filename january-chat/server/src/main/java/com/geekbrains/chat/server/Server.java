package com.geekbrains.chat.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Server {


    private AuthManager authManager;
    private List<ClientHandler> clients;
    private final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public AuthManager getAuthManager() {
        return authManager;
    }

    private ClientHandler clientHandler;

    public AuthManager authManager(){
        return authManager;
    }

    private FileOutputStream fis;
    private DataOutputStream out;

    public Server(int port) {
        clients = new ArrayList<>();
        authManager = new SqlAuthManager();
        authManager.connect();



        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер запущен. Ожидаем подключения клиентов...");

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Клиент подключился");
                clientHandler = new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcastMsg(String msg, boolean withDateTime) {
            String name = "";
            if (withDateTime) {
                msg = String.format("[%s] %s", LocalDateTime.now().format(DTF), msg);
            }
            for (ClientHandler o : clients) {
                o.sendMsg(msg);
                name = o.getNickname();
            }
            log(name, msg);
    }

    public void log(String name, String msg) {
        try {
            fis = new FileOutputStream(name + " log", true);
            out = new DataOutputStream(fis);
            out.writeUTF(name + " " + msg + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void broadcastClientsList() {
        StringBuilder stringBuilder = new StringBuilder("/clients_list ");
        for (ClientHandler o : clients) {
            stringBuilder.append(o.getNickname()).append(" ");
        }
        stringBuilder.setLength(stringBuilder.length() - 1);
        String out = stringBuilder.toString();
        broadcastMsg(out, false);
    }

    public void sendPrivateMsg(ClientHandler sender, String receiverNickname, String msg) {
        if (sender.getNickname().equals(receiverNickname)) {
            sender.sendMsg("Нельзя посылать личное сообщение самому себе");
            return;
        }
        for (ClientHandler o : clients) {
            if (o.getNickname().equals(receiverNickname)) {
                o.sendMsg("from " + sender.getNickname() + ": " + msg);
                sender.sendMsg("to " + receiverNickname + ": " + msg);
                return;
            }
        }
        sender.sendMsg(receiverNickname + " не в сети");
    }


    public boolean isNickBusy(String nickname) {
        for (ClientHandler o : clients) {
            if (o.getNickname().equals(nickname)) {
                return true;
            }
        }
        return false;
    }



    public synchronized void subscribe(ClientHandler clientHandler) {
        broadcastMsg(clientHandler.getNickname() + " зашел в чат", false);
        clients.add(clientHandler);
        broadcastClientsList();
    }

    public synchronized void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        broadcastMsg(clientHandler.getNickname() + " вышел из чата", false);
        broadcastClientsList();
    }
}
