import com.formdev.flatlaf.FlatIntelliJLaf;
import db.DatabaseManager;
import repository.OrderRepository;
import repository.ProductRepository;
import repository.UserRepository;
import service.AuthService;
import service.ReportService;
import ui.POSAppFrame;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

public class UIMain {
    /**
     * เมธอดหลัก (Main method) สำหรับเริ่มการทำงานของแอปพลิเคชัน
     * ทำการตั้งค่าธีม, เชื่อมต่อฐานข้อมูล และเปิดหน้าจอหลัก
     */
    public static void main(String[] args) {
        // Setup FlatLaf theme
        try {
            UIManager.setLookAndFeel(new FlatIntelliJLaf());
        } catch (Exception e) {
            System.err.println("Failed to initialize FlatLaf theme: "+ e.getMessage());
        }

        DatabaseManager databaseManager = new DatabaseManager("poscoffee.db");
        try {
            databaseManager.initialize();
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(
                    null,
                    "Database initialization failed:\n"+ e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        UserRepository userRepository = new UserRepository(databaseManager);
        ProductRepository productRepository = new ProductRepository(databaseManager);
        OrderRepository orderRepository = new OrderRepository(databaseManager);
        AuthService authService = new AuthService(userRepository);
        ReportService reportService = new ReportService();

        POSAppFrame.launchOnEdt(
                authService,
                productRepository,
                orderRepository,
                reportService
        );
    }
}
