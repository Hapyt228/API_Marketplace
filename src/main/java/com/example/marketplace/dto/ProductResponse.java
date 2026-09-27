package com.example.marketplace.dto;

import com.example.marketplace.entity.Category;
import com.example.marketplace.entity.Product;
import com.example.marketplace.entity.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProductResponse {
    private String name;
    private String description;
    private BigDecimal price;
    private CategoryResponse category;
    private UserInfoResponce seller;
    private LocalDateTime createdAt;

    public ProductResponse() {
    }


    public ProductResponse(Product product) {
        this.name = product.getName();
        this.description = product.getDescription();
        this.price = product.getPrice();
        this.category = new CategoryResponse(product.getCategory());
        this.seller = new UserInfoResponce(product.getSeller());
        this.createdAt = product.getCreatedAt();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public CategoryResponse getCategory() {
        return category;
    }

    public void setCategory(CategoryResponse category) {
        this.category = category;
    }

    public UserInfoResponce getSeller() {
        return seller;
    }

    public void setSeller(UserInfoResponce seller) {
        this.seller = seller;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
