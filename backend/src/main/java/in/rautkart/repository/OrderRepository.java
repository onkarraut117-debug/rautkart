package in.rautkart.repository;

import in.rautkart.entity.Order;
import in.rautkart.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Order> findByIdAndUserId(Long id, Long userId);

    Optional<Order> findByRazorpayOrderId(String razorpayOrderId);

    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status, Pageable pageable);

    long countByStatus(OrderStatus status);

    long countByCreatedAtAfter(Instant since);

    @Query("SELECT COALESCE(SUM(o.total), 0) FROM Order o WHERE o.status <> in.rautkart.entity.OrderStatus.CANCELLED")
    BigDecimal totalRevenue();

    @Query("""
            SELECT oi.productName, SUM(oi.quantity)
            FROM OrderItem oi
            WHERE oi.order.status <> in.rautkart.entity.OrderStatus.CANCELLED
            GROUP BY oi.productName
            ORDER BY SUM(oi.quantity) DESC
            """)
    List<Object[]> topSellingProducts(Pageable pageable);
}
