package in.rautkart.service;

import in.rautkart.dto.AddressDtos;
import in.rautkart.dto.CatalogDtos;
import in.rautkart.dto.OrderDtos;
import in.rautkart.entity.Address;
import in.rautkart.entity.CartItem;
import in.rautkart.entity.Order;
import in.rautkart.entity.OrderItem;
import in.rautkart.entity.OrderStatus;
import in.rautkart.entity.PaymentStatus;
import in.rautkart.entity.Product;
import in.rautkart.entity.User;
import in.rautkart.exception.ApiException;
import in.rautkart.repository.OrderRepository;
import in.rautkart.repository.ProductRepository;
import in.rautkart.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Year;
import java.util.List;
import java.util.Locale;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartService cartService;
    private final AddressService addressService;
    private final PaymentService paymentService;

    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        UserRepository userRepository,
                        CartService cartService,
                        AddressService addressService,
                        PaymentService paymentService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.cartService = cartService;
        this.addressService = addressService;
        this.paymentService = paymentService;
    }

    /**
     * Turns the cart into an order.
     *
     * COD orders are confirmed immediately. Online orders are created as PENDING
     * and only flip to PAID once the Razorpay signature checks out - or right
     * away when the backend runs without Razorpay keys (mock mode).
     */
    @Transactional
    public OrderDtos.CheckoutResponse placeOrder(Long userId, OrderDtos.PlaceOrderRequest req) {
        List<CartItem> cartItems = cartService.rawItems(userId);
        if (cartItems.isEmpty()) {
            throw ApiException.badRequest("Your cart is empty");
        }

        User user = userRepository.findById(userId).orElseThrow(() -> ApiException.notFound("User"));
        Ship ship = resolveAddress(userId, req);

        Order order = Order.builder()
                .orderNumber(nextOrderNumber())
                .user(user)
                .status(OrderStatus.PLACED)
                .paymentStatus(PaymentStatus.PENDING)
                .shipName(ship.name())
                .shipPhone(ship.phone())
                .shipLine1(ship.line1())
                .shipLine2(ship.line2())
                .shipCity(ship.city())
                .shipState(ship.state())
                .shipPincode(ship.pincode())
                .subtotal(BigDecimal.ZERO)
                .deliveryFee(BigDecimal.ZERO)
                .total(BigDecimal.ZERO)
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem cartItem : cartItems) {
            Product product = productRepository.findById(cartItem.getProduct().getId())
                    .orElseThrow(() -> ApiException.notFound("Product"));

            if (!product.isActive()) {
                throw ApiException.badRequest(product.getName() + " is no longer available");
            }
            if (cartItem.getQuantity() > product.getStockQty()) {
                throw ApiException.badRequest("Only " + product.getStockQty() + " of "
                        + product.getName() + " left in stock");
            }

            BigDecimal lineTotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()))
                    .setScale(2, RoundingMode.HALF_UP);

            order.addItem(OrderItem.builder()
                    .product(product)
                    .productName(product.getName())
                    .productUnit(product.getUnit())
                    .imageUrl(product.getImageUrl())
                    .emoji(product.getEmoji())
                    .unitPrice(product.getPrice())
                    .quantity(cartItem.getQuantity())
                    .lineTotal(lineTotal)
                    .build());

            // Stock is reserved at checkout; cancelling an order puts it back.
            product.setStockQty(product.getStockQty() - cartItem.getQuantity());
            productRepository.save(product);

            subtotal = subtotal.add(lineTotal);
        }

        BigDecimal deliveryFee = cartService.deliveryFeeFor(subtotal);
        order.setSubtotal(subtotal);
        order.setDeliveryFee(deliveryFee);
        order.setTotal(subtotal.add(deliveryFee));

        boolean cod = "COD".equalsIgnoreCase(req.paymentMethod());
        String razorpayOrderId = null;

        if (cod) {
            order.setPaymentStatus(PaymentStatus.COD);
        } else if (paymentService.isConfigured()) {
            razorpayOrderId = paymentService.createOrder(order.getTotal(), order.getOrderNumber());
            order.setRazorpayOrderId(razorpayOrderId);
        } else {
            // Mock mode - no Razorpay keys configured, so treat the order as paid.
            order.setPaymentStatus(PaymentStatus.PAID);
        }

        orderRepository.save(order);
        cartService.clear(userId);

        boolean paymentRequired = razorpayOrderId != null;
        return new OrderDtos.CheckoutResponse(
                Mappers.toOrder(order),
                razorpayOrderId,
                paymentRequired ? paymentService.getKeyId() : null,
                paymentRequired ? PaymentService.toPaise(order.getTotal()) : null,
                paymentRequired);
    }

    @Transactional
    public OrderDtos.OrderResponse confirmPayment(Long userId, OrderDtos.PaymentVerificationRequest req) {
        Order order = orderRepository.findByRazorpayOrderId(req.razorpayOrderId())
                .orElseThrow(() -> ApiException.notFound("Order"));

        if (!order.getUser().getId().equals(userId)) {
            throw ApiException.notFound("Order");
        }

        boolean ok = paymentService.verifySignature(
                req.razorpayOrderId(), req.razorpayPaymentId(), req.razorpaySignature());

        if (!ok) {
            order.setPaymentStatus(PaymentStatus.FAILED);
            orderRepository.save(order);
            throw ApiException.badRequest("Payment could not be verified");
        }

        order.setPaymentStatus(PaymentStatus.PAID);
        order.setRazorpayPaymentId(req.razorpayPaymentId());
        return Mappers.toOrder(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public List<OrderDtos.OrderResponse> myOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(Mappers::toOrder)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderDtos.OrderResponse myOrder(Long userId, Long orderId) {
        return orderRepository.findByIdAndUserId(orderId, userId)
                .map(Mappers::toOrder)
                .orElseThrow(() -> ApiException.notFound("Order"));
    }

    @Transactional
    public OrderDtos.OrderResponse cancelMyOrder(Long userId, Long orderId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> ApiException.notFound("Order"));
        if (order.getStatus() != OrderStatus.PLACED) {
            throw ApiException.badRequest("This order is already being packed and can no longer be cancelled");
        }
        restock(order);
        order.setStatus(OrderStatus.CANCELLED);
        return Mappers.toOrder(orderRepository.save(order));
    }

    // -------------------------------------------------------------------- admin

    @Transactional(readOnly = true)
    public CatalogDtos.PageResponse<OrderDtos.OrderResponse> allOrders(String status, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        Page<Order> result = (status == null || status.isBlank())
                ? orderRepository.findAllByOrderByCreatedAtDesc(pageable)
                : orderRepository.findByStatusOrderByCreatedAtDesc(parseStatus(status), pageable);

        return new CatalogDtos.PageResponse<>(
                result.getContent().stream().map(Mappers::toOrder).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    @Transactional
    public OrderDtos.OrderResponse updateStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> ApiException.notFound("Order"));
        OrderStatus next = parseStatus(status);

        if (next == OrderStatus.CANCELLED && order.getStatus() != OrderStatus.CANCELLED) {
            restock(order);
        }
        if (next == OrderStatus.DELIVERED && order.getPaymentStatus() == PaymentStatus.COD) {
            order.setPaymentStatus(PaymentStatus.PAID);
        }

        order.setStatus(next);
        return Mappers.toOrder(orderRepository.save(order));
    }

    // ------------------------------------------------------------------ helpers

    private void restock(Order order) {
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            if (product == null) {
                continue;
            }
            product.setStockQty(product.getStockQty() + item.getQuantity());
            productRepository.save(product);
        }
    }

    private OrderStatus parseStatus(String status) {
        try {
            return OrderStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw ApiException.badRequest("Unknown order status: " + status);
        }
    }

    private String nextOrderNumber() {
        long count = orderRepository.count() + 1;
        return String.format("RK-%d-%06d", Year.now().getValue(), count);
    }

    private Ship resolveAddress(Long userId, OrderDtos.PlaceOrderRequest req) {
        if (req.addressId() != null) {
            Address a = addressService.requireOwned(userId, req.addressId());
            return new Ship(a.getFullName(), a.getPhone(), a.getLine1(), a.getLine2(),
                    a.getCity(), a.getState(), a.getPincode());
        }
        AddressDtos.AddressRequest a = req.address();
        if (a == null) {
            throw ApiException.badRequest("A delivery address is required");
        }

        // A fresh address typed at checkout is kept in the address book, so the
        // next order can just pick it from the list.
        addressService.create(userId, a);

        return new Ship(a.fullName(), a.phone(), a.line1(), a.line2(), a.city(), a.state(), a.pincode());
    }

    private record Ship(String name, String phone, String line1, String line2,
                        String city, String state, String pincode) {
    }
}
