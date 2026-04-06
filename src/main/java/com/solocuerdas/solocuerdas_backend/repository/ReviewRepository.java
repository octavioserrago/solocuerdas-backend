package com.solocuerdas.solocuerdas_backend.repository;

import com.solocuerdas.solocuerdas_backend.model.Review;
import com.solocuerdas.solocuerdas_backend.model.ReviewType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByTransactionIdAndReviewerId(Long transactionId, Long reviewerId);

    boolean existsByTransactionIdAndReviewerId(Long transactionId, Long reviewerId);

    List<Review> findByReviewedIdAndTypeOrderByCreatedAtDesc(Long reviewedId, ReviewType type);

    // Average rating as seller
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.reviewed.id = :userId AND r.type = 'BUYER_REVIEWS_SELLER'")
    Double averageRatingAsSeller(@Param("userId") Long userId);

    // Average rating as buyer
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.reviewed.id = :userId AND r.type = 'SELLER_REVIEWS_BUYER'")
    Double averageRatingAsBuyer(@Param("userId") Long userId);

    long countByTransactionId(Long transactionId);
}
