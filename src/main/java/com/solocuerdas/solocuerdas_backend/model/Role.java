package com.solocuerdas.solocuerdas_backend.model;

/**
 * ROLE ENUM
 * Defines all user roles in the system
 * Hierarchy: SUPER_ADMIN > ADMIN > MODERATOR > PRO_SELLER > VENDEDOR > USER
 */
public enum Role {
    /**
     * USER - Free tier
     * - Max 2 publications
     * - Can buy and sell
     */
    USER,

    /**
     * VENDEDOR - Basic subscription
     * - Max 10 publications
     * - Monthly payment
     * - 10 days grace period
     */
    VENDEDOR,

    /**
     * PRO_SELLER - Premium subscription
     * - Unlimited publications
     * - Monthly payment
     * - 10 days grace period
     * - Featured products
     * - Advanced statistics
     */
    PRO_SELLER,

    /**
     * MODERATOR - Support and moderation
     * - Can suspend users
     * - Can hide/restore publications
     * - Can modify user data (not email/password without approval)
     * - Can intervene in disputes
     * - Can read all conversations
     */
    MODERATOR,

    /**
     * ADMIN - Platform administrator
     * - All MODERATOR permissions +
     * - Can delete users/publications permanently
     * - Can create coupons
     * - Can make refunds
     * - Can create users (except ADMIN)
     * - Can approve email changes
     * - Can view all analytics
     */
    ADMIN,

    /**
     * SUPER_ADMIN - Founder/Owner
     * - All ADMIN permissions +
     * - Can create/delete ADMIN
     * - Full system control
     */
    SUPER_ADMIN
}
