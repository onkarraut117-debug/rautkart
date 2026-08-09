package in.rautkart.controller;

import in.rautkart.dto.OrderDtos;
import in.rautkart.security.AuthUser;
import in.rautkart.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /** Checkout. Converts the cart into an order and, for online payments, a Razorpay order. */
    @PostMapping
    public OrderDtos.CheckoutResponse place(@AuthenticationPrincipal AuthUser user,
                                            @Valid @RequestBody OrderDtos.PlaceOrderRequest request) {
        return orderService.placeOrder(user.getId(), request);
    }

    /** Called by the frontend once the Razorpay widget hands back a payment. */
    @PostMapping("/payment/verify")
    public OrderDtos.OrderResponse verify(@AuthenticationPrincipal AuthUser user,
                                          @Valid @RequestBody OrderDtos.PaymentVerificationRequest request) {
        return orderService.confirmPayment(user.getId(), request);
    }

    @GetMapping
    public List<OrderDtos.OrderResponse> myOrders(@AuthenticationPrincipal AuthUser user) {
        return orderService.myOrders(user.getId());
    }

    @GetMapping("/{id}")
    public OrderDtos.OrderResponse myOrder(@AuthenticationPrincipal AuthUser user, @PathVariable Long id) {
        return orderService.myOrder(user.getId(), id);
    }

    @PostMapping("/{id}/cancel")
    public OrderDtos.OrderResponse cancel(@AuthenticationPrincipal AuthUser user, @PathVariable Long id) {
        return orderService.cancelMyOrder(user.getId(), id);
    }
}
