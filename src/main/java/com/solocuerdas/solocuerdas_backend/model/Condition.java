package com.solocuerdas.solocuerdas_backend.model;

/**
 * CONDITION ENUM
 * Physical condition of the instrument
 */
public enum Condition {
    NEW("New"),
    LIKE_NEW("Like New"),
    EXCELLENT("Excellent"),
    GOOD("Good"),
    FAIR("Fair");

    private final String displayName;

    Condition(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
