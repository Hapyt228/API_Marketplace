package com.example.marketplace.service.order;

import com.example.marketplace.dto.order.OrderResponse;
import com.example.marketplace.entity.cart.Cart;
import com.example.marketplace.entity.cart.CartItem;
import com.example.marketplace.entity.order.Order;
import com.example.marketplace.entity.order.StatusOrder;
import com.example.marketplace.entity.product.Product;
import com.example.marketplace.entity.user.User;
import com.example.marketplace.exception.EmptyCartException;
import com.example.marketplace.exception.ResourceNotFoundException;
import com.example.marketplace.repository.cart.CartRepository;
import com.example.marketplace.repository.order.OrderItemRepository;
import com.example.marketplace.repository.order.OrderRepository;
import com.example.marketplace.repository.user.UserRepository;
import com.example.marketplace.service.cart.CartService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock OrderRepository orderRepository;
    @Mock CartRepository cartRepository;
    @Mock OrderItemRepository orderItemRepository;
    @Mock UserRepository userRepository;
    @Mock CartService cartService;
    @InjectMocks OrderService orderService;

    private void addItem(Cart cart, long productId, String price, int qty) {
        Product p = mock(Product.class);
        lenient().when(p.getId()).thenReturn(productId);
        lenient().when(p.getPrice()).thenReturn(new BigDecimal(price));
        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProduct(p);
        item.setQuantity(qty);
        cart.getCartItemList().add(item);
    }

    @Test
    void createOrder_noCart_throwsEmptyCart() {
        when(cartRepository.findByUser_Id(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrderFromCart(1L))
                .isInstanceOf(EmptyCartException.class);
    }

    @Test
    void createOrder_emptyCart_throwsEmptyCart() {
        when(cartRepository.findByUser_Id(1L)).thenReturn(Optional.of(new Cart()));

        assertThatThrownBy(() -> orderService.createOrderFromCart(1L))
                .isInstanceOf(EmptyCartException.class);
    }

    @Test
    void createOrder_countsTotalSetsStatusAndClearsCart() {
        Cart cart = new Cart();
        addItem(cart, 1L, "10.00", 2);
        addItem(cart, 2L, "5.00", 1);
        when(cartRepository.findByUser_Id(1L)).thenReturn(Optional.of(cart));
        when(userRepository.findById(1L)).thenReturn(Optional.of(mock(User.class)));

        OrderResponse response = orderService.createOrderFromCart(1L);

        assertThat(response.getTotalPrice()).isEqualByComparingTo("25.00");
        assertThat(response.getStatus()).isEqualTo(StatusOrder.CREATED);
        assertThat(response.getItems()).hasSize(2);
        verify(cartService).clearCart(1L);
    }

    @Test
    void getOrder_foreignUser_throwsAccessDenied() {
        User owner = mock(User.class);
        when(owner.getId()).thenReturn(1L);
        Order order = new Order();
        order.setUser(owner);
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.getOrderResponse(10L, 2L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getOrder_unknownId_throwsNotFound() {
        when(orderRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderResponse(10L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}