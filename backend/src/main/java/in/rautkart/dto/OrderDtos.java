package in.rautkart.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class OrderDtos {

    private OrderDtos() {
    }

    /**
     * Either pick a saved address by id, or pass a fresh one inline.
     * paymentMethod is "ONLINE" (Razorpay test mode) or "COD".
     */
    public record PlaceOrderRequest(
            Long addressId,
            @Valid AddressDtos.AddressRequest address,
            @NotBlank String paymentMethod
    ) {
    }

    public record OrderItemResponse(
            Long id,
            Long productId,
            String productName,
            String productUnit,
            String imageUrl,
            String emoji,
            BigDecimal unitPrice,
            Integer quantity,
            BigDecimal lineTotal
    ) {
    }

    public record OrderResponse(
            Long id,
            String orderNumber,
            String status,
            String paymentStatus,
            BigDecimal subtotal,
            BigDecimal deliveryFee,
            BigDecimal total,
            String shipName,
            String shipPhone,
            String shipLine1,
            String shipLine2,
            String shipCity,
            String shipState,
            String shipPincode,
            String customerName,
            String customerEmail,
            String razorpayOrderId,
            Instant createdAt,
            List<OrderItemResponse> items
    ) {
    }

    /**
     * Returned by checkout. When razorpayOrderId is null the backend is running
     * without Razorpay keys and the order is already confirmed (mock payment).
     */
    public record CheckoutResponse(
            OrderResponse order,
            String razorpayOrderId,
            String razorpayKeyId,
            Integer amountInPaise,
            boolean paymentRequired
    ) {
    }

    public record PaymentVerificationRequest(
            @NotBlank String razorpayOrderId,
            @NotBlank String razorpayPaymentId,
            @NotBlank String razorpaySignature
    ) {
    }

    public record UpdateStatusRequest(
            @NotNull String status
    ) {
    }
}
