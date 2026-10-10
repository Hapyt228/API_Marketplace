package com.example.marketplace.service.order;

import com.example.marketplace.dto.cart.CartItemResponse;
import com.example.marketplace.dto.cart.CartResponse;
import com.example.marketplace.dto.order.OrderItemResponse;
import com.example.marketplace.dto.order.OrderResponse;
import com.example.marketplace.entity.order.Order;
import com.example.marketplace.entity.order.OrderItem;
import com.example.marketplace.entity.order.StatusOrder;
import com.example.marketplace.entity.product.Product;
import com.example.marketplace.entity.сart.Cart;
import com.example.marketplace.entity.сart.CartItem;
import com.example.marketplace.repository.cart.CartRepository;
import com.example.marketplace.repository.order.OrderItemRepository;
import com.example.marketplace.repository.order.OrderRepository;
import com.example.marketplace.repository.user.UserRepository;
import com.example.marketplace.service.cart.CartService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    private final CartRepository cartRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final CartService cartService;

    public OrderService(OrderRepository orderRepository, CartRepository cartRepository, OrderItemRepository orderItemRepository, UserRepository userRepository, CartService cartService) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
        this.cartService = cartService;
    }

    public OrderResponse createOrderFromCart(Long userId){
        Cart cart = cartRepository.findByUser_Id(userId).orElseThrow(() -> new NoSuchElementException("Not found by id = " + userId));
        Order order = new Order();
        BigDecimal orderTotalPrice = new BigDecimal(0);
        List<OrderItem> orderItemList = new ArrayList<>();
        for(CartItem cartItem : cart.getCartItemList()){
           OrderItem orderItem =cartItemByOrderItem(cartItem, order);
           orderItemList.add(orderItem);
           BigDecimal totalPrice = orderItem.getPriceAtPurchase().multiply(new BigDecimal(orderItem.getQuantity()));
           orderTotalPrice = orderTotalPrice.add(totalPrice);
        }
        order.setUser(userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("Not found by id = " + userId)));
        order.setTotalPrice(orderTotalPrice);
        order.setOrderItemList(orderItemList);
        order.setStatus(StatusOrder.CREATED);
        order.setCreatedAt(LocalDateTime.now());
        orderRepository.save(order);
        orderItemRepository.saveAll(orderItemList);
        cartService.clearCart(userId);
        return orderByOrderResponse(order);
    }

    public List<OrderResponse> getAllOrderResponse(Long userId){
        List<Order> orderList = orderRepository.findAllByUser_Id(userId);
        List<OrderResponse> orderResponseList = new ArrayList<>();
        for(Order order : orderList){
            orderResponseList.add(orderByOrderResponse(order));
        }
        return orderResponseList;
    }

    public OrderResponse getOrderResponse(Long orderId, Long currentUserId){
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Not found by id = " + orderId));
        if (!order.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Order does not belong to current user");
        }
        return orderByOrderResponse(order);
    }

    private OrderItem cartItemByOrderItem(CartItem cartItem, Order order) {
        Product product = cartItem.getProduct();

        OrderItem orderIteme = new OrderItem();
        orderIteme.setOrder(order);
        orderIteme.setProduct(product);
        orderIteme.setQuantity(cartItem.getQuantity());
        orderIteme.setPriceAtPurchase(product.getPrice());

        return orderIteme;
    }

    private OrderItemResponse orderItemByOrderItemResponse(OrderItem orderItem) {
        OrderItemResponse orderItemResponse = new OrderItemResponse();
        orderItemResponse.setProductId(orderItem.getProduct().getId());
        orderItemResponse.setProductName(orderItem.getProduct().getName());
        orderItemResponse.setQuantity(orderItem.getQuantity());
        orderItemResponse.setPriceAtPurchase(orderItem.getPriceAtPurchase());
        orderItemResponse.setSubtotal(orderItem.getPriceAtPurchase().multiply(new BigDecimal(orderItem.getQuantity())));

        return orderItemResponse;
    }

    private OrderResponse orderByOrderResponse(Order order) {

        OrderResponse orderResponse = new OrderResponse();
        List <OrderItemResponse> orderItemList = new ArrayList<>();
        for (OrderItem orderItem : order.getOrderItemList()) {
            orderItemList.add(orderItemByOrderItemResponse(orderItem));
        }
        orderResponse.setItems(orderItemList);
        orderResponse.setTotalPrice(order.getTotalPrice());
        orderResponse.setStatus(order.getStatus());
        orderResponse.setCreatedAt(order.getCreatedAt());

        return orderResponse;
    }

}
