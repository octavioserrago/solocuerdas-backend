package com.solocuerdas.solocuerdas_backend.dto;

import com.solocuerdas.solocuerdas_backend.model.Role;
import java.time.LocalDateTime;

/**
 * DTO for login responses
 * Returns user data WITHOUT password for security
 */
public class LoginResponse {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private LocalDateTime registrationDate;
    private Role role;
    private Boolean isSuspended;
    private Boolean isDeleted;

    // Constructor
    public LoginResponse() {
    }

    public LoginResponse(Long id, String name, String email, String phone,
            LocalDateTime registrationDate, Role role,
            Boolean isSuspended, Boolean isDeleted) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.registrationDate = registrationDate;
        this.role = role;
        this.isSuspended = isSuspended;
        this.isDeleted = isDeleted;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Boolean getIsSuspended() {
        return isSuspended;
    }

    public void setIsSuspended(Boolean isSuspended) {
        this.isSuspended = isSuspended;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
}
