package model;

import java.time.LocalDateTime;

public class SalesRecord {
    private final Order order;
    private final double subtotal;
    private final double vatAmount;
    private final double totalAmount;
    private final double cashReceived;
    private final double changeAmount;
    private final LocalDateTime paidAt;

    /**
     * คอนสตรักเตอร์สำหรับสร้างบันทึกประวัติการขาย
     */
    public SalesRecord(Order order,
                       double subtotal,
                       double vatAmount,
                       double totalAmount,
                       double cashReceived,
                       double changeAmount,
                       LocalDateTime paidAt) {
        this.order = order;
        this.subtotal = subtotal;
        this.vatAmount = vatAmount;
        this.totalAmount = totalAmount;
        this.cashReceived = cashReceived;
        this.changeAmount = changeAmount;
        this.paidAt = paidAt;
    }

    /**
     * ดึงข้อมูลออเดอร์ที่ถูกบันทึก
     */
    public Order getOrder() {
        return order;
    }

    /**
     * ดึงยอดรวมราคาสินค้าก่อนภาษี
     */
    public double getSubtotal() {
        return subtotal;
    }

    /**
     * ดึงจำนวนเงินภาษีมูลค่าเพิ่ม (VAT)
     */
    public double getVatAmount() {
        return vatAmount;
    }

    /**
     * ดึงยอดรวมราคาสิทธิ (รวมภาษี)
     */
    public double getTotalAmount() {
        return totalAmount;
    }

    /**
     * ดึงจำนวนเงินสดที่รับจากลูกค้า
     */
    public double getCashReceived() {
        return cashReceived;
    }

    /**
     * ดึงจำนวนเงินทอน
     */
    public double getChangeAmount() {
        return changeAmount;
    }

    /**
     * ดึงเวลาที่มีการชำระเงิน
     */
    public LocalDateTime getPaidAt() {
        return paidAt;
    }
}
