package com.example.marketplace.repository.cart;

import com.example.marketplace.entity.user.User;
import com.example.marketplace.entity.сart.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart,Long> {
    Optional<Cart> findByUser_Id(Long userId);

    List<Cart> user(User user);
}
