package com.solocuerdas.solocuerdas_backend.dto;

import com.solocuerdas.solocuerdas_backend.model.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionResponse {

    private Long id;
    private Long inquiryId;
    private Long buyerId;
    private String buyerName;
    private Long sellerId;
    private String sellerName;
    private Long publicationId;
    private String publicationTitle;
    private BigDecimal agreedPrice;
    private TransactionStatus status;
    private LocalDateTime codeExpiresAt;
    private LocalDateTime sellerConfirmedAt;
    private LocalDateTime buyerConfirmedAt;
    private LocalDateTime createdAt;
    // confirmationCode is only included in the seller's own response for security
    private String confirmationCode;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getInquiryId() {
        return inquiryId;
    }

    public void setInquiryId(Long inquiryId) {
        this.inquiryId = inquiryId;
    }

    public Long getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(Long buyerId) {
        this.buyerId = buyerId;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public Long getPublicationId() {
        return publicationId;
    }

    public void setPublicationId(Long publicationId) {
        this.publicationId = publicationId;
    }

    public String getPublicationTitle() {
        return publicationTitle;
    }

    public void setPublicationTitle(String publicationTitle) {
        this.publicationTitle = publicationTitle;
    }

    public BigDecimal getAgreedPrice() {
        return agreedPrice;
    }

    public void setAgreedPrice(BigDecimal agreedPrice) {
        this.agreedPrice = agreedPrice;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getConfirmationCode() {
        return confirmationCode;
    }

    public void setConfirmationCode(String confirmationCode) {
        this.confirmationCode = confirmationCode;
    }
}
