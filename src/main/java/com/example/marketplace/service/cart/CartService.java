package com.example.marketplace.service.cart;

import com.example.marketplace.dto.cart.CartItemRequest;
import com.example.marketplace.dto.cart.CartItemResponse;
import com.example.marketplace.dto.cart.CartResponse;
import com.example.marketplace.entity.product.Product;
import com.example.marketplace.entity.cart.Cart;
import com.example.marketplace.entity.cart.CartItem;
import com.example.marketplace.repository.cart.CartItemRepository;
import com.example.marketplace.repository.cart.CartRepository;
import com.example.marketplace.repository.product.ProductRepository;
import com.example.marketplace.repository.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;

    public CartService(CartRepository cartRepository, ProductRepository productRepository, CartItemRepository cartItemRepository, UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CartResponse addToCart(Long userId, CartItemRequest cartItemRequest){
        Cart cart = cartRepository.findByUser_Id(userId).orElse(getOrCreateCart(userId));
        CartResponse cartResponse = new CartResponse();
        List<CartItemResponse> cartItemResponses = new ArrayList<>();
        BigDecimal cartTotalPrice = new BigDecimal(0);
        Boolean checkAdd = false;
        for(CartItem cartItem : cart.getCartItemList()){
            if(cartItem.getProduct().getId().equals(cartItemRequest.getProductId())){
                Integer cartItemQuentity = cartItem.getQuantity();
                cartItem.setQuantity(cartItemQuentity + cartItemRequest.getQuantity());
                checkAdd = true;
                cartItemRepository.save(cartItem);
            }
            CartItemResponse cartItemResponse = cartItemByCartItemResponse(cartItem);

            cartItemResponses.add(cartItemResponse);
            cartTotalPrice = cartTotalPrice.add(cartItemResponse.getTotalPrice());
        }
        if(!checkAdd){
            CartItem cartItem = cartItemRequstAndCartByCartItem(cartItemRequest, cart);
            CartItemResponse cartItemResponse = cartItemByCartItemResponse(cartItem);
            cartItemResponses.add(cartItemResponse);
            cartTotalPrice = cartTotalPrice.add(cartItemResponse.getTotalPrice());
            cartItemRepository.save(cartItem);
        }

        cartResponse.setItems(cartItemResponses);
        cartResponse.setTotalPrice(cartTotalPrice);
        return cartResponse;
    }

    @Transactional
    public CartResponse getCart(Long userId) {
       Cart cart = cartRepository.findByUser_Id(userId).orElse(null);
       CartResponse cartResponse = new CartResponse();
       List<CartItemResponse> cartItemResponses = new ArrayList<>();
       BigDecimal cartTotalPrice = new BigDecimal(0);

       if (cart == null) {
           cartResponse.setItems(cartItemResponses);
           cartResponse.setTotalPrice(cartTotalPrice);
           return cartResponse;
       }

       for(CartItem cartItem : cart.getCartItemList()){
           CartItemResponse cartItemResponse = cartItemByCartItemResponse(cartItem);

           cartItemResponses.add(cartItemResponse);
           cartTotalPrice = cartTotalPrice.add(cartItemResponse.getTotalPrice());
       }

       cartResponse.setItems(cartItemResponses);
       cartResponse.setTotalPrice(cartTotalPrice);
       return cartResponse;
    }

    @Transactional
    public CartResponse removeFromCart(Long userId, Long productId){
        Cart cart = cartRepository.findByUser_Id(userId).orElse(null);
        CartResponse cartResponse = new CartResponse();
        List<CartItemResponse> cartItemResponses = new ArrayList<>();
        BigDecimal cartTotalPrice = new BigDecimal(0);
        if (cart == null) {
            cartResponse.setItems(cartItemResponses);
            cartResponse.setTotalPrice(cartTotalPrice);
            return cartResponse;
        }
        for(CartItem cartItem : cart.getCartItemList()){
            if(cartItem.getProduct().getId().equals(productId)){
                Integer cartItemQuentity = cartItem.getQuantity();
                if((cartItemQuentity) == 1){
                    cartItemRepository.delete(cartItem);
                    continue;
                }
                cartItem.setQuantity(cartItemQuentity - 1);
                cartItemRepository.save(cartItem);
            }
            CartItemResponse cartItemResponse = cartItemByCartItemResponse(cartItem);

            cartItemResponses.add(cartItemResponse);
            cartTotalPrice = cartTotalPrice.add(cartItemResponse.getTotalPrice());
        }

        cartResponse.setItems(cartItemResponses);
        cartResponse.setTotalPrice(cartTotalPrice);
        return cartResponse;
    }

    @Transactional
    public CartResponse clearCart(Long userId) {
        cartRepository.findByUser_Id(userId)
                .ifPresent(cart -> cart.getCartItemList().clear());
        return new CartResponse(new ArrayList<>(), BigDecimal.ZERO);
    }

    private CartItemResponse cartItemByCartItemResponse(CartItem cartItem) {
        Product product = cartItem.getProduct();

        BigDecimal totalPrice = product.getPrice().multiply(new BigDecimal(cartItem.getQuantity()));

        CartItemResponse cartItemResponse = new CartItemResponse();
        cartItemResponse.setProductId(product.getId());
        cartItemResponse.setProductName(product.getName());
        cartItemResponse.setQuantity(cartItem.getQuantity());
        cartItemResponse.setPrice(product.getPrice());
        cartItemResponse.setTotalPrice(totalPrice);

        return cartItemResponse;
    }

    private CartItem cartItemRequstAndCartByCartItem(CartItemRequest cartItemRequest, Cart cart) {
        Product product = productRepository.findById(cartItemRequest.getProductId()).orElseThrow(() -> new NoSuchElementException("User not found product: "));

        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(cartItemRequest.getQuantity());

        return cartItem;
    }

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUser_Id(userId).orElseGet(() -> {
            Cart cart = new Cart();
            cart.setUser(userRepository.findById(userId)
                    .orElseThrow(() -> new NoSuchElementException("User not found: " + userId)));
            return cartRepository.save(cart);
        });
    }
}
