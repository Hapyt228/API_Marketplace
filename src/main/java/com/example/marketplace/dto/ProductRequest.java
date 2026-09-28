package com.example.marketplace.dto;

import com.example.marketplace.entity.Category;
import com.example.marketplace.entity.User;


import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProductRequest {
    private String name;
    private String description;
    private BigDecimal price;
    private Long categoryId;
    private UserInfoResponce seller;

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

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public UserInfoResponce getSeller() {
        return seller;
    }

    public void setSeller(UserInfoResponce seller) {
        this.seller = seller;
    }
}
