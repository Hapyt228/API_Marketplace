package com.example.marketplace.service.cart;

import com.example.marketplace.dto.cart.CartItemRequest;
import com.example.marketplace.dto.cart.CartResponse;
import com.example.marketplace.entity.cart.Cart;
import com.example.marketplace.entity.cart.CartItem;
import com.example.marketplace.entity.product.Product;
import com.example.marketplace.exception.ResourceNotFoundException;
import com.example.marketplace.repository.cart.CartItemRepository;
import com.example.marketplace.repository.cart.CartRepository;
import com.example.marketplace.repository.product.ProductRepository;
import com.example.marketplace.repository.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock CartRepository cartRepository;
    @Mock ProductRepository productRepository;
    @Mock CartItemRepository cartItemRepository;
    @Mock UserRepository userRepository;
    @InjectMocks
    CartService cartService;

    private Product product(long id, String price) {
        Product p = mock(Product.class);
        lenient().when(p.getId()).thenReturn(id);
        lenient().when(p.getPrice()).thenReturn(new BigDecimal(price));
        return p;
    }

    private CartItem item(Cart cart, Product p, int qty) {
        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProduct(p);
        item.setQuantity(qty);
        cart.getCartItemList().add(item);
        return item;
    }

    private CartItemRequest request(long productId, int qty) {
        CartItemRequest r = new CartItemRequest();
        r.setProductId(productId);
        r.setQuantity(qty);
        return r;
    }

    @Test
    void addToCart_newProduct_addsItemAndCountsTotal() {
        Cart cart = new Cart();
        Product p = product(1L, "10.00");
        when(cartRepository.findByUser_Id(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));

        CartResponse response = cartService.addToCart(1L, request(1L, 2));

        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getTotalPrice()).isEqualByComparingTo("20.00");
    }

    @Test
    void addToCart_existingProduct_increasesQuantity() {
        Cart cart = new Cart();
        Product p = product(1L, "10.00");
        CartItem existing = item(cart, p, 1);
        when(cartRepository.findByUser_Id(1L)).thenReturn(Optional.of(cart));

        CartResponse response = cartService.addToCart(1L, request(1L, 2));

        assertThat(existing.getQuantity()).isEqualTo(3);
        assertThat(response.getTotalPrice()).isEqualByComparingTo("30.00");
    }

    @Test
    void addToCart_unknownProduct_throwsNotFound() {
        when(cartRepository.findByUser_Id(1L)).thenReturn(Optional.of(new Cart()));
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addToCart(1L, request(99L, 1)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void removeFromCart_quantityAboveOne_decreasesByOne() {
        Cart cart = new Cart();
        CartItem existing = item(cart, product(1L, "10.00"), 3);
        when(cartRepository.findByUser_Id(1L)).thenReturn(Optional.of(cart));

        CartResponse response = cartService.removeFromCart(1L, 1L);

        assertThat(existing.getQuantity()).isEqualTo(2);
        assertThat(response.getItems()).hasSize(1);
    }

    @Test
    void removeFromCart_quantityOne_removesItem() {
        Cart cart = new Cart();
        item(cart, product(1L, "10.00"), 1);
        when(cartRepository.findByUser_Id(1L)).thenReturn(Optional.of(cart));

        CartResponse response = cartService.removeFromCart(1L, 1L);

        assertThat(cart.getCartItemList()).isEmpty();
        assertThat(response.getItems()).isEmpty();
        assertThat(response.getTotalPrice()).isEqualByComparingTo("0");
    }

    @Test
    void removeFromCart_noCart_returnsEmptyResponse() {
        when(cartRepository.findByUser_Id(1L)).thenReturn(Optional.empty());

        CartResponse response = cartService.removeFromCart(1L, 1L);

        assertThat(response.getItems()).isEmpty();
    }
}