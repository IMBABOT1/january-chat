package com.geekbrains.chat.server;

public interface AuthManager {
    String getNicknameByLoginAndPassword(String login, String password);
    void connect();
    void disconnect();
    boolean changeNickName(String newNick, String login, String password);
}
