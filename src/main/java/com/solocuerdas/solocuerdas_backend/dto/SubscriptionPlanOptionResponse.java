package com.solocuerdas.solocuerdas_backend.dto;

import com.solocuerdas.solocuerdas_backend.model.SubscriptionPlan;

import java.math.BigDecimal;

/**
 * DTO for listing available subscription plans.
 */
public class SubscriptionPlanOptionResponse {

    private SubscriptionPlan plan;
    private Integer maxActivePublications;
    private BigDecimal monthlyPriceUsd;
    private String description;

    public SubscriptionPlanOptionResponse() {
    }

    public SubscriptionPlanOptionResponse(SubscriptionPlan plan, Integer maxActivePublications,
            BigDecimal monthlyPriceUsd, String description) {
        this.plan = plan;
        this.maxActivePublications = maxActivePublications;
        this.monthlyPriceUsd = monthlyPriceUsd;
        this.description = description;
    }

    public SubscriptionPlan getPlan() {
        return plan;
    }

    public void setPlan(SubscriptionPlan plan) {
        this.plan = plan;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
