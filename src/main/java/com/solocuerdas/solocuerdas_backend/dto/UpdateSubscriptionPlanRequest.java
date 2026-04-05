package com.solocuerdas.solocuerdas_backend.dto;

import com.solocuerdas.solocuerdas_backend.model.SubscriptionPlan;

/**
 * DTO to update seller subscription plan.
 */
public class UpdateSubscriptionPlanRequest {

    private SubscriptionPlan plan;

    public UpdateSubscriptionPlanRequest() {
    }

    public SubscriptionPlan getPlan() {
        return plan;
    }

    public void setPlan(SubscriptionPlan plan) {
        this.plan = plan;
    }
}
