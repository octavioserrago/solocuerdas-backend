package com.solocuerdas.solocuerdas_backend.repository;

import com.solocuerdas.solocuerdas_backend.model.Inquiry;
import com.solocuerdas.solocuerdas_backend.model.InquiryStatus;
import com.solocuerdas.solocuerdas_backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    List<Inquiry> findByBuyerOrderByCreatedAtDesc(Usuario buyer);

    List<Inquiry> findBySellerOrderByCreatedAtDesc(Usuario seller);

    List<Inquiry> findBySellerAndStatusOrderByCreatedAtDesc(Usuario seller, InquiryStatus status);

    Optional<Inquiry> findByBuyerIdAndPublicationId(Long buyerId, Long publicationId);

    boolean existsByBuyerIdAndPublicationId(Long buyerId, Long publicationId);

    // Count how many OPEN or ACCEPTED inquiries a buyer has on publications of the
    // same seller
    // (used for anti-fraud rate limiting)
    @Query("SELECT COUNT(i) FROM Inquiry i WHERE i.buyer.id = :buyerId AND i.seller.id = :sellerId " +
            "AND i.status IN ('OPEN', 'ACCEPTED')")
    long countActiveInquiriesBetween(@Param("buyerId") Long buyerId, @Param("sellerId") Long sellerId);

    // Count completed transactions for anti-fraud weekly rate limit check
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.buyer.id = :userId AND t.status = 'COMPLETED' " +
            "AND t.buyerConfirmedAt >= :since")
    long countCompletedPurchasesSince(@Param("userId") Long userId,
            @Param("since") java.time.LocalDateTime since);
}
