package in.rautkart.dto;

import java.math.BigDecimal;
import java.util.List;

public final class AdminDtos {

    private AdminDtos() {
    }

    public record TopProduct(String productName, long unitsSold) {
    }

    public record LowStockProduct(Long id, String name, String unit, Integer stockQty) {
    }

    public record DashboardResponse(
            long totalOrders,
            long ordersLast7Days,
            long pendingOrders,
            long activeProducts,
            long customers,
            BigDecimal totalRevenue,
            List<TopProduct> topProducts,
            List<LowStockProduct> lowStock
    ) {
    }
}
