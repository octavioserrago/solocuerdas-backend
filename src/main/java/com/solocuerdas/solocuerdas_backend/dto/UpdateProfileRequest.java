package com.solocuerdas.solocuerdas_backend.dto;

/**
 * DTO for updating user profile
 * Excludes email and password for security
 */
public class UpdateProfileRequest {
    
    private String name;
    private String phone;

    // Constructors
    public UpdateProfileRequest() {
    }

    public UpdateProfileRequest(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
