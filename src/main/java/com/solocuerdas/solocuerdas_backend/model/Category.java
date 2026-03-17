package com.solocuerdas.solocuerdas_backend.model;

/**
 * CATEGORY ENUM
 * String instrument categories for the marketplace
 */
public enum Category {
    ACOUSTIC_GUITAR("Acoustic Guitar"),
    ELECTRIC_GUITAR("Electric Guitar"),
    CLASSICAL_GUITAR("Classical Guitar"),
    BASS("Bass"),
    UKULELE("Ukulele"),
    VIOLIN("Violin"),
    CELLO("Cello"),
    MANDOLIN("Mandolin"),
    BANJO("Banjo"),
    OTHER_STRING("Other String Instruments");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
