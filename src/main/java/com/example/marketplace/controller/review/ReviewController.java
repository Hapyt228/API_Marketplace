package com.example.marketplace.controller.review;

import com.example.marketplace.dto.review.ReviewRequest;
import com.example.marketplace.dto.review.ReviewResponse;
import com.example.marketplace.entity.user.User;
import com.example.marketplace.service.review.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/products/{productId}/reviews")
    public ResponseEntity<ReviewResponse> createReview(
            @PathVariable("productId") Long productId,
            @RequestBody @Valid ReviewRequest reviewRequest,
            @AuthenticationPrincipal User user
    ){
        return ResponseEntity.status(HttpStatus.CREATED).body(
                reviewService.createReview(productId, reviewRequest, user.getId())
        );
    }

    @GetMapping("/products/{productId}/reviews")
    public ResponseEntity<List<ReviewResponse>> getReviews(
            @PathVariable("productId") Long productId
    ){
            return ResponseEntity.status(HttpStatus.OK).body(reviewService.getReviews(productId));
    }

    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable("id") Long reviewId,
            @AuthenticationPrincipal User user
    ){
            reviewService.deleteReview(reviewId, user.getId());
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
