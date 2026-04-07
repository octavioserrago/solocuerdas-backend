package com.solocuerdas.solocuerdas_backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * USER ENTITY
 * Represents a user in the musical instruments marketplace
 */
@Entity
@Table(name = "users")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "registration_date", nullable = false)
    private LocalDateTime registrationDate;

    // ============ SUBSCRIPTION FIELDS ============

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_plan", nullable = false, length = 20)
    private SubscriptionPlan subscriptionPlan = SubscriptionPlan.FREE;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_status", nullable = false, length = 20)
    private SubscriptionStatus subscriptionStatus = SubscriptionStatus.NONE;

    @Column(name = "subscription_start_date")
    private LocalDateTime subscriptionStartDate;

    @Column(name = "subscription_end_date")
    private LocalDateTime subscriptionEndDate;

    @Column(name = "grace_period_end_date")
    private LocalDateTime gracePeriodEndDate;

    @Column(name = "extra_posts_purchased", nullable = false)
    private Integer extraPostsPurchased = 0;

    // ============ ROLE AND PERMISSIONS ============

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role = Role.USER; // Default role

    // ============ SUSPENSION FIELDS ============

    @Column(name = "is_suspended", nullable = false)
    private Boolean isSuspended = false;

    @Column(name = "suspended_until")
    private LocalDateTime suspendedUntil;

    @Column(name = "suspended_by")
    private Long suspendedBy; // ID of MODERATOR/ADMIN who suspended

    @Column(name = "suspension_reason", length = 500)
    private String suspensionReason;

    // ============ RATING FIELDS ============

    @Column(name = "rating_as_seller", precision = 3, scale = 2)
    private java.math.BigDecimal ratingAsSeller;

    @Column(name = "rating_as_buyer", precision = 3, scale = 2)
    private java.math.BigDecimal ratingAsBuyer;

    @Column(name = "total_sales", nullable = false)
    private Integer totalSales = 0;

    @Column(name = "total_purchases", nullable = false)
    private Integer totalPurchases = 0;

    // Anti-fraud: block new inquiries if pending review exists
    @Column(name = "has_pending_review", nullable = false)
    private Boolean hasPendingReview = false;

    // Expo push notification token (registered by the mobile app on login)
    @Column(name = "expo_push_token", length = 200)
    private String expoPushToken;

    // ============ SOFT DELETE FIELDS ============

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy; // ID of ADMIN who deleted

    @Column(name = "deletion_reason", length = 500)
    private String deletionReason;

    // ============ CONSTRUCTORS ============

    // Empty constructor (required for JPA)
    public Usuario() {
    }

    // Constructor with required parameters
    public Usuario(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.registrationDate = LocalDateTime.now();
        this.role = Role.USER;
        this.subscriptionPlan = SubscriptionPlan.FREE;
        this.subscriptionStatus = SubscriptionStatus.NONE;
        this.isSuspended = false;
        this.isDeleted = false;
    }

    /**
     * @PrePersist: This method is called automatically before saving to database
     */
    @PrePersist
    protected void onCreate() {
        if (registrationDate == null) {
            registrationDate = LocalDateTime.now();
        }
        if (role == null) {
            role = Role.USER;
        }
        if (subscriptionPlan == null) {
            subscriptionPlan = SubscriptionPlan.FREE;
        }
        if (subscriptionStatus == null) {
            subscriptionStatus = SubscriptionStatus.NONE;
        }
        if (isSuspended == null) {
            isSuspended = false;
        }
        if (isDeleted == null) {
            isDeleted = false;
        }
    }

    // ============ GETTERS AND SETTERS ============

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public SubscriptionPlan getSubscriptionPlan() {
        return subscriptionPlan;
    }

    public void setSubscriptionPlan(SubscriptionPlan subscriptionPlan) {
        this.subscriptionPlan = subscriptionPlan;
    }

    public SubscriptionStatus getSubscriptionStatus() {
        return subscriptionStatus;
    }

    public void setSubscriptionStatus(SubscriptionStatus subscriptionStatus) {
        this.subscriptionStatus = subscriptionStatus;
    }

    public LocalDateTime getSubscriptionStartDate() {
        return subscriptionStartDate;
    }

    public void setSubscriptionStartDate(LocalDateTime subscriptionStartDate) {
        this.subscriptionStartDate = subscriptionStartDate;
    }

    public LocalDateTime getSubscriptionEndDate() {
        return subscriptionEndDate;
    }

    public void setSubscriptionEndDate(LocalDateTime subscriptionEndDate) {
        this.subscriptionEndDate = subscriptionEndDate;
    }

    public LocalDateTime getGracePeriodEndDate() {
        return gracePeriodEndDate;
    }

    public void setGracePeriodEndDate(LocalDateTime gracePeriodEndDate) {
        this.gracePeriodEndDate = gracePeriodEndDate;
    }

    public Integer getExtraPostsPurchased() {
        return extraPostsPurchased;
    }

    public void setExtraPostsPurchased(Integer extraPostsPurchased) {
        this.extraPostsPurchased = extraPostsPurchased;
    }

    // Role
    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    // Suspension
    public Boolean getIsSuspended() {
        return isSuspended;
    }

    public void setIsSuspended(Boolean isSuspended) {
        this.isSuspended = isSuspended;
    }

    public LocalDateTime getSuspendedUntil() {
        return suspendedUntil;
    }

    public void setSuspendedUntil(LocalDateTime suspendedUntil) {
        this.suspendedUntil = suspendedUntil;
    }

    public Long getSuspendedBy() {
        return suspendedBy;
    }

    public void setSuspendedBy(Long suspendedBy) {
        this.suspendedBy = suspendedBy;
    }

    public String getSuspensionReason() {
        return suspensionReason;
    }

    public void setSuspensionReason(String suspensionReason) {
        this.suspensionReason = suspensionReason;
    }

    // Deletion
    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public Long getDeletedBy() {
        return deletedBy;
    }

    public void setDeletedBy(Long deletedBy) {
        this.deletedBy = deletedBy;
    }

    public String getDeletionReason() {
        return deletionReason;
    }

    public void setDeletionReason(String deletionReason) {
        this.deletionReason = deletionReason;
    }

    public String getExpoPushToken() {
        return expoPushToken;
    }

    public void setExpoPushToken(String expoPushToken) {
        this.expoPushToken = expoPushToken;
    }

    public java.math.BigDecimal getRatingAsSeller() {
        return ratingAsSeller;
    }

    public void setRatingAsSeller(java.math.BigDecimal ratingAsSeller) {
        this.ratingAsSeller = ratingAsSeller;
    }

    public java.math.BigDecimal getRatingAsBuyer() {
        return ratingAsBuyer;
    }

    public void setRatingAsBuyer(java.math.BigDecimal ratingAsBuyer) {
        this.ratingAsBuyer = ratingAsBuyer;
    }

    public Integer getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(Integer totalSales) {
        this.totalSales = totalSales;
    }

    public Integer getTotalPurchases() {
        return totalPurchases;
    }

    public void setTotalPurchases(Integer totalPurchases) {
        this.totalPurchases = totalPurchases;
    }

    public Boolean getHasPendingReview() {
        return hasPendingReview;
    }

    public void setHasPendingReview(Boolean hasPendingReview) {
        this.hasPendingReview = hasPendingReview;
    }

    // ============ BUSINESS LOGIC METHODS ============

    /**
     * Checks if user account is active
     */
    public boolean isActive() {
        return !isDeleted && !isSuspended;
    }

    /**
     * Checks if suspension has expired
     */
    public boolean isSuspensionExpired() {
        if (!isSuspended || suspendedUntil == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(suspendedUntil);
    }
}
