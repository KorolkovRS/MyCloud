package com.korolkovrs.Connecrors;

import com.mysql.jdbc.Driver;

import java.sql.*;

public class MySQLConnector {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/cloudusers?serverTimezone=Europe/Moscow";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "root";
    private static final String DB_DRIVER = "com.mysql.jdbc.Driver";


    private static Connection connection;

    public static void connection() throws ClassNotFoundException, SQLException {
        Class.forName(DB_DRIVER);
        DriverManager.registerDriver(new Driver());
        connection = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
        System.out.println("База подключена");
    }

    public static boolean checkUser(String username, String password) throws SQLException {
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement("SELECT * FROM users WHERE (username = ?) AND (password = ?)");
            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet resultSet = ps.executeQuery();
            if (resultSet.next()) {
                return true;
            }
            return false;
        } finally {
            ps.close();
        }
    }

    public static boolean addUser(String username, String password) throws SQLException {
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement("INSERT INTO users (username, password) VALUES (?, ?)");
            ps.setString(1, username);
            ps.setString(2, password);
            ps.addBatch();
            int[] result = ps.executeBatch();
            if (result[0] == 1) {
                return true;
            }
            return false;
        } finally {
            ps.close();
        }
    }
}
