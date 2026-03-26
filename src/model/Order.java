package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Order {
    private final String orderId;
    private final LocalDateTime createdAt;
    private final List<OrderItem> items = new ArrayList<>();

    /**
     * คอนสตรักเตอร์สำหรับสร้างออเดอร์ใหม่โดยกำหนดเวลาปัจจุบันอัตโนมัติ
     */
    public Order(String orderId) {
        this(orderId, LocalDateTime.now());
    }

    /**
     * คอนสตรักเตอร์สำหรับสร้างออเดอร์พร้อมระบุเวลาที่สร้าง
     */
    public Order(String orderId, LocalDateTime createdAt) {
        this.orderId = orderId;
        this.createdAt = createdAt;
    }

    /**
     * เพิ่มสินค้าลงในออเดอร์ หากมีสินค้านั้นอยู่แล้วจะบวกจำนวนเพิ่มเข้าไป
     */
    public void addProduct(Product product, int quantity) {
        for (OrderItem item : items) {
            if (item.getProduct().getCode().equals(product.getCode())) {
                item.addQuantity(quantity);
                return;
            }
        }
        items.add(new OrderItem(product, quantity));
    }



    /**
     * คำนวณราคารวมของสินค้าทั้งหมดในออเดอร์ (ยังไม่รวมภาษี)
     */
    public double calculateSubtotal() {
        double total = 0;
        for (OrderItem item : items) {
            total += item.getLineTotal();
        }
        return total;
    }

    /**
     * คำนวณภาษีมูลค่าเพิ่ม (VAT) ตามเปอร์เซ็นต์ที่กำหนด
     */
    public double calculateVat(double vatRatePercent) {
        return calculateSubtotal() * (vatRatePercent / 100.0);
    }

    /**
     * คำนวณราคาสุทธิ สามารถเลือกรวมหรือไม่รวมภาษีมูลค่าเพิ่มได้
     */
    public double calculateTotal(boolean includeVat, double vatRatePercent) {
        if (!includeVat) {
            return calculateSubtotal();
        }
        return calculateSubtotal() + calculateVat(vatRatePercent);
    }

    /**
     * ดึงรหัสออเดอร์
     */
    public String getOrderId() {
        return orderId;
    }

    /**
     * ดึงเวลาที่สร้างออเดอร์
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * ดึงรายการสินค้าทั้งหมดในออเดอร์ (ไม่สามารถแก้ไขลิสต์ได้โดยตรง)
     */
    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }
}
