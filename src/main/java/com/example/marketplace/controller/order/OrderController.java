package com.example.marketplace.controller.order;

import com.example.marketplace.dto.order.OrderResponse;
import com.example.marketplace.entity.user.User;
import com.example.marketplace.service.order.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @AuthenticationPrincipal User user
    ){
        Long userId = user.getId();
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrderFromCart(userId));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders(
            @AuthenticationPrincipal User user
    ){
        Long userId = user.getId();
        return ResponseEntity.status(HttpStatus.OK).body(orderService.getAllOrderResponse(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(
            @AuthenticationPrincipal User user,
            @PathVariable Long id
    ){
        Long userId = user.getId();
        return ResponseEntity.status(HttpStatus.OK).body(orderService.getOrderResponse(id, userId));
    }
}
