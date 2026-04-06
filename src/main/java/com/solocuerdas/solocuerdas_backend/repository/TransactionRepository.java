package com.solocuerdas.solocuerdas_backend.repository;

import com.solocuerdas.solocuerdas_backend.model.Transaction;
import com.solocuerdas.solocuerdas_backend.model.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByInquiryId(Long inquiryId);

    boolean existsByPublicationIdAndStatusIn(Long publicationId, List<TransactionStatus> statuses);

    List<Transaction> findByBuyerIdOrderByCreatedAtDesc(Long buyerId);

    List<Transaction> findBySellerIdOrderByCreatedAtDesc(Long sellerId);

    // Anti-fraud: count completed transactions between two users in the last 30
    // days
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.buyer.id = :buyerId AND t.seller.id = :sellerId " +
            "AND t.status = 'COMPLETED' AND t.buyerConfirmedAt >= :since")
    long countCompletedBetweenUsers(@Param("buyerId") Long buyerId,
            @Param("sellerId") Long sellerId,
            @Param("since") LocalDateTime since);

    // Anti-fraud: count completed transactions as buyer in the last 7 days
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.buyer.id = :userId " +
            "AND t.status = 'COMPLETED' AND t.buyerConfirmedAt >= :since")
    long countCompletedAsBuyerSince(@Param("userId") Long userId,
            @Param("since") LocalDateTime since);
}
