package in.rautkart.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/** Category and product payloads for both the storefront and the admin panel. */
public final class CatalogDtos {

    private CatalogDtos() {
    }

    public record CategoryResponse(Long id, String name, String slug, String icon, Integer sortOrder) {
    }

    public record CategoryRequest(
            @NotBlank @Size(max = 80) String name,
            String icon,
            Integer sortOrder
    ) {
    }

    public record ProductResponse(
            Long id,
            String name,
            String slug,
            String description,
            Long categoryId,
            String categoryName,
            String categorySlug,
            BigDecimal price,
            BigDecimal mrp,
            String unit,
            Integer stockQty,
            String imageUrl,
            String emoji,
            boolean active,
            boolean inStock,
            boolean lowStock,
            String availabilityLabel,
            Integer discountPercent
    ) {
    }

    public record ProductRequest(
            @NotBlank @Size(max = 150) String name,
            @Size(max = 2000) String description,
            @NotNull Long categoryId,
            @NotNull @DecimalMin(value = "0.01") BigDecimal price,
            @DecimalMin(value = "0.00") BigDecimal mrp,
            @NotBlank @Size(max = 40) String unit,
            @NotNull @PositiveOrZero Integer stockQty,
            String imageUrl,
            String emoji,
            Boolean active
    ) {
    }

    /** Slim, framework-agnostic page wrapper so the JSON stays stable. */
    public record PageResponse<T>(
            List<T> content,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {
    }
}
