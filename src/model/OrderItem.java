package model;

public class OrderItem {
    private final Product product;
    private int quantity;

    /**
     * คอนสตรักเตอร์สำหรับสร้างรายการสินค้าในออเดอร์ พร้อมระบุจำนวน
     */
    public OrderItem(Product product, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        this.product = product;
        this.quantity = quantity;
    }

    /**
     * ดึงข้อมูลสินค้า
     */
    public Product getProduct() {
        return product;
    }

    /**
     * ดึงจำนวนสินค้า
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * เพิ่มจำนวนสินค้าตามที่ระบุ
     */
    public void addQuantity(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }
        this.quantity += amount;
    }

    /**
     * คำนวณราคารวมของรายการสินค้านี้ (ราคาต่อหน่วย * จำนวน)
     */
    public double getLineTotal() {
        return product.getPrice() * quantity;
    }
}
