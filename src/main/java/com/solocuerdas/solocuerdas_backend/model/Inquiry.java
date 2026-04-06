package com.solocuerdas.solocuerdas_backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * INQUIRY ENTITY
 * Represents a buyer's interest in a publication.
 * Once accepted by the seller, enables the chat.
 */
@Entity
@Table(name = "inquiries", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "buyer_id", "publication_id" })
})
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private Usuario buyer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Usuario seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publication_id", nullable = false)
    private Publication publication;

    @Column(name = "message", length = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InquiryStatus status = InquiryStatus.OPEN;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Inquiry() {
    }

    public Inquiry(Usuario buyer, Usuario seller, Publication publication, String message) {
        this.buyer = buyer;
        this.seller = seller;
        this.publication = publication;
        this.message = message;
        this.status = InquiryStatus.OPEN;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null)
            createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ============ GETTERS AND SETTERS ============

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public InquiryStatus getStatus() {
        return status;
    }

    public void setStatus(InquiryStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
