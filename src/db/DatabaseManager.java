package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private final String dbUrl;

    /**
     * คอนสตรักเตอร์สำหรับกำหนดที่อยู่ของไฟล์ฐานข้อมูล
     */
    public DatabaseManager(String dbFilePath) {
        this.dbUrl = "jdbc:sqlite:" + dbFilePath;
    }

    /**
     * ดึงข้อมูลการเชื่อมต่อ (Connection) กับฐานข้อมูล SQLite
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl);
    }

    /**
     * สร้างตารางที่จำเป็นในฐานข้อมูล (หากยังไม่มีตารางเหล่านั้นอยู่)
     */
    public void initialize() {
        String usersTable = "CREATE TABLE IF NOT EXISTS users (" +
                "username TEXT PRIMARY KEY," +
                "password TEXT NOT NULL" +
                ")";

        String productsTable = "CREATE TABLE IF NOT EXISTS products (" +
                "code TEXT PRIMARY KEY," +
                "name TEXT NOT NULL," +
                "price REAL NOT NULL," +
                "category TEXT NOT NULL" +
                ")";

        String ordersTable = "CREATE TABLE IF NOT EXISTS orders (" +
                "order_id TEXT PRIMARY KEY," +
                "created_at TEXT NOT NULL," +
                "paid_at TEXT NOT NULL," +
                "subtotal REAL NOT NULL," +
                "vat_amount REAL NOT NULL," +
                "total_amount REAL NOT NULL," +
                "cash_received REAL NOT NULL," +
                "change_amount REAL NOT NULL" +
                ")";

        String orderItemsTable = "CREATE TABLE IF NOT EXISTS order_items (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "order_id TEXT NOT NULL," +
                "product_code TEXT NOT NULL," +
                "product_name TEXT NOT NULL," +
                "price_each REAL NOT NULL," +
                "category TEXT NOT NULL," +
                "quantity INTEGER NOT NULL," +
                "FOREIGN KEY(order_id) REFERENCES orders(order_id)" +
                ")";

        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            statement.execute(usersTable);
            statement.execute(productsTable);
            statement.execute(ordersTable);
            statement.execute(orderItemsTable);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database: " + e.getMessage(), e);
        }
    }
}
