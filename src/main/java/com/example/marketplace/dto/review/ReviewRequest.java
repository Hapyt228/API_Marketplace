package com.example.marketplace.dto.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ReviewRequest {

    @Min(1)
    @Max(5)
    @NotNull
    private Integer rating;

    @Size(max = 255)
    private String text;

    public ReviewRequest(Integer rating, String text) {
        this.rating = rating;
        this.text = text;
    }

    public ReviewRequest() {
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
