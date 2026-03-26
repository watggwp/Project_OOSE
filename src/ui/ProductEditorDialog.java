package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import model.ProductCategory;

public class ProductEditorDialog extends JDialog {

    private JTextField codeField = new JTextField();
    private JTextField nameField = new JTextField();
    private JTextField priceField = new JTextField();
    private JComboBox<ProductCategory> categoryCombo; 
    private boolean confirmed = false;

    /**
     * คอนสตรักเตอร์สำหรับสร้างหน้าต่างเพิ่มหรือแก้ไขข้อมูลสินค้า
     */
    public ProductEditorDialog(Frame parent) {
        super(parent, "Add New Product", true);

        categoryCombo = new JComboBox<>(ProductCategory.values());

        setLayout(new BorderLayout());

        add(createHeader(), BorderLayout.NORTH);
        add(createForm(), BorderLayout.CENTER);
        add(createButtons(), BorderLayout.SOUTH);

        pack();
        setMinimumSize(new Dimension(450, 450));
        setLocationRelativeTo(parent);
    }

    /**
     * สร้างส่วนหัว (Header) ของหน้าต่างเพิ่ม/แก้ไขสินค้า
     */
    private JPanel createHeader() {
        JPanel p = new JPanel();
        p.setBackground(new Color(46, 204, 113));
        p.setBorder(new EmptyBorder(18, 10, 18, 10));

        JLabel l = new JLabel("Add New Product");
        l.setForeground(Color.WHITE);
        l.setFont(new Font("Dialog", Font.BOLD, 20));
        p.add(l);

        return p;
    }

    /**
     * สร้างฟอร์มสำหรับกรอกข้อมูลสินค้า (รหัส, ชื่อ, ราคา, หมวดหมู่)
     */
    private JPanel createForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(25, 40, 25, 40));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        g.insets = new Insets(8, 0, 8, 0);

        g.gridy = 0;
        panel.add(new JLabel("Product Code *"));
        g.gridy = 1;
        codeField.setPreferredSize(new Dimension(300, 38));
        panel.add(codeField, g);

        g.gridy = 2;
        panel.add(new JLabel("Product Name *"));
        g.gridy = 3;
        nameField.setPreferredSize(new Dimension(300, 38));
        panel.add(nameField, g);

        g.gridy = 4;
        panel.add(new JLabel("Price (฿) *"));
        g.gridy = 5;
        priceField.setPreferredSize(new Dimension(300, 38));
        panel.add(priceField, g);

        g.gridy = 6;
        panel.add(new JLabel("Category *"));
        g.gridy = 7;
        categoryCombo.setPreferredSize(new Dimension(300, 38));
        categoryCombo.setBackground(Color.WHITE);
        panel.add(categoryCombo, g);

        return panel;
    }

    /**
     * สร้างส่วนปุ่มกดด้านล่าง (Cancel, Add Product)
     */
    private JPanel createButtons() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        p.setBackground(Color.WHITE);

        JButton cancel = new JButton("Cancel");
        JButton add = new JButton("Add Product");

        cancel.setPreferredSize(new Dimension(110, 38));
        add.setPreferredSize(new Dimension(120, 38));

        add.setBackground(new Color(46, 204, 113));
        add.setForeground(Color.WHITE);

        cancel.addActionListener(e -> dispose());
        add.addActionListener(e -> {
            confirmed = true;
            dispose();
        });

        p.add(cancel);
        p.add(add);

        return p;
    }

    /**
     * ตรวจสอบว่าผู้ใช้ได้ยืนยันการเพิ่มหรือแก้ไขสินค้าหรือไม่
     */
    public boolean isConfirmed() { return confirmed; }

    public String getProductCode() { return codeField.getText().trim(); }
    public String getProductName() { return nameField.getText().trim(); }
    public double getProductPrice() {
        try { return Double.parseDouble(priceField.getText().trim()); } 
        catch (NumberFormatException e) { return 0.0; }
    }
    public ProductCategory getProductCategory() {
        return (ProductCategory) categoryCombo.getSelectedItem();
    }
}