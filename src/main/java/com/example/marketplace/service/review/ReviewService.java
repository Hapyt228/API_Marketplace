package com.example.marketplace.service.review;

import com.example.marketplace.dto.review.ReviewRequest;
import com.example.marketplace.dto.review.ReviewResponse;
import com.example.marketplace.entity.product.Product;
import com.example.marketplace.entity.review.Review;
import com.example.marketplace.entity.user.Role;
import com.example.marketplace.entity.user.User;
import com.example.marketplace.exception.ResourceNotFoundException;
import com.example.marketplace.exception.ReviewAlreadyExistsException;
import com.example.marketplace.repository.product.ProductRepository;
import com.example.marketplace.repository.review.ReviewRepository;
import com.example.marketplace.repository.user.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ReviewService(ReviewRepository reviewRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public ReviewResponse createReview(Long productId, ReviewRequest reviewRequest, Long userId) {
         Product product = productRepository.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Not found by id = " + productId));
         User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Not found by id = " + userId));
         if(product.getSeller().getId().equals(userId)){
             throw new AccessDeniedException("Seller not make review");
         }
         if (reviewRepository.existsByUser_IdAndProduct_Id(userId, productId)) {
             throw new ReviewAlreadyExistsException("You make 1 review");
         }
         Review review = new Review();
         review.setProduct(product);
         review.setUser(user);
         review.setRating(reviewRequest.getRating());
         review.setText(reviewRequest.getText());
         reviewRepository.save(review);
         return reviewToReviewResponse(review);
    }

    public List<ReviewResponse> getReviews(Long productId) {
        if(productRepository.findById(productId).isEmpty()) {
            throw new ResourceNotFoundException("Product not found");
        }
        List<Review> reviewsList = reviewRepository.findAllByProduct_IdOrderByCreatedAtDesc(productId);
        List<ReviewResponse> reviewResponseList = new ArrayList<>();
        for(Review review : reviewsList){
            reviewResponseList.add(reviewToReviewResponse(review));
        }
        return reviewResponseList;
    }

    public void deleteReview(Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if(!(review.getUser().getId().equals(userId ) || user.getRole().equals(Role.ADMIN))){
            throw new AccessDeniedException("You don't delete this review");
        }
        reviewRepository.delete(review);
    }


    private ReviewResponse reviewToReviewResponse(Review review){
        ReviewResponse reviewResponse = new ReviewResponse();
        reviewResponse.setId(review.getId());
        reviewResponse.setAuthorId(review.getUser().getId());
        reviewResponse.setProductId(review.getProduct().getId());
        reviewResponse.setText(review.getText());
        reviewResponse.setRating(review.getRating());
        reviewResponse.setCreatedAt(review.getCreatedAt());
        return reviewResponse;
    }

}
