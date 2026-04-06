package com.solocuerdas.solocuerdas_backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * REVIEW ENTITY
 * Created after a COMPLETED transaction. Both parties must review each other.
 * Mandatory. Rating is 1-5 stars. Non-editable after submission.
 */
@Entity
@Table(name = "reviews", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "transaction_id", "reviewer_id" })
})
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private Usuario reviewer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_id", nullable = false)
    private Usuario reviewed;

    @Column(name = "rating", nullable = false)
    private Integer rating; // 1-5

    @Column(name = "comment", nullable = false, length = 1000)
    private String comment;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private ReviewType type;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Review() {
    }

    public Review(Transaction transaction, Usuario reviewer, Usuario reviewed,
            Integer rating, String comment, ReviewType type) {
        this.transaction = transaction;
        this.reviewer = reviewer;
        this.reviewed = reviewed;
        this.rating = rating;
        this.comment = comment;
        this.type = type;
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

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public Usuario getReviewer() {
        return reviewer;
    }

    public void setReviewer(Usuario reviewer) {
        this.reviewer = reviewer;
    }

    public Usuario getReviewed() {
        return reviewed;
    }

    public void setReviewed(Usuario reviewed) {
        this.reviewed = reviewed;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public ReviewType getType() {
        return type;
    }

    public void setType(ReviewType type) {
        this.type = type;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
