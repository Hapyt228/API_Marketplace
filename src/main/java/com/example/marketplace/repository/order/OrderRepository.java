package com.example.marketplace.repository.order;

import com.example.marketplace.entity.order.Order;
import com.example.marketplace.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByUser_Id(Long userId);

    Optional<Order> findByUser_Id(Long userId);

    Long user(User user);
}
