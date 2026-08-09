package in.rautkart.controller;

import in.rautkart.dto.CartDtos;
import in.rautkart.security.AuthUser;
import in.rautkart.service.CartService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public CartDtos.CartResponse cart(@AuthenticationPrincipal AuthUser user) {
        return cartService.getCart(user.getId());
    }

    @PostMapping("/items")
    public CartDtos.CartResponse add(@AuthenticationPrincipal AuthUser user,
                                     @Valid @RequestBody CartDtos.AddToCartRequest request) {
        return cartService.addItem(user.getId(), request);
    }

    @PutMapping("/items/{productId}")
    public CartDtos.CartResponse update(@AuthenticationPrincipal AuthUser user,
                                        @PathVariable Long productId,
                                        @Valid @RequestBody CartDtos.UpdateQuantityRequest request) {
        return cartService.updateQuantity(user.getId(), productId, request.quantity());
    }

    @DeleteMapping("/items/{productId}")
    public CartDtos.CartResponse remove(@AuthenticationPrincipal AuthUser user, @PathVariable Long productId) {
        return cartService.removeItem(user.getId(), productId);
    }

    @DeleteMapping
    public CartDtos.CartResponse clear(@AuthenticationPrincipal AuthUser user) {
        cartService.clear(user.getId());
        return cartService.getCart(user.getId());
    }
}
