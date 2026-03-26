package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import model.SalesRecord;
import repository.OrderRepository;
import service.ReportService;

public class ModernReportDialog extends JDialog {

    private final OrderRepository orderRepository;

    // UI Components
    private JComboBox<String> timeRangeCombo;
    private JSpinner startDateSpinner;
    private JSpinner endDateSpinner;

    // Summary Labels
    private JLabel totalSalesLabel;
    private JLabel totalOrdersLabel;
    private JLabel avgOrderLabel;

    // Chart Panel
    private CustomChartPanel chartPanel;

    /**
     * คอนสตรักเตอร์สำหรับสร้างหน้าต่างรายงานยอดขายและสถิติ
     */
    public ModernReportDialog(Frame parent, OrderRepository orderRepository, ReportService reportService) {
        super(parent, "Sales Analytics & Reports", true);
        this.orderRepository = orderRepository;

        setLayout(new BorderLayout());

        add(createHeader(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);

        setSize(1050, 750); // ปรับหน้าต่างให้กว้างขึ้นนิดหน่อยให้กราฟ 24 ชม. ดูสบายตา
        setLocationRelativeTo(parent);

        // โหลดข้อมูลเริ่มต้น (วันนี้)
        generateReport();
    }

    /**
     * สร้างส่วนหัว (Header) ของหน้าต่างรายงาน
     */
    private JPanel createHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(155, 89, 182));
        p.setBorder(new EmptyBorder(18, 20, 18, 20));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setOpaque(false);
        
        JLabel icon = new JLabel("📈");
        icon.setForeground(Color.WHITE);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        
        JLabel l = new JLabel("Sales Analytics & Reports");
        l.setForeground(Color.WHITE);
        l.setFont(new Font("Tahoma", Font.BOLD, 24));
        
        titlePanel.add(icon);
        titlePanel.add(l);
        p.add(titlePanel, BorderLayout.WEST);

        return p;
    }

    /**
     * สร้างส่วนเนื้อหาหลักที่ประกอบด้วยตัวกรองวันที่ ตัวเลขสรุปผล และกราฟ
     */
    private JPanel createMainContent() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(new Color(245, 246, 250));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        mainPanel.add(createFilterPanel(), BorderLayout.NORTH);

        JPanel dashboardPanel = new JPanel(new BorderLayout(15, 15));
        dashboardPanel.setOpaque(false);

        dashboardPanel.add(createSummaryPanel(), BorderLayout.NORTH);

        chartPanel = new CustomChartPanel();
        dashboardPanel.add(chartPanel, BorderLayout.CENTER);

        mainPanel.add(dashboardPanel, BorderLayout.CENTER);

        return mainPanel;
    }

    /**
     * สร้างส่วนสำหรับเลือกช่วงเวลาของรายงาน (วันที่เริ่มต้น - สิ้นสุด)
     */
    private JPanel createFilterPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 221, 225)),
                new EmptyBorder(5, 10, 5, 10)));

        String[] ranges = { "Today", "This Week", "This Month", "This Year", "Custom Range" };
        timeRangeCombo = new JComboBox<>(ranges);
        timeRangeCombo.setFont(new Font("Tahoma", Font.PLAIN, 14));

        // --- แก้ไข: ปลดล็อคให้ช่องวันที่เลือกได้ตลอดเวลา ---
        startDateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor startEditor = new JSpinner.DateEditor(startDateSpinner, "dd/MM/yyyy");
        startDateSpinner.setEditor(startEditor);
        startDateSpinner.setFont(new Font("Tahoma", Font.PLAIN, 14));

        endDateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor endEditor = new JSpinner.DateEditor(endDateSpinner, "dd/MM/yyyy");
        endDateSpinner.setEditor(endEditor);
        endDateSpinner.setFont(new Font("Tahoma", Font.PLAIN, 14));

        timeRangeCombo.addActionListener(e -> updateDateSpinners());

        JButton genBtn = new JButton("Generate Report");
        genBtn.setBackground(new Color(52, 152, 219));
        genBtn.setForeground(Color.WHITE);
        genBtn.setFont(new Font("Tahoma", Font.BOLD, 14));
        genBtn.addActionListener(e -> generateReport());

        p.add(new JLabel("Report Period:"));
        p.add(timeRangeCombo);
        p.add(new JLabel("  From:"));
        p.add(startDateSpinner);
        p.add(new JLabel("To:"));
        p.add(endDateSpinner);
        p.add(Box.createHorizontalStrut(10));
        p.add(genBtn);

        return p;
    }

    /**
     * สร้างแผงแสดงตัวเลขสรุปผลการขายรวม (ยอดขายรวม, จำนวนออเดอร์, ยอดเฉลี่ยต่อบิล)
     */
    private JPanel createSummaryPanel() {
        JPanel p = new JPanel(new GridLayout(1, 3, 15, 0));
        p.setOpaque(false);

        totalSalesLabel = new JLabel("฿0.00", SwingConstants.CENTER);
        totalOrdersLabel = new JLabel("0", SwingConstants.CENTER);
        avgOrderLabel = new JLabel("฿0.00", SwingConstants.CENTER);

        p.add(createSummaryCard("Total Sales", totalSalesLabel, new Color(46, 204, 113)));
        p.add(createSummaryCard("Total Orders", totalOrdersLabel, new Color(52, 152, 219)));
        p.add(createSummaryCard("Average per Order", avgOrderLabel, new Color(241, 196, 15)));

        return p;
    }

    /**
     * สร้างการ์ดย่อยสำหรับแสดงข้อมูลตัวเลขสถิติแต่ละรายการ
     */
    private JPanel createSummaryCard(String title, JLabel valueLabel, Color accent) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(4, 0, 0, 0, accent),
                new EmptyBorder(15, 10, 15, 10)));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Tahoma", Font.PLAIN, 14));
        titleLabel.setForeground(Color.GRAY);

        valueLabel.setFont(new Font("Tahoma", Font.BOLD, 28));
        valueLabel.setForeground(new Color(44, 62, 80));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    /**
     * อัปเดตช่องเลือกวันที่เริ่มต้นและสิ้นสุดอัตโนมัติตามช่วงเวลาที่ผู้ใช้เลือกจาก
     * Dropdown
     */
    private void updateDateSpinners() {
        String selection = (String) timeRangeCombo.getSelectedItem();
        LocalDate now = LocalDate.now();
        LocalDate start = now;
        LocalDate end = now;

        if ("Custom Range".equals(selection)) {
            return; // ปล่อยให้ผู้ใช้เลือกวันที่เอง
        }

        switch (selection) {
            case "Today":
                break;
            case "This Week":
                start = now.minusDays(now.getDayOfWeek().getValue() - 1);
                break;
            case "This Month":
                start = now.withDayOfMonth(1);
                break;
            case "This Year":
                start = now.withDayOfYear(1);
                break;
        }

        startDateSpinner.setValue(java.sql.Date.valueOf(start));
        endDateSpinner.setValue(java.sql.Date.valueOf(end));
    }

    /**
     * ดึงข้อมูลจากฐานข้อมูลและสร้างรายงานตามช่วงเวลาที่กำหนด
     * อัปเดตข้อมูลบนหน้าจอและกราฟ
     */
    private void generateReport() {
        LocalDate start = ((java.util.Date) startDateSpinner.getValue()).toInstant().atZone(ZoneId.systemDefault())
                .toLocalDate();
        LocalDate end = ((java.util.Date) endDateSpinner.getValue()).toInstant().atZone(ZoneId.systemDefault())
                .toLocalDate();

        if (start.isAfter(end)) {
            JOptionPane.showMessageDialog(this, "Start date cannot be after end date.", "Invalid Date",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<SalesRecord> records = orderRepository.findSalesRecordsByDateRange(start, end);

        double totalSales = 0;
        int totalOrders = records.size();

        Map<String, Double> salesMap = new LinkedHashMap<>();
        Map<String, Integer> orderMap = new LinkedHashMap<>();

        boolean isSingleDay = start.equals(end);

        // --- แก้ไข: ถ้ารายวัน ให้กราฟโชว์ครบ 24 ชั่วโมง (00:00 - 23:00) ---
        if (isSingleDay) {
            for (int i = 0; i <= 23; i++) {
                String hour = String.format("%02d:00", i);
                salesMap.put(hour, 0.0);
                orderMap.put(hour, 0);
            }
        }

        for (SalesRecord r : records) {
            totalSales += r.getTotalAmount();

            String key;
            if (isSingleDay) {
                int hour = r.getPaidAt().getHour();
                key = String.format("%02d:00", hour);
            } else {
                key = r.getPaidAt().toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM"));
                salesMap.putIfAbsent(key, 0.0);
                orderMap.putIfAbsent(key, 0);
            }

            if (salesMap.containsKey(key)) {
                salesMap.put(key, salesMap.get(key) + r.getTotalAmount());
                orderMap.put(key, orderMap.get(key) + 1);
            }
        }

        totalSalesLabel.setText(String.format("฿%,.2f", totalSales));
        totalOrdersLabel.setText(String.valueOf(totalOrders));
        avgOrderLabel.setText(totalOrders > 0 ? String.format("฿%,.2f", totalSales / totalOrders) : "฿0.00");

        chartPanel.updateData(salesMap, orderMap, isSingleDay ? "Time (24 Hours)" : "Date (Day/Month)");
    }

    class CustomChartPanel extends JPanel {
        private Map<String, Double> salesData = new LinkedHashMap<>();
        private Map<String, Integer> orderData = new LinkedHashMap<>();
        private String xAxisLabel = "";

        public CustomChartPanel() {
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(new Color(220, 221, 225)));
        }

        public void updateData(Map<String, Double> sales, Map<String, Integer> orders, String xLabel) {
            this.salesData = sales;
            this.orderData = orders;
            this.xAxisLabel = xLabel;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (salesData.isEmpty())
                return;

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int pad = 60;
            int width = getWidth() - (2 * pad);
            int height = getHeight() - (2 * pad);

            double maxSales = salesData.values().stream().mapToDouble(v -> v).max().orElse(100);
            int maxOrders = orderData.values().stream().mapToInt(v -> v).max().orElse(10);

            if (maxSales == 0)
                maxSales = 100;
            if (maxOrders == 0)
                maxOrders = 10;

            maxSales *= 1.2;
            maxOrders = (int) (maxOrders * 1.5);

            g2.setColor(new Color(236, 240, 241));
            for (int i = 0; i <= 5; i++) {
                int y = getHeight() - pad - (i * height / 5);
                g2.drawLine(pad, y, getWidth() - pad, y);

                g2.setColor(new Color(52, 152, 219));
                String saleTxt = String.format("฿%,.0f", (maxSales * i / 5));
                g2.drawString(saleTxt, 5, y + 5);

                g2.setColor(new Color(230, 126, 34));
                String ordTxt = String.valueOf(maxOrders * i / 5);
                g2.drawString(ordTxt, getWidth() - pad + 10, y + 5);

                g2.setColor(new Color(236, 240, 241));
            }

            g2.setColor(Color.DARK_GRAY);
            g2.drawLine(pad, getHeight() - pad, pad, pad);
            g2.drawLine(getWidth() - pad, getHeight() - pad, getWidth() - pad, pad);
            g2.drawLine(pad, getHeight() - pad, getWidth() - pad, getHeight() - pad);

            List<String> keys = new ArrayList<>(salesData.keySet());
            int xStep = keys.size() > 1 ? width / (keys.size() - 1) : width / 2;

            g2.setStroke(new BasicStroke(3f));
            g2.setColor(new Color(52, 152, 219));
            int prevX = -1, prevYSales = -1;

            for (int i = 0; i < keys.size(); i++) {
                String key = keys.get(i);
                int x = pad + (i * xStep);
                int ySales = getHeight() - pad - (int) ((salesData.get(key) / maxSales) * height);

                if (prevX != -1)
                    g2.drawLine(prevX, prevYSales, x, ySales);
                g2.fillOval(x - 4, ySales - 4, 8, 8);

                prevX = x;
                prevYSales = ySales;
            }

            prevX = -1;
            int prevYOrders = -1;
            g2.setColor(new Color(230, 126, 34));

            for (int i = 0; i < keys.size(); i++) {
                String key = keys.get(i);
                int x = pad + (i * xStep);
                int yOrders = getHeight() - pad - (int) ((double) orderData.get(key) / maxOrders * height);

                if (prevX != -1)
                    g2.drawLine(prevX, prevYOrders, x, yOrders);
                g2.fillOval(x - 4, yOrders - 4, 8, 8);

                prevX = x;
                prevYOrders = yOrders;

                // วาดข้อความแกน X (ถ้าชั่วโมงเยอะ จะวาดแนวสลับเพื่อไม่ให้ตัวหนังสือเบียดกัน)
                g2.setColor(Color.DARK_GRAY);
                g2.setFont(new Font("Tahoma", Font.PLAIN, 10));

                // เช็คว่าถ้าเป็น 24 ชั่วโมง ให้วาดตัวหนังสือสลับขึ้นลง (Z-zag)
                // เพื่อไม่ให้ทับกัน
                int textY = getHeight() - pad + 20 + (keys.size() > 15 && i % 2 != 0 ? 12 : 0);
                g2.drawString(key, x - 12, textY);

                g2.setColor(new Color(230, 126, 34));
            }

            g2.setFont(new Font("Tahoma", Font.BOLD, 14));

            g2.setColor(new Color(52, 152, 219));
            g2.fillOval(pad + 10, 20, 10, 10);
            g2.drawString("Sales Amount (฿)", pad + 25, 30);

            g2.setColor(new Color(230, 126, 34));
            g2.fillOval(pad + 180, 20, 10, 10);
            g2.drawString("Order Quantity", pad + 195, 30);

            g2.setColor(Color.GRAY);
            g2.drawString(xAxisLabel, getWidth() / 2 - 30, getHeight() - 10);
        }
    }
}