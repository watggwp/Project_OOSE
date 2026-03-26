package service;

public class PaymentService {

    /**
     * คำนวณเงินทอนจากการชำระเงิน โดยตรวจสอบว่าเงินที่รับมาพอดีหรือมากกว่ายอดรวมหรือไม่
     */
    public double calculateChange(double cashReceived, double totalAmount) {
        if (cashReceived < totalAmount) {
            throw new IllegalArgumentException("Cash received is not enough");
        }
        return cashReceived - totalAmount;
    }
}
