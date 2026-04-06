package com.solocuerdas.solocuerdas_backend.exception;

import com.solocuerdas.solocuerdas_backend.dto.PublicationResponse;

import java.util.List;

/**
 * Thrown when a user tries to cancel their subscription but has more active
 * publications than the FREE plan allows. The frontend must ask the user
 * which publications to deactivate before confirming the cancellation.
 */
public class PublicationLimitConflictException extends RuntimeException {

    private final List<PublicationResponse> activePublications;
    private final int allowedLimit;

    public PublicationLimitConflictException(List<PublicationResponse> activePublications, int allowedLimit) {
        super("Must deactivate " + (activePublications.size() - allowedLimit)
                + " publication(s) before cancelling subscription");
        this.activePublications = activePublications;
        this.allowedLimit = allowedLimit;
    }

    public List<PublicationResponse> getActivePublications() {
        return activePublications;
    }

    public int getAllowedLimit() {
        return allowedLimit;
    }
}
