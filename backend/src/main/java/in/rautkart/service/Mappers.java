package in.rautkart.service;

import in.rautkart.dto.AddressDtos;
import in.rautkart.dto.AuthDtos;
import in.rautkart.dto.CatalogDtos;
import in.rautkart.dto.OrderDtos;
import in.rautkart.entity.Address;
import in.rautkart.entity.Category;
import in.rautkart.entity.Order;
import in.rautkart.entity.OrderItem;
import in.rautkart.entity.Product;
import in.rautkart.entity.User;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

/** Entity to DTO conversions, kept in one place so the JSON shape is easy to audit. */
public final class Mappers {

    private Mappers() {
    }

    public static AuthDtos.UserResponse toUser(User u) {
        return new AuthDtos.UserResponse(u.getId(), u.getName(), u.getEmail(), u.getPhone(), u.getRole().name());
    }

    public static CatalogDtos.CategoryResponse toCategory(Category c) {
        return new CatalogDtos.CategoryResponse(c.getId(), c.getName(), c.getSlug(), c.getIcon(), c.getSortOrder());
    }

    public static CatalogDtos.ProductResponse toProduct(Product p) {
        return new CatalogDtos.ProductResponse(
                p.getId(),
                p.getName(),
                p.getSlug(),
                p.getDescription(),
                p.getCategory().getId(),
                p.getCategory().getName(),
                p.getCategory().getSlug(),
                p.getPrice(),
                p.getMrp(),
                p.getUnit(),
                p.getStockQty(),
                p.getImageUrl(),
                p.getEmoji(),
                p.isActive(),
                p.isInStock(),
                p.isLowStock(),
                availabilityLabel(p),
                discountPercent(p.getMrp(), p.getPrice())
        );
    }

    /** The stock line the storefront shows verbatim - no client-side thresholds. */
    private static String availabilityLabel(Product p) {
        if (!p.isInStock()) {
            return "Out of stock";
        }
        if (p.isLowStock()) {
            return "Only " + p.getStockQty() + " left in stock";
        }
        return "In stock";
    }

    public static AddressDtos.AddressResponse toAddress(Address a) {
        return new AddressDtos.AddressResponse(
                a.getId(), a.getFullName(), a.getPhone(), a.getLine1(), a.getLine2(),
                a.getCity(), a.getState(), a.getPincode(), a.isDefault());
    }

    public static OrderDtos.OrderItemResponse toOrderItem(OrderItem i) {
        return new OrderDtos.OrderItemResponse(
                i.getId(),
                i.getProduct() == null ? null : i.getProduct().getId(),
                i.getProductName(),
                i.getProductUnit(),
                i.getImageUrl(),
                i.getEmoji(),
                i.getUnitPrice(),
                i.getQuantity(),
                i.getLineTotal());
    }

    public static OrderDtos.OrderResponse toOrder(Order o) {
        List<OrderDtos.OrderItemResponse> items = o.getItems().stream().map(Mappers::toOrderItem).toList();
        return new OrderDtos.OrderResponse(
                o.getId(),
                o.getOrderNumber(),
                o.getStatus().name(),
                o.getPaymentStatus().name(),
                o.getSubtotal(),
                o.getDeliveryFee(),
                o.getTotal(),
                o.getShipName(),
                o.getShipPhone(),
                o.getShipLine1(),
                o.getShipLine2(),
                o.getShipCity(),
                o.getShipState(),
                o.getShipPincode(),
                o.getUser().getName(),
                o.getUser().getEmail(),
                o.getRazorpayOrderId(),
                o.getCreatedAt(),
                items);
    }

    /** null when there is no meaningful discount to show. */
    public static Integer discountPercent(BigDecimal mrp, BigDecimal price) {
        if (mrp == null || price == null || mrp.compareTo(price) <= 0 || mrp.signum() <= 0) {
            return null;
        }
        BigDecimal pct = mrp.subtract(price)
                .multiply(BigDecimal.valueOf(100))
                .divide(mrp, 0, RoundingMode.HALF_UP);
        return pct.intValue();
    }

    /** Turns a product name into a URL-safe slug. */
    public static String slugify(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        String slug = normalized.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        return slug.isEmpty() ? "item" : slug;
    }
}
