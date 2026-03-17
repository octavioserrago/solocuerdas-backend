package com.solocuerdas.solocuerdas_backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * PUBLICATION ENTITY
 * Represents a musical instrument listing in the marketplace
 */
@Entity
@Table(name = "publications")
public class Publication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_type", nullable = false, length = 20)
    private Condition condition;

    @Column(name = "brand", length = 50)
    private String brand;

    @Column(name = "year")
    private Integer year;

    @Column(name = "location", length = 100)
    private String location;

    // JSON array of Cloudinary URLs: ["url1", "url2", "url3"]
    @Column(name = "images", columnDefinition = "JSON")
    private String images;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PublicationStatus status = PublicationStatus.ACTIVE;

    // ============ RELATIONSHIPS ============

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Usuario user;

    // ============ TIMESTAMPS ============

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "sold_at")
    private LocalDateTime soldAt;

    // ============ METRICS ============

    @Column(name = "views_count", nullable = false)
    private Integer viewsCount = 0;

    // ============ CONSTRUCTORS ============

    public Publication() {
    }

    public Publication(String title, String description, BigDecimal price, Category category,
            Condition condition, String location, Usuario user) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.category = category;
        this.condition = condition;
        this.location = location;
        this.user = user;
        this.status = PublicationStatus.ACTIVE;
        this.viewsCount = 0;
        this.createdAt = LocalDateTime.now();
    }

    // ============ JPA LIFECYCLE HOOKS ============

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = PublicationStatus.ACTIVE;
        }
        if (viewsCount == null) {
            viewsCount = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ============ BUSINESS LOGIC METHODS ============

    /**
     * Checks if publication is visible in marketplace
     */
    public boolean isVisible() {
        return status == PublicationStatus.ACTIVE;
    }

    /**
     * Marks publication as sold
     */
    public void markAsSold() {
        this.status = PublicationStatus.SOLD;
        this.soldAt = LocalDateTime.now();
    }

    /**
     * Pauses publication (hides from marketplace)
     */
    public void pause() {
        this.status = PublicationStatus.PAUSED;
    }

    /**
     * Activates publication (shows in marketplace)
     */
    public void activate() {
        this.status = PublicationStatus.ACTIVE;
    }

    /**
     * Soft deletes publication
     */
    public void delete() {
        this.status = PublicationStatus.DELETED;
    }

    /**
     * Increments view counter
     */
    public void incrementViews() {
        this.viewsCount++;
    }

    // ============ GETTERS AND SETTERS ============

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public PublicationStatus getStatus() {
        return status;
    }

    public void setStatus(PublicationStatus status) {
        this.status = status;
    }

    public Usuario getUser() {
        return user;
    }

    public void setUser(Usuario user) {
        this.user = user;
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

    public LocalDateTime getSoldAt() {
        return soldAt;
    }

    public void setSoldAt(LocalDateTime soldAt) {
        this.soldAt = soldAt;
    }

    public Integer getViewsCount() {
        return viewsCount;
    }

    public void setViewsCount(Integer viewsCount) {
        this.viewsCount = viewsCount;
    }
}
