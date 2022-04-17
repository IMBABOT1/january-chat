package com.geekbrains.chat.server;

import java.sql.*;

public class SqlAuthManager implements AuthManager{

    private Connection connection;
    private PreparedStatement ps;

    @Override
    public String getNicknameByLoginAndPassword(String login, String password) {
        String result = "";
        try {
            ps.setString(1, login);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                while (!rs.next()){
                    return null;
                }
                result= rs.getString(1);
        }
    }catch  (SQLException e) {
            e.printStackTrace();
        }

        return  result;
    }

    @Override
    public void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:dbMain.db");
            ps = connection.prepareStatement("SELECT nickname FROM users WHERE login = ? AND password = ?");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void disconnect() {
        try {
            connection.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean changeNickName(String newNick, String login, String password) {
        if (msg.startsWith("/change_nick ")){
            String[] tokens = msg.split(" ", 2);
            nickname = tokens[1];
        }
    }
}
