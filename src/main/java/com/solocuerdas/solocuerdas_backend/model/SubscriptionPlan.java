package com.solocuerdas.solocuerdas_backend.model;

/**
 * SUBSCRIPTION PLAN ENUM
 * Defines the commercial plans for sellers.
 */
public enum SubscriptionPlan {
    /**
     * FREE plan: up to 2 active publications.
     */
    FREE,

    /**
     * SELLER_BASIC plan: up to 10 active publications.
     */
    SELLER_BASIC,

    /**
     * SELLER_PRO plan: up to 30 active publications.
     */
    SELLER_PRO,

    /**
     * BUSINESS_UNLIMITED plan: unlimited active publications.
     */
    BUSINESS_UNLIMITED
}
