package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import model.Product;
import model.ProductCategory;
import repository.OrderRepository;

public class ModernOrderDialog extends JDialog {

    private final Map<String, Product> productCatalog;
    private final OrderRepository orderRepository;

    // ระบบตะกร้าสินค้า
    private final Map<Product, Integer> cart = new LinkedHashMap<>();
    private double grandTotal = 0.0;

    // UI Components
    private DefaultTableModel cartTableModel;
    private JTable cartTable;
    private JLabel totalLabel;
    private JTextField cashField;
    private JLabel changeLabel;

    /**
     * คอนสตรักเตอร์สำหรับสร้างหน้าต่างการสั่งซื้อ (Point of Sale)
     */
    public ModernOrderDialog(Frame parent,
                             Map<String, Product> productCatalog,
                             OrderRepository orderRepository) {

        super(parent, "New Order (Point of Sale)", true);
        this.productCatalog = productCatalog;
        this.orderRepository = orderRepository;

        setLayout(new BorderLayout());
        
        add(createHeader(), BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(245, 246, 250));

        mainPanel.add(createCategoryTabs(), BorderLayout.CENTER); // ฝั่งซ้าย: แท็บหมวดหมู่
        mainPanel.add(createCartPanel(), BorderLayout.EAST);      // ฝั่งขวา: ตะกร้าสินค้า

        add(mainPanel, BorderLayout.CENTER);

        setSize(1100, 750); // ปรับขนาดให้กว้างขึ้นนิดหน่อย
        setLocationRelativeTo(parent);
    }

    /**
     * สร้างส่วนหัวของหน้าต่างออเดอร์
     */
    private JPanel createHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(52, 152, 219));
        p.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel l = new JLabel("🛒 New Order");
        l.setForeground(Color.WHITE);
        l.setFont(new Font("Dialog", Font.BOLD, 24));
        p.add(l, BorderLayout.WEST);

        return p;
    }

    // --- ส่วนฝั่งซ้าย: สร้างแท็บหมวดหมู่สินค้า ---
    /**
     * สร้างส่วนแท็บหมวดหมู่สินค้าทางด้านซ้ายของหน้าจอ
     */
    private JPanel createCategoryTabs() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Dialog", Font.BOLD, 16));

        // จัดกลุ่มสินค้าตาม Category
        Map<ProductCategory, List<Product>> groupedProducts = new EnumMap<>(ProductCategory.class);
        for (Product p : productCatalog.values()) {
            groupedProducts.computeIfAbsent(p.getCategory(), k -> new ArrayList<>()).add(p);
        }

        // สร้างหน้าย่อยสำหรับแต่ละ Category
        for (Map.Entry<ProductCategory, List<Product>> entry : groupedProducts.entrySet()) {
            ProductCategory category = entry.getKey();
            List<Product> products = entry.getValue();

            // ใช้ JPanel ซ้อนกันเพื่อไม่ให้ปุ่มยืดจนน่าเกลียดเมื่อมีสินค้าน้อย
            JPanel gridPanel = new JPanel(new GridLayout(0, 3, 15, 15));
            gridPanel.setOpaque(false);

            for (Product p : products) {
                JButton btn = new JButton();
                btn.setText("<html><center><b style='font-size:14px;'>" + p.getName() + "</b><br><span style='color:#7f8c8d;'>฿" + p.getPrice() + "</span></center></html>");
                btn.setBackground(Color.WHITE);
                btn.setFocusPainted(false);
                btn.setBorder(BorderFactory.createLineBorder(new Color(220, 221, 225), 2));
                btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                btn.setPreferredSize(new Dimension(120, 100));

                btn.addActionListener(e -> addProductToCart(p));
                gridPanel.add(btn);
            }

            JPanel alignTopPanel = new JPanel(new BorderLayout());
            alignTopPanel.setOpaque(false);
            alignTopPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
            alignTopPanel.add(gridPanel, BorderLayout.NORTH); // ดันปุ่มให้ชิดบน

            JScrollPane scrollPane = new JScrollPane(alignTopPanel);
            scrollPane.setBorder(null);
            scrollPane.getViewport().setOpaque(false);
            scrollPane.setOpaque(false);

            tabbedPane.addTab(" " + category.getDisplayName() + " ", scrollPane);
        }

        panel.add(tabbedPane, BorderLayout.CENTER);
        return panel;
    }

    // --- ส่วนฝั่งขวา: ตะกร้าสินค้าและระบบจัดการ ---
    /**
     * สร้างส่วนตารางตะกร้าสินค้า การจัดการรายการ และการคิดเงินทางด้านขวาของหน้าจอ
     */
    private JPanel createCartPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setPreferredSize(new Dimension(420, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 221, 225), 1),
            new EmptyBorder(15, 15, 15, 15)
        ));

        // 1. ตารางตะกร้าสินค้า
        String[] columns = {"Item", "Qty", "Price", "Total"};
        cartTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        cartTable = new JTable(cartTableModel);
        cartTable.setRowHeight(35);
        cartTable.setFont(new Font("Dialog", Font.PLAIN, 14));
        cartTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // เลือกได้ทีละบรรทัด
        
        cartTable.getColumnModel().getColumn(0).setPreferredWidth(160);
        cartTable.getColumnModel().getColumn(1).setPreferredWidth(40);
        
        JScrollPane scrollPane = new JScrollPane(cartTable);

        // 2. แถบปุ่มจัดการตะกร้า (+, -, ลบ)
        JPanel cartControlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        cartControlPanel.setOpaque(false);
        
        JButton btnMinus = createCartControlButton("  -  ", new Color(243, 156, 18));
        JButton btnPlus = createCartControlButton("  +  ", new Color(46, 204, 113));
        JButton btnRemove = createCartControlButton(" Remove ", new Color(231, 76, 60));

        btnMinus.addActionListener(e -> adjustSelectedQuantity(-1));
        btnPlus.addActionListener(e -> adjustSelectedQuantity(1));
        btnRemove.addActionListener(e -> removeSelectedProduct());

        cartControlPanel.add(btnMinus);
        cartControlPanel.add(btnPlus);
        cartControlPanel.add(btnRemove);

        // รวมตารางและปุ่มจัดการเข้าด้วยกัน
        JPanel tableWithControls = new JPanel(new BorderLayout(0, 5));
        tableWithControls.setOpaque(false);
        tableWithControls.add(scrollPane, BorderLayout.CENTER);
        tableWithControls.add(cartControlPanel, BorderLayout.SOUTH);
        
        // 3. ส่วนคิดเงิน (Checkout)
        JPanel checkoutPanel = new JPanel(new GridBagLayout());
        checkoutPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.5;
        JLabel totalTextLabel = new JLabel("Grand Total:");
        totalTextLabel.setFont(new Font("Dialog", Font.BOLD, 18));
        checkoutPanel.add(totalTextLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 0.5;
        totalLabel = new JLabel("฿0.00", SwingConstants.RIGHT);
        totalLabel.setFont(new Font("Dialog", Font.BOLD, 22));
        totalLabel.setForeground(new Color(231, 76, 60)); 
        checkoutPanel.add(totalLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        JLabel cashTextLabel = new JLabel("Cash Given (฿):");
        cashTextLabel.setFont(new Font("Dialog", Font.PLAIN, 16));
        checkoutPanel.add(cashTextLabel, gbc);

        gbc.gridx = 1;
        cashField = new JTextField();
        cashField.setFont(new Font("Dialog", Font.BOLD, 18));
        cashField.setHorizontalAlignment(JTextField.RIGHT);
        checkoutPanel.add(cashField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        JLabel changeTextLabel = new JLabel("Change:");
        changeTextLabel.setFont(new Font("Dialog", Font.BOLD, 18));
        checkoutPanel.add(changeTextLabel, gbc);

        gbc.gridx = 1;
        changeLabel = new JLabel("฿0.00", SwingConstants.RIGHT);
        changeLabel.setFont(new Font("Dialog", Font.BOLD, 22));
        changeLabel.setForeground(new Color(46, 204, 113)); 
        checkoutPanel.add(changeLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.insets = new Insets(20, 5, 5, 5);
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.setOpaque(false);

        JButton clearBtn = new JButton("Clear Cart");
        clearBtn.setBackground(new Color(149, 165, 166));
        clearBtn.setForeground(Color.WHITE);
        clearBtn.setFont(new Font("Dialog", Font.BOLD, 14));
        clearBtn.addActionListener(e -> clearCart());

        JButton payBtn = new JButton("Pay & Complete");
        payBtn.setBackground(new Color(46, 204, 113));
        payBtn.setForeground(Color.WHITE);
        payBtn.setFont(new Font("Dialog", Font.BOLD, 14));
        payBtn.setPreferredSize(new Dimension(0, 45));
        payBtn.addActionListener(e -> processPayment());

        buttonPanel.add(clearBtn);
        buttonPanel.add(payBtn);
        checkoutPanel.add(buttonPanel, gbc);

        panel.add(new JLabel("<html><b style='font-size:16px;'>Current Order</b></html>"), BorderLayout.NORTH);
        panel.add(tableWithControls, BorderLayout.CENTER);
        panel.add(checkoutPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * สร้างปุ่มควบคุมตะกร้าแบบสั้น (เช่นปุ่ม + , -, Remove)
     */
    private JButton createCartControlButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Dialog", Font.BOLD, 13));
        btn.setMargin(new Insets(2, 8, 2, 8));
        btn.setFocusPainted(false);
        return btn;
    }

    // --- Logic จัดการตะกร้า ---

    /**
     * ค้นหาว่า ณ แถวที่เลือกอยู่ในตารางตะกร้าตรงกับสินค้าหมวดหมู่ใด
     */
    private Product getProductFromSelectedRow() {
        int selectedRow = cartTable.getSelectedRow();
        if (selectedRow == -1) return null;
        // อาศัยลำดับของ Map ที่เชื่อมโยงกับแถวของ Table
        List<Product> products = new ArrayList<>(cart.keySet());
        if (selectedRow < products.size()) {
            return products.get(selectedRow);
        }
        return null;
    }

    /**
     * ปรับจำนวนสินค้าที่เลือก (เพิ่มหรือลด) อย่างปลอดภัย
     */
    private void adjustSelectedQuantity(int amount) {
        Product p = getProductFromSelectedRow();
        if (p == null) {
            JOptionPane.showMessageDialog(this, "Please select an item from the cart first.", "No Item Selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int currentQty = cart.getOrDefault(p, 0);
        int newQty = currentQty + amount;

        if (newQty <= 0) {
            cart.remove(p);
        } else {
            cart.put(p, newQty);
        }
        
        int selectedRow = cartTable.getSelectedRow(); // จำแถวที่เลือกไว้
        updateCartUI();
        
        // ไฮไลต์แถวเดิมที่เลือกอยู่ (ถ้ายังมีข้อมูลอยู่)
        if (selectedRow < cartTable.getRowCount() && selectedRow >= 0) {
            cartTable.setRowSelectionInterval(selectedRow, selectedRow);
        }
    }

    /**
     * ลบรายการสินค้าที่เลือกออกจากตะกร้าไปทั้งหมด
     */
    private void removeSelectedProduct() {
        Product p = getProductFromSelectedRow();
        if (p == null) {
            JOptionPane.showMessageDialog(this, "Please select an item from the cart to remove.", "No Item Selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        cart.remove(p);
        updateCartUI();
    }

    /**
     * เพิ่มสินค้าที่เลือกลงในตะกร้า หากมีอยู่แล้วจะเพิ่มจำนวน
     */
    private void addProductToCart(Product p) {
        cart.put(p, cart.getOrDefault(p, 0) + 1);
        updateCartUI();
        
        // เลื่อนไฮไลต์ตารางไปที่รายการล่าสุดที่เพิ่งกดแอด
        int rowIndex = new ArrayList<>(cart.keySet()).indexOf(p);
        if (rowIndex >= 0) {
            cartTable.setRowSelectionInterval(rowIndex, rowIndex);
        }
    }

    /**
     * ล้างข้อมูลทั้งหมดในตะกร้า เริ่มต้นการขายรอบใหม่
     */
    private void clearCart() {
        cart.clear();
        cashField.setText("");
        updateCartUI();
    }

    /**
     * อัปเดตตารางสรุปผลข้อมูลฝั่งขวา เพื่อให้เห็นราคาแบบจับคู่ทันทีที่เปลี่ยนข้อมูลตะกร้า
     */
    private void updateCartUI() {
        cartTableModel.setRowCount(0); 
        grandTotal = 0;

        for (Map.Entry<Product, Integer> entry : cart.entrySet()) {
            Product p = entry.getKey();
            int qty = entry.getValue();
            double lineTotal = p.getPrice() * qty;
            grandTotal += lineTotal;

            cartTableModel.addRow(new Object[]{
                p.getName(),
                qty,
                p.getPrice(),
                lineTotal
            });
        }

        totalLabel.setText(String.format("฿%.2f", grandTotal));
        changeLabel.setText("฿0.00"); 
    }

    // --- ระบบคิดเงินและบันทึกลง Database ---
    /**
     * ดำเนินการชำระเงิน ตรวจสอบยอดเงินคงเหลือ และบันทึกประวัติการขาย
     */
    private void processPayment() {
        if (cart.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cart is empty!", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String cashStr = cashField.getText().trim();
            if (cashStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter cash amount.", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            double cash = Double.parseDouble(cashStr);

            if (cash < grandTotal) {
                JOptionPane.showMessageDialog(this, "Insufficient cash! Need ฿" + (grandTotal - cash) + " more.", "Payment Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 1. สร้าง Order
            String orderId = "ORD-" + System.currentTimeMillis(); 
            model.Order newOrder = new model.Order(orderId);

            for (Map.Entry<model.Product, Integer> entry : cart.entrySet()) {
                newOrder.addProduct(entry.getKey(), entry.getValue());
            }

            double subtotal = newOrder.calculateSubtotal();
            double vatAmount = newOrder.calculateVat(0.0); 
            double finalTotal = newOrder.calculateTotal(false, 0.0);
            
            double change = cash - finalTotal;
            changeLabel.setText(String.format("฿%.2f", change));

            // 2. สร้าง SalesRecord
            model.SalesRecord record = new model.SalesRecord(
                newOrder,
                subtotal,
                vatAmount,
                finalTotal,
                cash,
                change,
                java.time.LocalDateTime.now()
            );

            // 3. บันทึกลงฐานข้อมูล
            orderRepository.saveSalesRecord(record); 

            String receipt = String.format("Payment Successful!\nOrder: %s\n\nTotal: ฿%.2f\nCash: ฿%.2f\nChange: ฿%.2f", 
                                            orderId, finalTotal, cash, change);
            JOptionPane.showMessageDialog(this, receipt, "Success", JOptionPane.INFORMATION_MESSAGE);

            clearCart();
            dispose();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid number format for cash.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}