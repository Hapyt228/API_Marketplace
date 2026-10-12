package com.example.marketplace.dto.cart;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

public class CartResponse {

    private List<CartItemResponse> items;

    @NotNull
    @Positive
    private BigDecimal totalPrice;

    public CartResponse(List<CartItemResponse> items,  BigDecimal totalPrice) {
        this.items = items;
        this.totalPrice = totalPrice;
    }

    public CartResponse() {
    }

    public List<CartItemResponse> getItems() {
        return items;
    }

    public void setItems(List<CartItemResponse> items) {
        this.items = items;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
}
