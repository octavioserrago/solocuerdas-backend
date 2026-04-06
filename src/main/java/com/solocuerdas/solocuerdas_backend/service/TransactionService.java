package com.solocuerdas.solocuerdas_backend.service;

import com.solocuerdas.solocuerdas_backend.dto.InitiateTransactionRequest;
import com.solocuerdas.solocuerdas_backend.dto.TransactionResponse;
import com.solocuerdas.solocuerdas_backend.model.*;
import com.solocuerdas.solocuerdas_backend.repository.PublicationRepository;
import com.solocuerdas.solocuerdas_backend.repository.TransactionRepository;
import com.solocuerdas.solocuerdas_backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private static final int MAX_WEEKLY_COMPLETED = 3;
    private static final int MAX_SAME_USER_MONTHLY = 1;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private PublicationRepository publicationRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private InquiryService inquiryService;

    @Autowired
    private PushNotificationService pushNotificationService;

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * INITIATE TRANSACTION (seller action)
     * Seller confirms they want to close the sale.
     * Generates a 6-digit code that the buyer must enter in person.
     */
    public TransactionResponse initiateTransaction(Long inquiryId, Long sellerId,
            InitiateTransactionRequest request) {
        Inquiry inquiry = inquiryService.findAndValidateSeller(inquiryId, sellerId);

        if (inquiry.getStatus() != InquiryStatus.ACCEPTED) {
            throw new RuntimeException("Transaction can only be initiated for accepted inquiries.");
        }

        // Check no active/completed transaction already exists for this publication
        if (transactionRepository.existsByPublicationIdAndStatusIn(
                inquiry.getPublication().getId(),
                List.of(TransactionStatus.AWAITING_BUYER_CODE, TransactionStatus.COMPLETED))) {
            throw new RuntimeException("This publication already has an active or completed transaction.");
        }

        // Anti-fraud: weekly rate limit for buyer
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        long buyerWeeklyCount = transactionRepository.countCompletedAsBuyerSince(
                inquiry.getBuyer().getId(), oneWeekAgo);
        if (buyerWeeklyCount >= MAX_WEEKLY_COMPLETED) {
            throw new RuntimeException(
                    "This buyer has reached the maximum completed transactions for this week.");
        }

        // Anti-fraud: max 1 completed transaction between same buyer/seller in 30 days
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        long sameUserCount = transactionRepository.countCompletedBetweenUsers(
                inquiry.getBuyer().getId(), sellerId, thirtyDaysAgo);
        if (sameUserCount >= MAX_SAME_USER_MONTHLY) {
            throw new RuntimeException(
                    "Maximum transactions between the same buyer and seller per month reached.");
        }

        BigDecimal agreedPrice = request.getAgreedPrice() != null
                ? request.getAgreedPrice()
                : inquiry.getPublication().getPrice();

        String code = generateCode();

        Transaction transaction = new Transaction(
                inquiry,
                inquiry.getBuyer(),
                inquiry.getSeller(),
                inquiry.getPublication(),
                agreedPrice,
                code);

        Transaction saved = transactionRepository.save(transaction);

        // Notify buyer to prepare — do NOT include the code in the notification
        pushNotificationService.send(inquiry.getBuyer(),
                "¡Reunión confirmada!",
                inquiry.getSeller().getName() + " está listo para cerrar la venta de \""
                        + inquiry.getPublication().getTitle()
                        + "\". Coordinaron el encuentro, mostrá el código cuando se junten.");

        // Seller sees the code so they can show it to the buyer in person
        TransactionResponse response = mapToResponse(saved);
        response.setConfirmationCode(code);
        return response;
    }

    /**
     * CONFIRM TRANSACTION (buyer action)
     * Buyer enters the code they saw on the seller's screen in person.
     * Marks publication as SOLD and flags both users for pending review.
     */
    public TransactionResponse confirmTransaction(Long transactionId, Long buyerId, String code) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (!transaction.getBuyer().getId().equals(buyerId)) {
            throw new RuntimeException("You are not the buyer of this transaction.");
        }

        if (transaction.getStatus() != TransactionStatus.AWAITING_BUYER_CODE) {
            throw new RuntimeException("This transaction is not awaiting confirmation.");
        }

        if (LocalDateTime.now().isAfter(transaction.getCodeExpiresAt())) {
            transaction.setStatus(TransactionStatus.EXPIRED);
            transactionRepository.save(transaction);
            throw new RuntimeException("Confirmation code has expired. The transaction was cancelled.");
        }

        if (!transaction.getConfirmationCode().equals(code)) {
            throw new RuntimeException("Invalid confirmation code.");
        }

        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setBuyerConfirmedAt(LocalDateTime.now());
        transactionRepository.save(transaction);

        // Mark publication as SOLD
        Publication publication = transaction.getPublication();
        publication.setStatus(PublicationStatus.SOLD);
        publication.markAsSold();
        publicationRepository.save(publication);

        // Close the inquiry
        inquiryService.closeInquiry(transaction.getInquiry());

        // Flag both users as having a pending review
        Usuario seller = usuarioRepository.findById(transaction.getSeller().getId()).orElseThrow();
        Usuario buyer = usuarioRepository.findById(buyerId).orElseThrow();
        seller.setHasPendingReview(true);
        buyer.setHasPendingReview(true);
        seller.setTotalSales(seller.getTotalSales() + 1);
        buyer.setTotalPurchases(buyer.getTotalPurchases() + 1);
        usuarioRepository.save(seller);
        usuarioRepository.save(buyer);

        pushNotificationService.send(seller,
                "Venta confirmada ✅",
                buyer.getName() + " confirmó la compra de \"" + transaction.getPublication().getTitle()
                        + "\". Dejá tu reseña en los próximos 7 días.");
        pushNotificationService.send(buyer,
                "Compra confirmada ✅",
                "¡Compraste \"" + transaction.getPublication().getTitle()
                        + "\". Dejá tu reseña en los próximos 7 días.");

        return mapToResponse(transaction);
    }

    /**
     * CANCEL TRANSACTION (seller cancels before buyer confirms)
     */
    public TransactionResponse cancelTransaction(Long transactionId, Long sellerId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (!transaction.getSeller().getId().equals(sellerId)) {
            throw new RuntimeException("You are not the seller of this transaction.");
        }

        if (transaction.getStatus() != TransactionStatus.AWAITING_BUYER_CODE) {
            throw new RuntimeException("This transaction cannot be cancelled.");
        }

        transaction.setStatus(TransactionStatus.CANCELLED);
        return mapToResponse(transactionRepository.save(transaction));
    }

    /**
     * GET TRANSACTION BY INQUIRY ID
     */
    public TransactionResponse getByInquiry(Long inquiryId, Long requesterId) {
        Transaction transaction = transactionRepository.findByInquiryId(inquiryId)
                .orElseThrow(() -> new RuntimeException("No transaction found for this inquiry"));

        if (!transaction.getBuyer().getId().equals(requesterId) &&
                !transaction.getSeller().getId().equals(requesterId)) {
            throw new RuntimeException("Access denied.");
        }

        TransactionResponse response = mapToResponse(transaction);

        // Only the seller sees their own code (to show in person)
        if (transaction.getSeller().getId().equals(requesterId)
                && transaction.getStatus() == TransactionStatus.AWAITING_BUYER_CODE) {
            response.setConfirmationCode(transaction.getConfirmationCode());
        }

        return response;
    }

    // ============ HELPERS ============

    private String generateCode() {
        int code = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(code);
    }

    private TransactionResponse mapToResponse(Transaction t) {
        TransactionResponse r = new TransactionResponse();
        r.setId(t.getId());
        r.setInquiryId(t.getInquiry().getId());
        r.setBuyerId(t.getBuyer().getId());
        r.setBuyerName(t.getBuyer().getName());
        r.setSellerId(t.getSeller().getId());
        r.setSellerName(t.getSeller().getName());
        r.setPublicationId(t.getPublication().getId());
        r.setPublicationTitle(t.getPublication().getTitle());
        r.setAgreedPrice(t.getAgreedPrice());
        r.setStatus(t.getStatus());
        r.setCodeExpiresAt(t.getCodeExpiresAt());
        r.setSellerConfirmedAt(t.getSellerConfirmedAt());
        r.setBuyerConfirmedAt(t.getBuyerConfirmedAt());
        r.setCreatedAt(t.getCreatedAt());
        return r;
    }
}
