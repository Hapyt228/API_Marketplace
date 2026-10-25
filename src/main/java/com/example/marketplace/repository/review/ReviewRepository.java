package com.example.marketplace.repository.review;

import com.example.marketplace.entity.review.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByUser_IdAndProduct_Id(Long userId, Long productId);

    List<Review> findAllByProduct_IdOrderByCreatedAtDesc(Long productId);

    @Query("""
            SELECT COALESCE(AVG(r.rating), 0.0) AS averageRating,
                   COUNT(r) AS reviewsCount
            FROM Review r
            WHERE r.product.id = :productId
            """)
    ReviewStatistics getStatisticsByProductId(
            @Param("productId") Long productId
    );

}
