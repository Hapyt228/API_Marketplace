package com.example.marketplace.dto.review;

import java.time.LocalDateTime;

public class ReviewResponse {

    private Long id;
    private Long authorId;
    private Long productId;
    private Integer rating;
    private String text;
    private LocalDateTime createdAt;

    public ReviewResponse(Long id, Long authorId, Long productId, Integer rating, String text, LocalDateTime createdAt) {
        this.id = id;
        this.authorId = authorId;
        this.productId = productId;
        this.rating = rating;
        this.text = text;
        this.createdAt = createdAt;
    }

    public ReviewResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
