package com.solocuerdas.solocuerdas_backend.dto;

import com.solocuerdas.solocuerdas_backend.model.Role;
import com.solocuerdas.solocuerdas_backend.model.SubscriptionPlan;
import com.solocuerdas.solocuerdas_backend.model.SubscriptionStatus;
import java.time.LocalDateTime;

/**
 * DTO for login responses
 * Returns user data WITHOUT password for security
 */
public class LoginResponse {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private LocalDateTime registrationDate;
    private Role role;
    private SubscriptionPlan subscriptionPlan;
    private SubscriptionStatus subscriptionStatus;
    private LocalDateTime subscriptionEndDate;
    private Boolean isSuspended;
    private Boolean isDeleted;

    // Constructor
    public LoginResponse() {
    }

    public LoginResponse(Long id, String name, String email, String phone,
            LocalDateTime registrationDate, Role role,
            SubscriptionPlan subscriptionPlan, SubscriptionStatus subscriptionStatus,
            LocalDateTime subscriptionEndDate,
            Boolean isSuspended, Boolean isDeleted) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.registrationDate = registrationDate;
        this.role = role;
        this.subscriptionPlan = subscriptionPlan;
        this.subscriptionStatus = subscriptionStatus;
        this.subscriptionEndDate = subscriptionEndDate;
        this.isSuspended = isSuspended;
        this.isDeleted = isDeleted;
    }

    // Getters and Setters
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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
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

    public LocalDateTime getSubscriptionEndDate() {
        return subscriptionEndDate;
    }

    public void setSubscriptionEndDate(LocalDateTime subscriptionEndDate) {
        this.subscriptionEndDate = subscriptionEndDate;
    }

    public Boolean getIsSuspended() {
        return isSuspended;
    }

    public void setIsSuspended(Boolean isSuspended) {
        this.isSuspended = isSuspended;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
}
