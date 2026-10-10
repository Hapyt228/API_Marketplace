package com.example.marketplace.repository.cart;

import com.example.marketplace.entity.cart.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}
