package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ModernLoginDialog extends JDialog {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private boolean confirmed = false;

    /**
     * คอนสตรักเตอร์สำหรับสร้างหน้าต่างเข้าสู่ระบบ
     */
    public ModernLoginDialog(Frame parent) {
        super(parent, "Welcome Back", true);

        setLayout(new BorderLayout());

        add(createHeader(), BorderLayout.NORTH);
        add(createForm(), BorderLayout.CENTER);
        add(createButtons(), BorderLayout.SOUTH);

        pack();
        setMinimumSize(new Dimension(420, 300));
        setLocationRelativeTo(parent);
    }

    /**
     * สร้างส่วนหัว (Header) ของหน้าต่างล็อกอิน
     */
    private JPanel createHeader() {
        JPanel p = new JPanel();
        p.setBackground(new Color(52, 152, 219));
        p.setBorder(new EmptyBorder(18, 10, 18, 10));

        JLabel l = new JLabel("Login");
        l.setForeground(Color.WHITE);
        l.setFont(new Font("Tahoma", Font.BOLD, 20));
        p.add(l);

        return p;
    }

    /**
     * สร้างฟอร์มกรอกข้อมูลสำหรับล็อกอิน (Username, Password)
     */
    private JPanel createForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(25, 40, 25, 40));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        g.gridx = 0;
        g.insets = new Insets(8, 0, 8, 0);

        panel.add(new JLabel("Username"), g);

        g.gridy = 1;
        usernameField = new JTextField();
        usernameField.setPreferredSize(new Dimension(300, 38));
        panel.add(usernameField, g);

        g.gridy = 2;
        panel.add(new JLabel("Password"), g);

        g.gridy = 3;
        passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(300, 38));
        panel.add(passwordField, g);

        return panel;
    }

    /**
     * สร้างส่วนปุ่มกดด้านล่าง (Cancel, Login)
     */
    private JPanel createButtons() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(5, 20, 15, 20));

        JButton cancel = new JButton("Cancel");
        JButton ok = new JButton("Login");

        cancel.setPreferredSize(new Dimension(110, 38));
        ok.setPreferredSize(new Dimension(110, 38));

        ok.setBackground(new Color(52, 152, 219));
        ok.setForeground(Color.WHITE);

        cancel.addActionListener(e -> dispose());
        ok.addActionListener(e -> {
            if (usernameField.getText().trim().isEmpty() || new String(passwordField.getPassword()).isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter both username and password.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            confirmed = true;
            dispose();
        });

        p.add(cancel);
        p.add(ok);

        return p;
    }

    /**
     * ตรวจสอบว่าผู้ใช้ได้ยืนยันการเข้าสู่ระบบหรือไม่
     */
    public boolean isConfirmed() { return confirmed; }

    /**
     * ดึงข้อมูลชื่อผู้ใช้และรหัสผ่านที่กรอกไว้
     */
    public String[] getCredentials() {
        return new String[]{
            usernameField.getText().trim(),
            new String(passwordField.getPassword())
        };
    }
}