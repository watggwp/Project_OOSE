package service;

import model.Order;
import model.OrderItem;
import model.SalesRecord;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportService {

    /**
     * สร้างรายงานยอดขายประจำวันจากประวัติการขายที่ระบุ
     */
    public DailySalesReport generateDailyReport(List<SalesRecord> salesRecords, LocalDate date) {
        double totalSales = 0.0;
        Map<String, Integer> soldCounts = new HashMap<>();

        for (SalesRecord salesRecord : salesRecords) {
            Order order = salesRecord.getOrder();
            if (!salesRecord.getPaidAt().toLocalDate().equals(date)) {
                continue;
            }

            totalSales += salesRecord.getTotalAmount();

            for (OrderItem item : order.getItems()) {
                String productName = item.getProduct().getName();
                soldCounts.put(productName, soldCounts.getOrDefault(productName, 0) + item.getQuantity());
            }
        }

        return new DailySalesReport(date, totalSales, soldCounts);
    }
}
