package com.solocuerdas.solocuerdas_backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * TRANSACTION ENTITY
 * Created when the seller confirms a sale.
 * Requires both parties to confirm via a one-time code (shown in person).
 * Anti-fraud: code expires in 24h, only one transaction per publication.
 */
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquiry_id", nullable = false, unique = true)
    private Inquiry inquiry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private Usuario buyer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Usuario seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publication_id", nullable = false)
    private Publication publication;

    @Column(name = "agreed_price", precision = 10, scale = 2)
    private BigDecimal agreedPrice;

    @Column(name = "confirmation_code", nullable = false, length = 6)
    private String confirmationCode;

    @Column(name = "code_expires_at", nullable = false)
    private LocalDateTime codeExpiresAt;

    @Column(name = "seller_confirmed_at", nullable = false)
    private LocalDateTime sellerConfirmedAt;

    @Column(name = "buyer_confirmed_at")
    private LocalDateTime buyerConfirmedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private TransactionStatus status = TransactionStatus.AWAITING_BUYER_CODE;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Transaction() {
    }

    public Transaction(Inquiry inquiry, Usuario buyer, Usuario seller,
            Publication publication, BigDecimal agreedPrice, String confirmationCode) {
        this.inquiry = inquiry;
        this.buyer = buyer;
        this.seller = seller;
        this.publication = publication;
        this.agreedPrice = agreedPrice;
        this.confirmationCode = confirmationCode;
        this.status = TransactionStatus.AWAITING_BUYER_CODE;
        this.sellerConfirmedAt = LocalDateTime.now();
        this.codeExpiresAt = LocalDateTime.now().plusHours(24);
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null)
            createdAt = LocalDateTime.now();
    }

    // ============ GETTERS AND SETTERS ============

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Inquiry getInquiry() {
        return inquiry;
    }

    public void setInquiry(Inquiry inquiry) {
        this.inquiry = inquiry;
    }

    public Usuario getBuyer() {
        return buyer;
    }

    public void setBuyer(Usuario buyer) {
        this.buyer = buyer;
    }

    public Usuario getSeller() {
        return seller;
    }

    public void setSeller(Usuario seller) {
        this.seller = seller;
    }

    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    public BigDecimal getAgreedPrice() {
        return agreedPrice;
    }

    public void setAgreedPrice(BigDecimal agreedPrice) {
        this.agreedPrice = agreedPrice;
    }

    public String getConfirmationCode() {
        return confirmationCode;
    }

    public void setConfirmationCode(String confirmationCode) {
        this.confirmationCode = confirmationCode;
    }

    public LocalDateTime getCodeExpiresAt() {
        return codeExpiresAt;
    }

    public void setCodeExpiresAt(LocalDateTime codeExpiresAt) {
        this.codeExpiresAt = codeExpiresAt;
    }

    public LocalDateTime getSellerConfirmedAt() {
        return sellerConfirmedAt;
    }

    public void setSellerConfirmedAt(LocalDateTime sellerConfirmedAt) {
        this.sellerConfirmedAt = sellerConfirmedAt;
    }

    public LocalDateTime getBuyerConfirmedAt() {
        return buyerConfirmedAt;
    }

    public void setBuyerConfirmedAt(LocalDateTime buyerConfirmedAt) {
        this.buyerConfirmedAt = buyerConfirmedAt;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
