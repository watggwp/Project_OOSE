package service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;

public class DailySalesReport {
    private final LocalDate date;
    private final double totalSales;
    private final Map<String, Integer> soldProductCounts;

    /**
     * คอนสตรักเตอร์สำหรับสร้างรายงานยอดขายประจำวัน
     */
    public DailySalesReport(LocalDate date, double totalSales, Map<String, Integer> soldProductCounts) {
        this.date = date;
        this.totalSales = totalSales;
        this.soldProductCounts = soldProductCounts;
    }

    /**
     * ดึงวันที่ของรายงาน
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * ดึงยอดขายรวมในวันนั้น
     */
    public double getTotalSales() {
        return totalSales;
    }

    /**
     * ดึงจำนวนการขายของสินค้าแต่ละรายการ
     */
    public Map<String, Integer> getSoldProductCounts() {
        return Collections.unmodifiableMap(soldProductCounts);
    }
}
