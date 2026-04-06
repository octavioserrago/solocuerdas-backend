package com.solocuerdas.solocuerdas_backend.controller;

import com.solocuerdas.solocuerdas_backend.dto.CreateReviewRequest;
import com.solocuerdas.solocuerdas_backend.dto.ReviewResponse;
import com.solocuerdas.solocuerdas_backend.dto.UserRatingResponse;
import com.solocuerdas.solocuerdas_backend.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REVIEW CONTROLLER
 * Manages mandatory post-transaction reviews and public user ratings.
 *
 * Base paths:
 * /api/transactions/{transactionId}/review
 * /api/users/{userId}/rating
 */
@RestController
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    /**
     * SUBMIT REVIEW (mandatory after COMPLETED transaction)
     * POST /api/transactions/{transactionId}/review
     * Header: X-User-Id: <reviewerId>
     * Body: { "rating": 5, "comment": "..." }
     *
     * Must be submitted within 7 days of transaction completion.
     * Cannot be edited once submitted.
     */
    @PostMapping("/api/transactions/{transactionId}/review")
    public ResponseEntity<?> submitReview(
            @PathVariable Long transactionId,
            @RequestHeader("X-User-Id") Long reviewerId,
            @RequestBody CreateReviewRequest request) {
        try {
            ReviewResponse response = reviewService.submitReview(transactionId, reviewerId, request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * GET ALL REVIEWS RECEIVED AS SELLER
     * GET /api/users/{userId}/reviews/as-seller
     */
    @GetMapping("/api/users/{userId}/reviews/as-seller")
    public ResponseEntity<?> getSellerReviews(@PathVariable Long userId) {
        try {
            List<ReviewResponse> response = reviewService.getSellerReviews(userId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * GET ALL REVIEWS RECEIVED AS BUYER
     * GET /api/users/{userId}/reviews/as-buyer
     */
    @GetMapping("/api/users/{userId}/reviews/as-buyer")
    public ResponseEntity<?> getBuyerReviews(@PathVariable Long userId) {
        try {
            List<ReviewResponse> response = reviewService.getBuyerReviews(userId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * GET USER RATING SUMMARY (public profile)
     * GET /api/users/{userId}/rating
     *
     * Returns ratingAsSeller, ratingAsBuyer, totalSales, totalPurchases.
     */
    @GetMapping("/api/users/{userId}/rating")
    public ResponseEntity<?> getUserRating(@PathVariable Long userId) {
        try {
            UserRatingResponse response = reviewService.getUserRating(userId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
