package com.solocuerdas.solocuerdas_backend.dto;

import java.util.List;

/**
 * Returned as HTTP 409 CONFLICT when the user tries to cancel their
 * subscription but has more active publications than the FREE plan allows.
 * The frontend should display a modal showing activePublications and ask
 * the user to choose which ones to deactivate, then call
 * POST /api/users/{id}/subscription/cancel/confirm.
 */
public class CancelSubscriptionConflictResponse {

    private final boolean requiresPublicationDeactivation = true;
    private int allowedLimit;
    private int excessCount;
    private List<PublicationResponse> activePublications;

    public CancelSubscriptionConflictResponse(List<PublicationResponse> activePublications, int allowedLimit) {
        this.activePublications = activePublications;
        this.allowedLimit = allowedLimit;
        this.excessCount = activePublications.size() - allowedLimit;
    }

    public boolean isRequiresPublicationDeactivation() {
        return requiresPublicationDeactivation;
    }

    public int getAllowedLimit() {
        return allowedLimit;
    }

    public int getExcessCount() {
        return excessCount;
    }

    public List<PublicationResponse> getActivePublications() {
        return activePublications;
    }
}
