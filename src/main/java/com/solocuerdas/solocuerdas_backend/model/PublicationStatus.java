package com.solocuerdas.solocuerdas_backend.model;

/**
 * PUBLICATION STATUS ENUM
 * Current status of a publication
 */
public enum PublicationStatus {
    ACTIVE("Active"), // Visible in marketplace
    PAUSED("Paused"), // Hidden temporarily by user
    SOLD("Sold"), // Marked as sold
    DELETED("Deleted"); // Soft deleted

    private final String displayName;

    PublicationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
