package com.example.marketplace.repository.product;

import com.example.marketplace.entity.product.Product;
import com.example.marketplace.entity.сart.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
