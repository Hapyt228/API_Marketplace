package com.example.marketplace.controller.cart;

import com.example.marketplace.dto.cart.CartItemRequest;
import com.example.marketplace.dto.cart.CartResponse;
import com.example.marketplace.entity.user.User;
import com.example.marketplace.service.cart.CartService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/api/cart")
    public ResponseEntity<CartResponse> addToCart(
            @RequestBody @Valid CartItemRequest cartItemRequest,
            @AuthenticationPrincipal User user
    ){
        Long userId = user.getId();
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.addToCart(userId, cartItemRequest));
    }

    @GetMapping("/api/cart")
    public ResponseEntity<CartResponse> getCart(
            @AuthenticationPrincipal User user
    ){
        Long userId = user.getId();
        return ResponseEntity.status(HttpStatus.OK).body(cartService.getCart(userId));
    }

    @DeleteMapping("/api/cart/{productId}")
    public ResponseEntity<CartResponse> removeFromCart(
            @PathVariable("productId") Long productId,
            @AuthenticationPrincipal User user
    ){
        Long userId = user.getId();
        return ResponseEntity.status(HttpStatus.OK).body(cartService.removeFromCart(userId, productId));
    }

    @DeleteMapping("/api/cart")
    public ResponseEntity<CartResponse> clearCart(
            @AuthenticationPrincipal User user
    ){
        Long userId = user.getId();
        return ResponseEntity.status(HttpStatus.OK).body(cartService.clearCart(userId));
    }

}
