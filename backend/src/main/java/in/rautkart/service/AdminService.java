package in.rautkart.service;

import in.rautkart.dto.AdminDtos;
import in.rautkart.entity.OrderStatus;
import in.rautkart.entity.Product;
import in.rautkart.repository.OrderRepository;
import in.rautkart.repository.ProductRepository;
import in.rautkart.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class AdminService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public AdminService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public AdminDtos.DashboardResponse dashboard() {
        Instant weekAgo = Instant.now().minus(7, ChronoUnit.DAYS);

        List<AdminDtos.TopProduct> topProducts = orderRepository
                .topSellingProducts(PageRequest.of(0, 5)).stream()
                .map(row -> new AdminDtos.TopProduct((String) row[0], ((Number) row[1]).longValue()))
                .toList();

        List<AdminDtos.LowStockProduct> lowStock = productRepository
                .findByStockQtyLessThanEqualOrderByStockQtyAsc(Product.LOW_STOCK_THRESHOLD).stream()
                .filter(Product::isActive)
                .map(p -> new AdminDtos.LowStockProduct(p.getId(), p.getName(), p.getUnit(), p.getStockQty()))
                .toList();

        BigDecimal revenue = orderRepository.totalRevenue();

        return new AdminDtos.DashboardResponse(
                orderRepository.count(),
                orderRepository.countByCreatedAtAfter(weekAgo),
                orderRepository.countByStatus(OrderStatus.PLACED),
                productRepository.countByActiveTrue(),
                userRepository.count(),
                revenue == null ? BigDecimal.ZERO : revenue,
                topProducts,
                lowStock);
    }
}
