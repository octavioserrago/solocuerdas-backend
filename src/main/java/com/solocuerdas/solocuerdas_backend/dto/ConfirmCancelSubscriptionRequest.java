package com.solocuerdas.solocuerdas_backend.dto;

import java.util.List;

/**
 * Request body for POST /api/users/{id}/subscription/cancel/confirm.
 * The client must provide the IDs of the publications to deactivate (set to
 * PAUSED)
 * so that the remaining active count is within the FREE plan limit.
 */
public class ConfirmCancelSubscriptionRequest {

    private List<Long> publicationIdsToDeactivate;

    public List<Long> getPublicationIdsToDeactivate() {
        return publicationIdsToDeactivate;
    }

    public void setPublicationIdsToDeactivate(List<Long> publicationIdsToDeactivate) {
        this.publicationIdsToDeactivate = publicationIdsToDeactivate;
    }
}
