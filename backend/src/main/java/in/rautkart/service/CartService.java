package in.rautkart.service;

import in.rautkart.dto.CartDtos;
import in.rautkart.entity.CartItem;
import in.rautkart.entity.Product;
import in.rautkart.entity.User;
import in.rautkart.exception.ApiException;
import in.rautkart.repository.CartItemRepository;
import in.rautkart.repository.ProductRepository;
import in.rautkart.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Cart lives server-side keyed by user, so it survives a refresh and follows the
 * customer between devices.
 */
@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final BigDecimal deliveryFee;
    private final BigDecimal freeDeliveryAbove;

    public CartService(CartItemRepository cartItemRepository,
                       ProductRepository productRepository,
                       UserRepository userRepository,
                       @Value("${rautkart.delivery-fee}") BigDecimal deliveryFee,
                       @Value("${rautkart.free-delivery-above}") BigDecimal freeDeliveryAbove) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.deliveryFee = deliveryFee;
        this.freeDeliveryAbove = freeDeliveryAbove;
    }

    @Transactional(readOnly = true)
    public CartDtos.CartResponse getCart(Long userId) {
        return toResponse(cartItemRepository.findByUserIdOrderByIdAsc(userId));
    }

    @Transactional
    public CartDtos.CartResponse addItem(Long userId, CartDtos.AddToCartRequest req) {
        Product product = productRepository.findById(req.productId())
                .orElseThrow(() -> ApiException.notFound("Product"));
        if (!product.isActive()) {
            throw ApiException.badRequest("That product is no longer available");
        }

        CartItem item = cartItemRepository.findByUserIdAndProductId(userId, product.getId())
                .orElseGet(() -> {
                    User user = userRepository.getReferenceById(userId);
                    return CartItem.builder().user(user).product(product).quantity(0).build();
                });

        int newQty = item.getQuantity() + req.quantity();
        if (newQty > product.getStockQty()) {
            throw ApiException.badRequest("Only " + product.getStockQty() + " left in stock");
        }
        item.setQuantity(newQty);
        cartItemRepository.save(item);

        return getCart(userId);
    }

    /** Quantity 0 removes the line. */
    @Transactional
    public CartDtos.CartResponse updateQuantity(Long userId, Long productId, int quantity) {
        CartItem item = cartItemRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> ApiException.notFound("Cart item"));

        if (quantity <= 0) {
            cartItemRepository.delete(item);
            return getCart(userId);
        }
        if (quantity > item.getProduct().getStockQty()) {
            throw ApiException.badRequest("Only " + item.getProduct().getStockQty() + " left in stock");
        }
        item.setQuantity(quantity);
        cartItemRepository.save(item);
        return getCart(userId);
    }

    @Transactional
    public CartDtos.CartResponse removeItem(Long userId, Long productId) {
        cartItemRepository.findByUserIdAndProductId(userId, productId).ifPresent(cartItemRepository::delete);
        return getCart(userId);
    }

    @Transactional
    public void clear(Long userId) {
        cartItemRepository.deleteByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<CartItem> rawItems(Long userId) {
        return cartItemRepository.findByUserIdOrderByIdAsc(userId);
    }

    public BigDecimal deliveryFeeFor(BigDecimal subtotal) {
        if (subtotal.signum() <= 0 || subtotal.compareTo(freeDeliveryAbove) >= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return deliveryFee;
    }

    private CartDtos.CartResponse toResponse(List<CartItem> items) {
        List<CartDtos.CartItemResponse> lines = items.stream().map(item -> {
            Product p = item.getProduct();
            BigDecimal lineTotal = p.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            return new CartDtos.CartItemResponse(
                    item.getId(), p.getId(), p.getName(), p.getSlug(), p.getUnit(), p.getImageUrl(),
                    p.getEmoji(), p.getPrice(), item.getQuantity(), p.getStockQty(), lineTotal);
        }).toList();

        BigDecimal subtotal = lines.stream()
                .map(CartDtos.CartItemResponse::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal fee = deliveryFeeFor(subtotal);
        int itemCount = lines.stream().mapToInt(CartDtos.CartItemResponse::quantity).sum();

        BigDecimal toFreeDelivery = freeDeliveryAbove.subtract(subtotal).max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);

        return new CartDtos.CartResponse(lines, itemCount, subtotal, fee, subtotal.add(fee),
                freeDeliveryAbove, toFreeDelivery);
    }
}
