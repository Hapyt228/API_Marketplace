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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock ReviewRepository reviewRepository;
    @Mock ProductRepository productRepository;
    @Mock UserRepository userRepository;
    @InjectMocks ReviewService reviewService;

    private Product productSoldBy(long sellerId) {
        User seller = mock(User.class);
        when(seller.getId()).thenReturn(sellerId);
        Product product = mock(Product.class);
        when(product.getSeller()).thenReturn(seller);
        return product;
    }

    private Review reviewBy(User author, Product product, int rating) {
        Review review = new Review();
        review.setUser(author);
        review.setProduct(product);
        review.setRating(rating);
        review.setText("text");
        return review;
    }

    @Test
    void createReview_unknownProduct_throwsNotFound() {
        when(productRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.createReview(5L, new ReviewRequest(5, "ok"), 2L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReview_unknownUser_throwsNotFound() {
        when(productRepository.findById(5L)).thenReturn(Optional.of(mock(Product.class)));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.createReview(5L, new ReviewRequest(5, "ok"), 2L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReview_sellerOfProduct_throwsAccessDenied() {
        Product product = productSoldBy(1L);
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));
        when(userRepository.findById(1L)).thenReturn(Optional.of(mock(User.class)));

        assertThatThrownBy(() -> reviewService.createReview(5L, new ReviewRequest(5, "ok"), 1L))
                .isInstanceOf(AccessDeniedException.class);
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReview_duplicate_throwsAlreadyExists() {
        Product product = productSoldBy(1L);
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));
        when(userRepository.findById(2L)).thenReturn(Optional.of(mock(User.class)));
        when(reviewRepository.existsByUser_IdAndProduct_Id(2L, 5L)).thenReturn(true);

        assertThatThrownBy(() -> reviewService.createReview(5L, new ReviewRequest(5, "ok"), 2L))
                .isInstanceOf(ReviewAlreadyExistsException.class);
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReview_valid_savesReviewAndReturnsResponse() {
        Product product = productSoldBy(1L);
        when(product.getId()).thenReturn(5L);
        User buyer = mock(User.class);
        when(buyer.getId()).thenReturn(2L);
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));
        when(userRepository.findById(2L)).thenReturn(Optional.of(buyer));
        when(reviewRepository.existsByUser_IdAndProduct_Id(2L, 5L)).thenReturn(false);

        ReviewResponse response = reviewService.createReview(5L, new ReviewRequest(4, "Nice"), 2L);

        ArgumentCaptor<Review> captor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(captor.capture());
        Review saved = captor.getValue();
        assertThat(saved.getRating()).isEqualTo(4);
        assertThat(saved.getText()).isEqualTo("Nice");
        assertThat(saved.getUser()).isSameAs(buyer);
        assertThat(saved.getProduct()).isSameAs(product);

        assertThat(response.getRating()).isEqualTo(4);
        assertThat(response.getText()).isEqualTo("Nice");
        assertThat(response.getAuthorId()).isEqualTo(2L);
        assertThat(response.getProductId()).isEqualTo(5L);
    }

    @Test
    void getReviews_unknownProduct_throwsNotFound() {
        when(productRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.getReviews(5L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getReviews_returnsMappedListInRepositoryOrder() {
        Product product = mock(Product.class);
        when(product.getId()).thenReturn(5L);
        User author = mock(User.class);
        when(author.getId()).thenReturn(2L);
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));
        when(reviewRepository.findAllByProduct_IdOrderByCreatedAtDesc(5L))
                .thenReturn(List.of(reviewBy(author, product, 5), reviewBy(author, product, 2)));

        List<ReviewResponse> result = reviewService.getReviews(5L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getRating()).isEqualTo(5);
        assertThat(result.get(1).getRating()).isEqualTo(2);
        assertThat(result.get(0).getProductId()).isEqualTo(5L);
    }

    @Test
    void deleteReview_unknownReview_throwsNotFound() {
        when(reviewRepository.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.deleteReview(7L, 2L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(reviewRepository, never()).delete(any());
    }

    @Test
    void deleteReview_unknownUser_throwsNotFound() {
        Review review = reviewBy(mock(User.class), mock(Product.class), 5);
        when(reviewRepository.findById(7L)).thenReturn(Optional.of(review));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.deleteReview(7L, 2L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(reviewRepository, never()).delete(any());
    }

    @Test
    void deleteReview_foreignUser_throwsAccessDenied() {
        User author = mock(User.class);
        when(author.getId()).thenReturn(1L);
        Review review = reviewBy(author, mock(Product.class), 5);
        User stranger = mock(User.class);
        when(stranger.getRole()).thenReturn(Role.USER);
        when(reviewRepository.findById(7L)).thenReturn(Optional.of(review));
        when(userRepository.findById(2L)).thenReturn(Optional.of(stranger));

        assertThatThrownBy(() -> reviewService.deleteReview(7L, 2L))
                .isInstanceOf(AccessDeniedException.class);
        verify(reviewRepository, never()).delete(any());
    }

    @Test
    void deleteReview_author_deletes() {
        User author = mock(User.class);
        when(author.getId()).thenReturn(1L);
        Review review = reviewBy(author, mock(Product.class), 5);
        when(reviewRepository.findById(7L)).thenReturn(Optional.of(review));
        when(userRepository.findById(1L)).thenReturn(Optional.of(author));

        reviewService.deleteReview(7L, 1L);

        verify(reviewRepository).delete(review);
    }

    @Test
    void deleteReview_admin_deletesForeignReview() {
        User author = mock(User.class);
        when(author.getId()).thenReturn(1L);
        Review review = reviewBy(author, mock(Product.class), 5);
        User admin = mock(User.class);
        when(admin.getRole()).thenReturn(Role.ADMIN);
        when(reviewRepository.findById(7L)).thenReturn(Optional.of(review));
        when(userRepository.findById(2L)).thenReturn(Optional.of(admin));

        reviewService.deleteReview(7L, 2L);

        verify(reviewRepository).delete(review);
    }
}