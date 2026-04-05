package com.solocuerdas.solocuerdas_backend.dto;

import com.solocuerdas.solocuerdas_backend.model.SubscriptionPlan;
import com.solocuerdas.solocuerdas_backend.model.SubscriptionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for subscription and plan details.
 */
public class SubscriptionResponse {

    private Long userId;
    private SubscriptionPlan plan;
    private SubscriptionStatus status;
    private Integer maxActivePublications;
    private BigDecimal monthlyPriceUsd;
    private LocalDateTime subscriptionStartDate;
    private LocalDateTime subscriptionEndDate;
    private LocalDateTime gracePeriodEndDate;
    private boolean canCreateMorePublications;

    public SubscriptionResponse() {
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public SubscriptionPlan getPlan() {
        return plan;
    }

    public void setPlan(SubscriptionPlan plan) {
        this.plan = plan;
    }

    public SubscriptionStatus getStatus() {
        return status;
    }

    public void setStatus(SubscriptionStatus status) {
        this.status = status;
    }

    public Integer getMaxActivePublications() {
        return maxActivePublications;
    }

    public void setMaxActivePublications(Integer maxActivePublications) {
        this.maxActivePublications = maxActivePublications;
    }

    public BigDecimal getMonthlyPriceUsd() {
        return monthlyPriceUsd;
    }

    public void setMonthlyPriceUsd(BigDecimal monthlyPriceUsd) {
        this.monthlyPriceUsd = monthlyPriceUsd;
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

    public boolean isCanCreateMorePublications() {
        return canCreateMorePublications;
    }

    public void setCanCreateMorePublications(boolean canCreateMorePublications) {
        this.canCreateMorePublications = canCreateMorePublications;
    }
}
