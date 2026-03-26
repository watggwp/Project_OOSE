package repository;

import db.DatabaseManager;
import model.Product;
import model.ProductCategory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class ProductRepository {
    private final DatabaseManager databaseManager;

    public ProductRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    /**
     * เพิ่มข้อมูลสินค้าเริ่มต้นลงในฐานข้อมูล (ในกรณีที่ยังไม่มีข้อมูลสินค้าเลย)
     */
    public void seedDefaultsIfEmpty() {
        String countSql = "SELECT COUNT(*) AS total FROM products";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement countStatement = connection.prepareStatement(countSql);
             ResultSet resultSet = countStatement.executeQuery()) {
            int total = resultSet.next() ? resultSet.getInt("total") : 0;
            if (total > 0) {
                return;
            }

            save(new Product("P001", "Americano", 65.0, ProductCategory.COFFEE));
            save(new Product("P002", "Espresso", 60.0, ProductCategory.COFFEE));
            save(new Product("P003", "Latte", 75.0, ProductCategory.COFFEE));
            save(new Product("P004", "Cappuccino", 75.0, ProductCategory.COFFEE));

            save(new Product("P005", "Thai Tea", 55.0, ProductCategory.TEA));
            save(new Product("P006", "Green Tea", 55.0, ProductCategory.TEA));
            save(new Product("P007", "Lemon Tea", 50.0, ProductCategory.TEA));

            save(new Product("P008", "Croissant", 45.0, ProductCategory.BAKERY));
            save(new Product("P009", "Chocolate Muffin", 40.0, ProductCategory.BAKERY));
            save(new Product("P010", "Brownie", 50.0, ProductCategory.BAKERY));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to seed products: " + e.getMessage(), e);
        }
    }

    /**
     * ดึงข้อมูลสินค้าทั้งหมดจากฐานข้อมูล คืนค่าเป็น Map โดยใช้รหัสสินค้าเป็นคีย์
     */
    public Map<String, Product> findAllAsMap() {
        String sql = "SELECT code, name, price, category FROM products ORDER BY code";
        Map<String, Product> productMap = new LinkedHashMap<>();
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Product product = new Product(
                        resultSet.getString("code"),
                        resultSet.getString("name"),
                        resultSet.getDouble("price"),
                        ProductCategory.valueOf(resultSet.getString("category"))
                );
                productMap.put(product.getCode(), product);
            }
            return productMap;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load products: " + e.getMessage(), e);
        }
    }

    /**
     * บันทึกข้อมูลสินค้าใหม่ลงในฐานข้อมูล
     */
    public boolean save(Product product) {
        String sql = "INSERT INTO products(code, name, price, category) VALUES(?, ?, ?, ?)";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, product.getCode());
            statement.setString(2, product.getName());
            statement.setDouble(3, product.getPrice());
            statement.setString(4, product.getCategory().name());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    // -- ฟังก์ชัน Update และ Delete ถูกลบออกเนื่องจากไม่ได้ใช้งาน --

}
