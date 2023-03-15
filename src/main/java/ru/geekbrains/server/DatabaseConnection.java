package ru.geekbrains.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection("jdbc:sqlite:E:\\Victor\\Программирование\\ДЗ Курс Базы данных Основы\\sqlite-tools-win32-x86-3360000\\users.db");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void closeConnection(Connection connection) {
        try {
            if(connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
