package in.rautkart.repository;

import in.rautkart.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySlug(String slug);

    boolean existsBySlug(String slug);

    List<Product> findTop8ByActiveTrueAndStockQtyGreaterThanOrderByCreatedAtDesc(int stockQty);

    /**
     * Storefront search. Every filter is optional - a null parameter means
     * "don't filter on this".
     *
     * The text filter takes a ready-made LIKE pattern rather than a nullable
     * term: PostgreSQL cannot infer the type of a null bind parameter inside
     * CONCAT, which made an empty search blow up. Callers pass "%" to match all.
     */
    @Query("""
            SELECT p FROM Product p
            WHERE p.active = true
              AND (:categorySlug IS NULL OR p.category.slug = :categorySlug)
              AND (LOWER(p.name) LIKE :pattern ESCAPE '!'
                   OR LOWER(COALESCE(p.description, '')) LIKE :pattern ESCAPE '!')
              AND (:minPrice IS NULL OR p.price >= :minPrice)
              AND (:maxPrice IS NULL OR p.price <= :maxPrice)
              AND (:inStockOnly = false OR p.stockQty > 0)
            """)
    Page<Product> search(@Param("pattern") String pattern,
                         @Param("categorySlug") String categorySlug,
                         @Param("minPrice") BigDecimal minPrice,
                         @Param("maxPrice") BigDecimal maxPrice,
                         @Param("inStockOnly") boolean inStockOnly,
                         Pageable pageable);

    List<Product> findByStockQtyLessThanEqualOrderByStockQtyAsc(int threshold);

    long countByActiveTrue();
}
