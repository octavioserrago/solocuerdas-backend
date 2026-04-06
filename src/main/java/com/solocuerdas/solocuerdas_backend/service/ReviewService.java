package com.solocuerdas.solocuerdas_backend.service;

import com.solocuerdas.solocuerdas_backend.dto.CreateReviewRequest;
import com.solocuerdas.solocuerdas_backend.dto.ReviewResponse;
import com.solocuerdas.solocuerdas_backend.dto.UserRatingResponse;
import com.solocuerdas.solocuerdas_backend.model.*;
import com.solocuerdas.solocuerdas_backend.repository.ReviewRepository;
import com.solocuerdas.solocuerdas_backend.repository.TransactionRepository;
import com.solocuerdas.solocuerdas_backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private static final int REVIEW_WINDOW_DAYS = 7;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PushNotificationService pushNotificationService;

    /**
     * SUBMIT REVIEW (mandatory after a COMPLETED transaction)
     * Buyer reviews seller or seller reviews buyer.
     * Once submitted it cannot be edited.
     * After both parties submit their reviews, hasPendingReview is cleared.
     */
    public ReviewResponse submitReview(Long transactionId, Long reviewerId, CreateReviewRequest request) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (transaction.getStatus() != TransactionStatus.COMPLETED) {
            throw new RuntimeException("Reviews are only allowed for completed transactions.");
        }

        if (reviewRepository.existsByTransactionIdAndReviewerId(transactionId, reviewerId)) {
            throw new RuntimeException("You have already submitted your review for this transaction.");
        }

        // Check 7-day review window
        LocalDateTime reviewDeadline = transaction.getBuyerConfirmedAt().plusDays(REVIEW_WINDOW_DAYS);
        if (LocalDateTime.now().isAfter(reviewDeadline)) {
            throw new RuntimeException("The review period for this transaction has closed.");
        }

        boolean isBuyer = transaction.getBuyer().getId().equals(reviewerId);
        boolean isSeller = transaction.getSeller().getId().equals(reviewerId);
        if (!isBuyer && !isSeller) {
            throw new RuntimeException("You are not a participant in this transaction.");
        }

        validateReviewRequest(request);

        Usuario reviewer = usuarioRepository.findById(reviewerId).orElseThrow();
        Usuario reviewed = isBuyer ? transaction.getSeller() : transaction.getBuyer();
        ReviewType type = isBuyer ? ReviewType.BUYER_REVIEWS_SELLER : ReviewType.SELLER_REVIEWS_BUYER;

        Review review = new Review(transaction, reviewer, reviewed, request.getRating(), request.getComment(), type);
        reviewRepository.save(review);

        // Notify reviewed user that they got a new review
        pushNotificationService.send(reviewed,
                "Recibió una nueva reseña",
                reviewer.getName() + " te dejó una reseña de " + request.getRating() + " estrella"
                        + (request.getRating() == 1 ? "" : "s") + ".");

        // Recalculate average rating for the reviewed user
        recalculateRating(reviewed);

        // Clear pending flag for the reviewer immediately — they already submitted
        clearPendingReview(reviewer);

        // If both reviews are now submitted, clear the other party too
        long reviewCount = reviewRepository.countByTransactionId(transactionId);
        if (reviewCount >= 2) {
            clearPendingReview(transaction.getBuyer());
            clearPendingReview(transaction.getSeller());
        } else {
            // Notify the other party that they still need to submit their review
            Usuario pending = reviewerId.equals(transaction.getBuyer().getId())
                    ? transaction.getSeller()
                    : transaction.getBuyer();
            pushNotificationService.send(pending,
                    "Reseña pendiente",
                    reviewer.getName() + " ya dejó su reseña. Tiene hasta " + REVIEW_WINDOW_DAYS
                            + " días para enviar la tuya.");
        }

        return mapToResponse(review);
    }

    /**
     * GET REVIEWS RECEIVED AS SELLER
     */
    public List<ReviewResponse> getSellerReviews(Long userId) {
        return reviewRepository
                .findByReviewedIdAndTypeOrderByCreatedAtDesc(userId, ReviewType.BUYER_REVIEWS_SELLER)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    /**
     * GET REVIEWS RECEIVED AS BUYER
     */
    public List<ReviewResponse> getBuyerReviews(Long userId) {
        return reviewRepository
                .findByReviewedIdAndTypeOrderByCreatedAtDesc(userId, ReviewType.SELLER_REVIEWS_BUYER)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    /**
     * GET USER RATING SUMMARY (displayed publicly on the profile)
     */
    public UserRatingResponse getUserRating(Long userId) {
        Usuario user = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserRatingResponse r = new UserRatingResponse();
        r.setUserId(user.getId());
        r.setUserName(user.getName());
        r.setRatingAsSeller(user.getRatingAsSeller());
        r.setTotalSales(user.getTotalSales());
        r.setRatingAsBuyer(user.getRatingAsBuyer());
        r.setTotalPurchases(user.getTotalPurchases());
        return r;
    }

    // ============ HELPERS ============

    private void validateReviewRequest(CreateReviewRequest request) {
        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new RuntimeException("Rating must be between 1 and 5.");
        }
        if (request.getComment() == null || request.getComment().trim().isEmpty()) {
            throw new RuntimeException("Comment is required.");
        }
    }

    private void recalculateRating(Usuario user) {
        Double avgSeller = reviewRepository.averageRatingAsSeller(user.getId());
        Double avgBuyer = reviewRepository.averageRatingAsBuyer(user.getId());

        if (avgSeller != null) {
            user.setRatingAsSeller(BigDecimal.valueOf(avgSeller).setScale(2, RoundingMode.HALF_UP));
        }
        if (avgBuyer != null) {
            user.setRatingAsBuyer(BigDecimal.valueOf(avgBuyer).setScale(2, RoundingMode.HALF_UP));
        }
        usuarioRepository.save(user);
    }

    private void clearPendingReview(Usuario user) {
        user.setHasPendingReview(false);
        usuarioRepository.save(user);
    }

    private ReviewResponse mapToResponse(Review r) {
        ReviewResponse res = new ReviewResponse();
        res.setId(r.getId());
        res.setTransactionId(r.getTransaction().getId());
        res.setReviewerId(r.getReviewer().getId());
        res.setReviewerName(r.getReviewer().getName());
        res.setReviewedId(r.getReviewed().getId());
        res.setReviewedName(r.getReviewed().getName());
        res.setRating(r.getRating());
        res.setComment(r.getComment());
        res.setType(r.getType());
        res.setCreatedAt(r.getCreatedAt());
        return res;
    }
}
