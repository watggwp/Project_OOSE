package repository;

import db.DatabaseManager;
import model.Order;
import model.OrderItem;
import model.Product;
import model.ProductCategory;
import model.SalesRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OrderRepository {
    private final DatabaseManager databaseManager;

    public OrderRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    /**
     * บันทึกประวัติการขาย (ข้อมูลออเดอร์และรายการสินค้า) ลงในฐานข้อมูล
     */
    public void saveSalesRecord(SalesRecord salesRecord) {
        String insertOrderSql = "INSERT INTO orders(order_id, created_at, paid_at, subtotal, vat_amount, total_amount, cash_received, change_amount) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
        String insertItemSql = "INSERT INTO order_items(order_id, product_code, product_name, price_each, category, quantity) " +
                "VALUES(?, ?, ?, ?, ?, ?)";

        try (Connection connection = databaseManager.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement orderStatement = connection.prepareStatement(insertOrderSql);
                 PreparedStatement itemStatement = connection.prepareStatement(insertItemSql)) {

                orderStatement.setString(1, salesRecord.getOrder().getOrderId());
                orderStatement.setString(2, salesRecord.getOrder().getCreatedAt().toString());
                orderStatement.setString(3, salesRecord.getPaidAt().toString());
                orderStatement.setDouble(4, salesRecord.getSubtotal());
                orderStatement.setDouble(5, salesRecord.getVatAmount());
                orderStatement.setDouble(6, salesRecord.getTotalAmount());
                orderStatement.setDouble(7, salesRecord.getCashReceived());
                orderStatement.setDouble(8, salesRecord.getChangeAmount());
                orderStatement.executeUpdate();

                for (OrderItem item : salesRecord.getOrder().getItems()) {
                    itemStatement.setString(1, salesRecord.getOrder().getOrderId());
                    itemStatement.setString(2, item.getProduct().getCode());
                    itemStatement.setString(3, item.getProduct().getName());
                    itemStatement.setDouble(4, item.getProduct().getPrice());
                    itemStatement.setString(5, item.getProduct().getCategory().name());
                    itemStatement.setInt(6, item.getQuantity());
                    itemStatement.addBatch();
                }
                itemStatement.executeBatch();

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new RuntimeException("Failed to save sales record: " + e.getMessage(), e);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error while saving sales record: " + e.getMessage(), e);
        }
    }

    /**
     * ค้นหาประวัติการขายทั้งหมดจากฐานข้อมูลตามวันที่ระบุ
     */
    public List<SalesRecord> findSalesRecordsByDate(LocalDate date) {
        // ใช้ LIKE แทน date() เพื่อแก้ปัญหาฟอร์แมตวันที่ของ Java
        String sql = "SELECT " +
                "o.order_id, o.created_at, o.paid_at, o.subtotal, o.vat_amount, o.total_amount, o.cash_received, o.change_amount, " +
                "oi.product_code, oi.product_name, oi.price_each, oi.category, oi.quantity " +
                "FROM orders o " +
                "LEFT JOIN order_items oi ON o.order_id = oi.order_id " +
                "WHERE o.paid_at LIKE ? " +
                "ORDER BY o.paid_at, o.order_id, oi.id";

        Map<String, SalesRecord> recordMap = new LinkedHashMap<>();

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
             
            // เติม % ต่อท้าย เพื่อให้ดึงข้อมูลที่ขึ้นต้นด้วยวันที่ของวันนี้ทั้งหมด
            statement.setString(1, date.toString() + "%"); 
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String orderId = resultSet.getString("order_id");
                    SalesRecord salesRecord = recordMap.get(orderId);
                    if (salesRecord == null) {
                        Order order = new Order(orderId, LocalDateTime.parse(resultSet.getString("created_at")));
                        salesRecord = new SalesRecord(
                                order,
                                resultSet.getDouble("subtotal"),
                                resultSet.getDouble("vat_amount"),
                                resultSet.getDouble("total_amount"),
                                resultSet.getDouble("cash_received"),
                                resultSet.getDouble("change_amount"),
                                LocalDateTime.parse(resultSet.getString("paid_at"))
                        );
                        recordMap.put(orderId, salesRecord);
                    }

                    String productCode = resultSet.getString("product_code");
                    if (productCode != null) {
                        Product product = new Product(
                                productCode,
                                resultSet.getString("product_name"),
                                resultSet.getDouble("price_each"),
                                ProductCategory.valueOf(resultSet.getString("category"))
                        );
                        salesRecord.getOrder().addProduct(product, resultSet.getInt("quantity"));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load sales records: " + e.getMessage(), e);
        }

        return new ArrayList<>(recordMap.values());
    }

    /**
     * ค้นหาประวัติการขายตามช่วงวันที่ (ตั้งแต่ startDate ถึง endDate)
     */
    public List<SalesRecord> findSalesRecordsByDateRange(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT " +
                "o.order_id, o.created_at, o.paid_at, o.subtotal, o.vat_amount, o.total_amount, o.cash_received, o.change_amount, " +
                "oi.product_code, oi.product_name, oi.price_each, oi.category, oi.quantity " +
                "FROM orders o " +
                "LEFT JOIN order_items oi ON o.order_id = oi.order_id " +
                "WHERE o.paid_at >= ? AND o.paid_at < ? " +
                "ORDER BY o.paid_at, o.order_id, oi.id";

        Map<String, SalesRecord> recordMap = new LinkedHashMap<>();

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
             
            // แปลงวันที่เริ่มต้นเป็นเวลา 00:00:00 และวันสิ้นสุดเป็นเวลา 00:00:00 ของอีกวัน เพื่อให้ครอบคลุมทั้งวัน
            statement.setString(1, startDate.toString() + "T00:00:00");
            statement.setString(2, endDate.plusDays(1).toString() + "T00:00:00");
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String orderId = resultSet.getString("order_id");
                    SalesRecord salesRecord = recordMap.get(orderId);
                    if (salesRecord == null) {
                        Order order = new Order(orderId, LocalDateTime.parse(resultSet.getString("created_at")));
                        salesRecord = new SalesRecord(
                                order,
                                resultSet.getDouble("subtotal"),
                                resultSet.getDouble("vat_amount"),
                                resultSet.getDouble("total_amount"),
                                resultSet.getDouble("cash_received"),
                                resultSet.getDouble("change_amount"),
                                LocalDateTime.parse(resultSet.getString("paid_at"))
                        );
                        recordMap.put(orderId, salesRecord);
                    }

                    String productCode = resultSet.getString("product_code");
                    if (productCode != null) {
                        Product product = new Product(
                                productCode,
                                resultSet.getString("product_name"),
                                resultSet.getDouble("price_each"),
                                ProductCategory.valueOf(resultSet.getString("category"))
                        );
                        salesRecord.getOrder().addProduct(product, resultSet.getInt("quantity"));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load sales records by range: " + e.getMessage(), e);
        }

        return new ArrayList<>(recordMap.values());
    }
}
