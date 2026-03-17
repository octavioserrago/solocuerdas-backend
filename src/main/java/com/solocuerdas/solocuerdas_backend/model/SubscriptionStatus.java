package com.solocuerdas.solocuerdas_backend.model;

/**
 * SUBSCRIPTION STATUS ENUM
 * Defines the status of a user's subscription
 */
public enum SubscriptionStatus {
    /**
     * ACTIVE - Subscription is paid and active
     * User has full access to their tier benefits
     */
    ACTIVE,

    /**
     * GRACE_PERIOD - Subscription expired but within 10-day grace period
     * User still has full access, but needs to pay soon
     */
    GRACE_PERIOD,

    /**
     * EXPIRED - Subscription expired and grace period is over
     * User reverted to FREE tier (2 publications max)
     * Extra publications are hidden
     */
    EXPIRED,

    /**
     * CANCELLED - Subscription was manually cancelled
     * By user or by ADMIN
     */
    CANCELLED,

    /**
     * NONE - User has no subscription (FREE tier)
     * Default state for new users
     */
    NONE
}
