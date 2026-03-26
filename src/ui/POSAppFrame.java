package ui;

import com.formdev.flatlaf.FlatIntelliJLaf;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import model.*;
import repository.OrderRepository;
import repository.ProductRepository;
import service.*;

public class POSAppFrame extends JFrame {
    private static final Color PRIMARY_COLOR = new Color(52, 152, 219);
    private static final Color SUCCESS_COLOR = new Color(46, 204, 113);
    private static final Color WARNING_COLOR = new Color(241, 196, 15);
    private static final Color DANGER_COLOR = new Color(231, 76, 60);
    private static final Color DARK_COLOR = new Color(52, 73, 94);
    private static final Color LIGHT_COLOR = new Color(245, 246, 250);
    
    private static final String AUTH_CARD = "auth";
    private static final String HOME_CARD = "home";

    private final AuthService authService;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final ReportService reportService;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel rootPanel = new JPanel(cardLayout);
    private final JLabel currentUserLabel = new JLabel("Not logged in");

    private User currentUser;
    private Map<String, Product> productCatalog = new LinkedHashMap<>();
    
    private final Font EMOJI_FONT = new Font("Segoe UI Emoji", Font.PLAIN, 40);

    /**
     * คอนสตรักเตอร์สำหรับสร้างหน้าต่างหลักของโปรแกรม
     */
    public POSAppFrame(AuthService authService,
                       ProductRepository productRepository,
                       OrderRepository orderRepository,
                       ReportService reportService) {
        this.authService = authService;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.reportService = reportService;

        this.productRepository.seedDefaultsIfEmpty();
        this.productCatalog = new LinkedHashMap<>(this.productRepository.findAllAsMap());

        setupModernUI();
    }

    /**
     * ตั้งค่ารูปแบบหน้าจอเริ่มต้น ธีม และโครงสร้าง UI
     */
    private void setupModernUI() {
        setTitle("POS Coffee - Modern Point of Sale System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 800);
        setMinimumSize(new Dimension(1000, 700));
        setLocationRelativeTo(null);

        // --- เปลี่ยนโค้ดโหลด Icon เป็นชุดนี้ครับ ---
        try {
            // ใช้ getResource เพื่อให้ Java เข้าไปดึงรูปจากในโฟลเดอร์ src ได้โดยตรง
            java.net.URL imgURL = getClass().getResource("/logo.png");
            if (imgURL != null) {
                Image appIcon = new ImageIcon(imgURL).getImage();
                setIconImage(appIcon);
            } else {
                System.out.println("หาไฟล์ logo.png ไม่เจอ! ตรวจสอบชื่อไฟล์อีกครั้ง");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // ------------------------------------
        
        getRootPane().putClientProperty("JRootPane.titleBarBackground", PRIMARY_COLOR);
        getRootPane().putClientProperty("JRootPane.titleBarForeground", Color.WHITE);

        rootPanel.add(createModernAuthPanel(), AUTH_CARD);
        rootPanel.add(createModernHomePanel(), HOME_CARD);
        add(rootPanel);
        showAuthCard();
    }

    /**
     * สร้างหน้าจอสำหรับเข้าสู่ระบบและสมัครสมาชิก
     */
    private JPanel createModernAuthPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(LIGHT_COLOR);

        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(
                    0, 0, PRIMARY_COLOR,
                    getWidth(), getHeight(), new Color(41, 128, 185)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        backgroundPanel.setLayout(new GridBagLayout());

        JPanel loginCard = createModernCard();
        loginCard.setPreferredSize(new Dimension(400, 500));
        loginCard.setLayout(new BoxLayout(loginCard, BoxLayout.Y_AXIS));

        JLabel logoLabel = new JLabel("☕", SwingConstants.CENTER);
        logoLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 80));
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("POS Coffee", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Tahoma", Font.BOLD, 32));
        titleLabel.setForeground(DARK_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Modern Point of Sale", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Tahoma", Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(127, 140, 141));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        loginCard.add(Box.createVerticalStrut(40));
        loginCard.add(logoLabel);
        loginCard.add(Box.createVerticalStrut(20));
        loginCard.add(titleLabel);
        loginCard.add(Box.createVerticalStrut(10));
        loginCard.add(subtitleLabel);
        loginCard.add(Box.createVerticalStrut(50));

        JButton loginBtn = createModernButton("Login", PRIMARY_COLOR, Color.WHITE);
        JButton registerBtn = createModernButton("Register", SUCCESS_COLOR, Color.WHITE);
        JButton exitBtn = createModernButton("Exit", DANGER_COLOR, Color.WHITE);

        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        exitBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        loginCard.add(loginBtn);
        loginCard.add(Box.createVerticalStrut(15));
        loginCard.add(registerBtn);
        loginCard.add(Box.createVerticalStrut(15));
        loginCard.add(exitBtn);
        loginCard.add(Box.createVerticalStrut(40));

        loginBtn.addActionListener(e -> handleLogin());
        registerBtn.addActionListener(e -> handleRegister());
        exitBtn.addActionListener(e -> System.exit(0));

        backgroundPanel.add(loginCard);
        mainPanel.add(backgroundPanel, BorderLayout.CENTER);
        return mainPanel;
    }

    /**
     * สร้างหน้าจอหลัก (Dashboard) หลังจากเข้าสู่ระบบสำเร็จ
     */
    private JPanel createModernHomePanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(LIGHT_COLOR);

        JPanel header = createModernHeader();
        mainPanel.add(header, BorderLayout.NORTH);

        JPanel dashboard = createModernDashboard();
        mainPanel.add(dashboard, BorderLayout.CENTER);

        return mainPanel;
    }

    /**
     * สร้างส่วนหัว (Header) ของหน้าจอหลัก แสดงโลโก้และข้อมูลผู้ใช้
     */
    private JPanel createModernHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 221, 225)),
            new EmptyBorder(15, 30, 15, 30)
        ));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftPanel.setOpaque(false);
        
        JLabel logoLabel = new JLabel("☕ ");
        logoLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        
        JLabel titleLabel = new JLabel("POS Coffee");
        titleLabel.setFont(new Font("Tahoma", Font.BOLD, 24));
        titleLabel.setForeground(DARK_COLOR);

        leftPanel.add(logoLabel);
        leftPanel.add(titleLabel);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setOpaque(false);

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        userPanel.setOpaque(false);
        userPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 221, 225), 1),
            new EmptyBorder(8, 15, 8, 15)
        ));

        JLabel userIcon = new JLabel("👤 ");
        userIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        currentUserLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
        currentUserLabel.setForeground(DARK_COLOR);

        userPanel.add(userIcon);
        userPanel.add(currentUserLabel);

        JButton logoutBtn = createModernButton("Logout", DANGER_COLOR, Color.WHITE);
        logoutBtn.setPreferredSize(new Dimension(100, 38));
        logoutBtn.addActionListener(e -> handleLogout());

        rightPanel.add(userPanel);
        rightPanel.add(logoutBtn);

        header.add(leftPanel, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);

        return header;
    }

    /**
     * สร้างส่วนเนื้อหากลาง (Dashboard) ของหน้าจอหลัก ประกอบด้วยสถิติและเมนูหลัก
     */
    private JPanel createModernDashboard() {
        JPanel dashboard = new JPanel(new BorderLayout());
        dashboard.setBackground(LIGHT_COLOR);
        dashboard.setBorder(new EmptyBorder(30, 30, 30, 30));

        JPanel statsPanel = createStatsPanel();
        dashboard.add(statsPanel, BorderLayout.NORTH);

        JPanel menuGrid = new JPanel(new GridLayout(2, 2, 25, 25));
        menuGrid.setOpaque(false);
        menuGrid.setBorder(new EmptyBorder(30, 0, 0, 0));

        menuGrid.add(createModernMenuCard(
            "🛒", "New Order", "Create and process customer orders",
            SUCCESS_COLOR, e -> openNewOrderDialog()
        ));
        
        menuGrid.add(createModernMenuCard(
            "📈", "Daily Report", "View sales analytics and reports", 
            PRIMARY_COLOR, e -> openDailyReportDialog()
        ));
        
        menuGrid.add(createModernMenuCard(
            "📋", "Manage Menu", "Add, edit, and remove products",
            WARNING_COLOR, e -> openManageMenuDialog()
        ));
        
        menuGrid.add(createModernMenuCard(
            "⚙️", "Settings", "System configuration and preferences",
            DARK_COLOR, e -> showSettingsDialog()
        ));

        dashboard.add(menuGrid, BorderLayout.CENTER);
        return dashboard;
    }

    /**
     * สร้างส่วนสถิติเพื่อแสดงยอดขายและตัวเลขที่สำคัญอื่นๆ
     */
    private JPanel createStatsPanel() {
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        statsPanel.setOpaque(false);
        statsPanel.setPreferredSize(new Dimension(0, 110));

        List<SalesRecord> todayRecords = orderRepository.findSalesRecordsByDate(LocalDate.now());
        DailySalesReport report = reportService.generateDailyReport(todayRecords, LocalDate.now());

        statsPanel.add(createStatCard("💵", "Today's Sales", formatMoney(report.getTotalSales()), SUCCESS_COLOR));
        statsPanel.add(createStatCard("📝", "Orders", String.valueOf(todayRecords.size()), PRIMARY_COLOR));
        statsPanel.add(createStatCard("📦", "Products", String.valueOf(productCatalog.size()), WARNING_COLOR));
        statsPanel.add(createLiveClockCard("⏰", "Current Time", DARK_COLOR));

        return statsPanel;
    }

    /**
     * สร้างการ์ดสถิติแต่ละอัน เพื่อใช้แสดงข้อมูลแบบตัวเลข
     */
    private JPanel createStatCard(String icon, String title, String value, Color accentColor) {
        JPanel card = createModernCard();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(4, 0, 0, 0, accentColor),
            new EmptyBorder(15, 10, 15, 10)
        ));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Tahoma", Font.PLAIN, 13));
        titleLabel.setForeground(new Color(127, 140, 141));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Tahoma", Font.BOLD, 20));
        valueLabel.setForeground(DARK_COLOR);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(valueLabel);

        card.add(iconLabel, BorderLayout.NORTH);
        card.add(textPanel, BorderLayout.CENTER);

        return card;
    }

    /**
     * สร้างการ์ดแสดงเวลาปัจจุบัน ที่มีการอัปเดตแบบเรียลไทม์
     */
    private JPanel createLiveClockCard(String icon, String title, Color accentColor) {
        JPanel card = createModernCard();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(4, 0, 0, 0, accentColor),
            new EmptyBorder(15, 10, 15, 10)
        ));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Tahoma", Font.PLAIN, 13));
        titleLabel.setForeground(new Color(127, 140, 141));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel valueLabel = new JLabel(getCurrentTimeWithSeconds());
        valueLabel.setFont(new Font("Tahoma", Font.BOLD, 20));
        valueLabel.setForeground(DARK_COLOR);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        javax.swing.Timer timer = new javax.swing.Timer(1000, e -> {
            valueLabel.setText(getCurrentTimeWithSeconds());
        });
        timer.start();

        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(valueLabel);

        card.add(iconLabel, BorderLayout.NORTH);
        card.add(textPanel, BorderLayout.CENTER);

        return card;
    }

    /**
     * สร้างปุ่มเมนูหลักแบบการ์ด ที่ใช้กดเพื่อเข้าไปที่หน้าต่างๆ
     */
    private JPanel createModernMenuCard(String icon, String title, String description, 
                                        Color accentColor, java.awt.event.ActionListener action) {
        JPanel card = createModernCard();
        card.setLayout(new BorderLayout());
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        Color originalBg = card.getBackground();
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(new Color(248, 249, 250));
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(accentColor, 2),
                    new EmptyBorder(28, 28, 28, 28)
                ));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(originalBg);
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(220, 221, 225), 1),
                    new EmptyBorder(29, 29, 29, 29)
                ));
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                action.actionPerformed(null);
            }
        });

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(EMOJI_FONT);
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Tahoma", Font.BOLD, 20));
        titleLabel.setForeground(DARK_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel descLabel = new JLabel("<html><center>"+ description + "</center></html>");
        descLabel.setFont(new Font("Tahoma", Font.PLAIN, 13));
        descLabel.setForeground(new Color(127, 140, 141));
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(iconLabel);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(descLabel);

        card.add(contentPanel, BorderLayout.CENTER);
        return card;
    }

    /**
     * สร้างกรอบการ์ดพื้นฐาน เพื่อใช้สำหรับใส่เนื้อหาอื่นๆ
     */
    private JPanel createModernCard() {
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 221, 225), 1),
            new EmptyBorder(30, 30, 30, 30)
        ));
        return card;
    }

    /**
     * สร้างปุ่มแบบสมัยใหม่ที่มีการเปลี่ยนสีเมื่อนำเมาส์ไปชี้
     */
    private JButton createModernButton(String text, Color bgColor, Color textColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Tahoma", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(textColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(250, 45));
        button.setMaximumSize(new Dimension(250, 45));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.darker());
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
        return button;
    }

    /**
     * จัดการเมื่อผู้ใช้กดปุ่มสมัครสมาชิก (Register) เปิดหน้าต่างลงทะเบียนและดำเนินการเก็บข้อมูล
     */
    private void handleRegister() {
        // เรียกใช้ ModernRegisterDialog
        ModernRegisterDialog dialog = new ModernRegisterDialog(this);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            String[] credentials = dialog.getCredentials();
            boolean success = authService.register(credentials[0], credentials[1]);
            
            if (success) {
                showModernMessage("Success", "Account created successfully! Please login.", SUCCESS_COLOR);
            } else {
                showModernMessage("Error", "Registration failed. Username may already exist.", DANGER_COLOR);
            }
        }
    }

    /**
     * จัดการเมื่อผู้ใช้คลิกปุ่มเข้าสู่ระบบ (Login)
     */
    private void handleLogin() {
        // เรียกใช้ ModernLoginDialog
        ModernLoginDialog dialog = new ModernLoginDialog(this);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            String[] credentials = dialog.getCredentials();
            User user = authService.login(credentials[0], credentials[1]);
            
            if (user != null) {
                currentUser = user;
                currentUserLabel.setText(currentUser.getUsername());
                cardLayout.show(rootPanel, HOME_CARD);
                showModernMessage("Welcome", "Login successful! Welcome to POS Coffee.", SUCCESS_COLOR);
            } else {
                showModernMessage("Error", "Invalid credentials. Please try again.", DANGER_COLOR);
            }
        }
    }

    /**
     * จัดการเมื่อผู้ใช้ต้องการออกจากระบบ (Logout)
     */
    private void handleLogout() {
        int result = JOptionPane.showConfirmDialog(
            this, "Are you sure you want to logout?", "Confirm Logout",
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE
        );
        if (result == JOptionPane.YES_OPTION) {
            if (currentUser != null) {
                currentUser.logout();
            }
            currentUser = null;
            currentUserLabel.setText("Not logged in");
            showAuthCard();
        }
    }

    /**
     * วนกลับมาแสดงหน้าจอสำหรับเข้าสู่ระบบใหม่
     */
    private void showAuthCard() {
        cardLayout.show(rootPanel, AUTH_CARD);
    }

    /**
     * เปิดหน้าต่างสำหรับทำรายการสั่งซื้อ (New Order)
     */
    private void openNewOrderDialog() {
        if (productCatalog.isEmpty()) {
            showModernMessage("Warning", "No products available in catalog.", WARNING_COLOR);
            return;
        }
        new ModernOrderDialog(this, productCatalog, orderRepository).setVisible(true);
        updateDashboardStats(); 
    }

    /**
     * เปิดหน้าต่างสำหรับจัดการเมนูสินค้า (Manage Menu)
     */
    private void openManageMenuDialog() {
        new ModernMenuDialog(this, productRepository, productCatalog).setVisible(true);
        this.productCatalog = new LinkedHashMap<>(productRepository.findAllAsMap());
        updateDashboardStats(); 
    }

    /**
     * เปิดหน้าต่างเพื่อดูรายงานยอดขายประจำวัน (Daily Report)
     */
    private void openDailyReportDialog() {
        new ModernReportDialog(this, orderRepository, reportService).setVisible(true);
    }

    /**
     * แสดงหน้าต่างสำหรับการตั้งค่าระบบ (กำลังพัฒนา)
     */
    private void showSettingsDialog() {
        showModernMessage("Info", "Settings feature coming soon!", PRIMARY_COLOR);
        // new ModernSettingsDialog(this).setVisible(true);
    }

    // private void updateDashboardStats() {
    //     Component[] components = rootPanel.getComponents();
    //     for (Component c : components) {
    //         if (c.isVisible() && !c.getName().equals(AUTH_CARD)) {
    //             rootPanel.add(createModernHomePanel(), HOME_CARD);
    //             cardLayout.show(rootPanel, HOME_CARD);
    //             break;
    //         }
    //     }
    // }
    /**
     * อัปเดตข้อมูลบนแผงควบคุมหลัก (Dashboard) ให้ดึงข้อมูลสถิติใหม่มารีเฟรช
     */
    private void updateDashboardStats() {
        // วาดหน้าต่าง Dashboard ใหม่และให้แสดงผลทันที เพื่ออัปเดตตัวเลขแบบ Real-time
        rootPanel.add(createModernHomePanel(), HOME_CARD);
        cardLayout.show(rootPanel, HOME_CARD);
    }

    /**
     * แสดงกล่องข้อความโต้ตอบที่มีการออกแบบตามธีม (Modern Message Dialog)
     */
    private void showModernMessage(String title, String message, Color accentColor) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setSize(400, 200);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

        JLabel messageLabel = new JLabel("<html><center>"+ message + "</center></html>");
        messageLabel.setFont(new Font("Tahoma", Font.PLAIN, 14));
        messageLabel.setForeground(DARK_COLOR);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JButton okButton = createModernButton("OK", accentColor, Color.WHITE);
        okButton.setPreferredSize(new Dimension(100, 35));
        okButton.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setOpaque(false);
        buttonPanel.add(okButton);

        contentPanel.add(messageLabel, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        // แก้บั๊ก FlatLaf: เอาลง getContentPane() โดยตรง
        dialog.getContentPane().add(contentPanel);
        dialog.setLocationRelativeTo(this); // จัดให้อยู่ตรงกลางหลังจาก add ของเสร็จ
        dialog.setVisible(true);
    }

    /**
     * ดึงข้อมูลเวลาปัจจุบันพร้อมแสดงวินาที สำหรับนาฬิกาบนหน้าจอหลัก
     */
    private String getCurrentTimeWithSeconds() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    /**
     * ฟอร์แมตจำนวนเงินให้อยู่ในรูปสกุลเงินบาท
     */
    private String formatMoney(double amount) {
        return String.format("฿%.2f", amount);
    }

    /**
     * เมธอดสำหรับเริ่มการทำงานของหน้าต่าง UI ให้อยู่บน Event Dispatch Thread (EDT)
     */
    public static void launchOnEdt(AuthService authService,
                                   ProductRepository productRepository,
                                   OrderRepository orderRepository,
                                   ReportService reportService) {
        SwingUtilities.invokeLater(() -> {
            try {
                FlatIntelliJLaf.setup();
                UIManager.put("defaultFont", new Font("Tahoma", Font.PLAIN, 14));
                UIManager.put("Button.arc", 8);
                UIManager.put("Component.arc", 8);
                UIManager.put("ProgressBar.arc", 8);
                UIManager.put("TextComponent.arc", 8);
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            POSAppFrame frame = new POSAppFrame(
                authService, productRepository, orderRepository,
                reportService
            );
            frame.setVisible(true);
        });
    }
}