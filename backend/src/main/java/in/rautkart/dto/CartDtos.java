package in.rautkart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.List;

public final class CartDtos {

    private CartDtos() {
    }

    public record AddToCartRequest(
            @NotNull Long productId,
            @NotNull @Min(1) Integer quantity
    ) {
    }

    public record UpdateQuantityRequest(
            @NotNull @PositiveOrZero Integer quantity
    ) {
    }

    public record CartItemResponse(
            Long id,
            Long productId,
            String name,
            String slug,
            String unit,
            String imageUrl,
            String emoji,
            BigDecimal price,
            Integer quantity,
            Integer stockQty,
            BigDecimal lineTotal
    ) {
    }

    public record CartResponse(
            List<CartItemResponse> items,
            int itemCount,
            BigDecimal subtotal,
            BigDecimal deliveryFee,
            BigDecimal total,
            BigDecimal freeDeliveryAbove,
            /** How much more to spend to unlock free delivery. Zero once it is earned. */
            BigDecimal amountForFreeDelivery
    ) {
    }
}
