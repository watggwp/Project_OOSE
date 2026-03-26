package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;
import model.Product;
import model.ProductCategory;
import repository.ProductRepository;

public class ModernMenuDialog extends JDialog {

    private ProductRepository productRepository;
    private Map<String, Product> catalog;
    private DefaultTableModel tableModel;

    /**
     * คอนสตรักเตอร์สำหรับสร้างหน้าต่างจัดการเมนูสินค้า
     */
    public ModernMenuDialog(Frame parent,
                            ProductRepository productRepository,
                            Map<String, Product> catalog) {

        super(parent, "Menu Management", true);
        
        this.productRepository = productRepository;
        this.catalog = catalog;

        setLayout(new BorderLayout());

        add(header(), BorderLayout.NORTH);
        add(center(), BorderLayout.CENTER);
        add(footer(), BorderLayout.SOUTH);

        setSize(750, 500);
        setLocationRelativeTo(parent);
    }

    /**
     * สร้างส่วนหัว (Header) ของหน้าต่างจัดการเมนู
     */
    private JPanel header() {
        JPanel p = new JPanel();
        p.setBackground(new Color(241, 196, 15));
        p.setBorder(new EmptyBorder(18, 10, 18, 10));

        JLabel l = new JLabel("Menu Management");
        l.setFont(new Font("Dialog", Font.BOLD, 20));
        l.setForeground(Color.WHITE);
        p.add(l);

        return p;
    }

    /**
     * สร้างส่วนแสดงผลตารางรายการสินค้าตรงกลางหน้าต่าง
     */
    private JPanel center() {
        String[] col = {"Code", "Name", "Category", "Price"};

        tableModel = new DefaultTableModel(col, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (Product p : catalog.values()) {
            tableModel.addRow(new Object[]{
                p.getCode(), 
                p.getName(), 
                p.getCategory(), 
                p.getPrice()
            });
        }

        JTable table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setFont(new Font("Dialog", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Dialog", Font.BOLD, 14));

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.add(new JScrollPane(table));

        return panel;
    }

    /**
     * สร้างส่วนปุ่มควบคุมด้านล่าง (เช่น ปุ่มเพิ่มสินค้า, ปิดหน้าต่าง)
     */
    private JPanel footer() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));

        JButton addBtn = new JButton("Add Product");
        addBtn.setBackground(new Color(46, 204, 113)); 
        addBtn.setForeground(Color.WHITE);
        addBtn.setPreferredSize(new Dimension(120, 38));

        JButton close = new JButton("Close");
        close.setPreferredSize(new Dimension(100, 38));

        addBtn.addActionListener(e -> {
            ProductEditorDialog dialog = new ProductEditorDialog((Frame) getParent());
            dialog.setVisible(true);

            if (dialog.isConfirmed()) {
                String code = dialog.getProductCode();
                String name = dialog.getProductName();
                double price = dialog.getProductPrice();
                ProductCategory category = dialog.getProductCategory();

                if (code.isEmpty() || name.isEmpty()) {
                    JOptionPane.showMessageDialog(this, 
                        "Please fill in all required fields.", 
                        "Input Error", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Product newProduct = new Product(code, name, price, category);
                productRepository.save(newProduct);
                catalog.put(code, newProduct);

                tableModel.addRow(new Object[]{code, name, category, price});

                JOptionPane.showMessageDialog(this, 
                    "Added: " + name + " (฿" + price + ")", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
        });

        close.addActionListener(e -> dispose());

        p.add(addBtn);
        p.add(close);

        return p;
    }
}