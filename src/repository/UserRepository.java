package repository;

import db.DatabaseManager;
import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRepository {
    private final DatabaseManager databaseManager;

    public UserRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    /**
     * ตรวจสอบว่ามีชื่อผู้ใช้นี้ในฐานข้อมูลแล้วหรือไม่
     */
    public boolean existsByUsername(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check username: " + e.getMessage(), e);
        }
    }

    /**
     * บันทึกข้อมูลผู้ใช้ใหม่ลงในฐานข้อมูล
     */
    public boolean save(User user) {
        String sql = "INSERT INTO users(username, password) VALUES(?, ?)";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPassword());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * ค้นหาข้อมูลผู้ใช้ในฐานข้อมูลจากชื่อผู้ใช้
     */
    public User findByUsername(String username) {
        String sql = "SELECT username, password FROM users WHERE username = ?";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                String dbUsername = resultSet.getString("username");
                String password = resultSet.getString("password");
                return new User(dbUsername, password);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user: " + e.getMessage(), e);
        }
    }
}
