package in.rautkart.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(length = 2000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    /** Selling price in INR. */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /** Printed / struck-through price, used to show a discount. */
    @Column(precision = 10, scale = 2)
    private BigDecimal mrp;

    /** e.g. "1 kg", "500 g", "1 L", "12 pcs" */
    @Column(nullable = false)
    private String unit;

    @Column(name = "stock_qty", nullable = false)
    private Integer stockQty;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    /** Fallback artwork when no image URL is set - keeps the demo offline-friendly. */
    @Column(length = 8)
    private String emoji;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    /** Stock level at or below which the storefront nudges the shopper. */
    public static final int LOW_STOCK_THRESHOLD = 10;

    public boolean isInStock() {
        return active && stockQty != null && stockQty > 0;
    }

    public boolean isLowStock() {
        return isInStock() && stockQty <= LOW_STOCK_THRESHOLD;
    }
}
