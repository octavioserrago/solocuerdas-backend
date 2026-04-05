package com.solocuerdas.solocuerdas_backend.service;

import com.solocuerdas.solocuerdas_backend.dto.ChangePasswordRequest;
import com.solocuerdas.solocuerdas_backend.dto.LoginRequest;
import com.solocuerdas.solocuerdas_backend.dto.LoginResponse;
import com.solocuerdas.solocuerdas_backend.dto.SubscriptionPlanOptionResponse;
import com.solocuerdas.solocuerdas_backend.dto.SubscriptionResponse;
import com.solocuerdas.solocuerdas_backend.dto.UpdateProfileRequest;
import com.solocuerdas.solocuerdas_backend.model.SubscriptionPlan;
import com.solocuerdas.solocuerdas_backend.model.SubscriptionStatus;
import com.solocuerdas.solocuerdas_backend.model.Usuario;
import com.solocuerdas.solocuerdas_backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * SERVICE - Business logic layer
 * 
 * This is where we put:
 * - Validations
 * - Business rules
 * - Data transformations
 * - Calls to Repository
 */
@Service
public class UsuarioService {

    private static final int FREE_LIMIT = 2;
    private static final int BASIC_LIMIT = 10;
    private static final int PRO_LIMIT = 30;
    private static final int UNLIMITED_LIMIT = -1;
    private static final int GRACE_DAYS = 5;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * CREATE USER
     * Validates that email doesn't exist, encrypts password, and saves the user
     */
    public Usuario createUser(Usuario usuario) {
        // Validation: Check if email already exists
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new RuntimeException("Email already exists: " + usuario.getEmail());
        }

        // Encrypt password before saving
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        // Save user to database
        return usuarioRepository.save(usuario);
    }

    /**
     * LOGIN / AUTHENTICATE USER
     * Validates email and password, returns user data without password
     */
    public LoginResponse authenticateUser(LoginRequest loginRequest) {
        // Find user by email
        Usuario usuario = usuarioRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        // Check if user is deleted
        if (usuario.getIsDeleted()) {
            throw new RuntimeException("This account has been deleted");
        }

        // Check if user is suspended
        if (usuario.getIsSuspended()) {
            throw new RuntimeException("This account is suspended");
        }

        // Verify password using BCrypt
        if (!passwordEncoder.matches(loginRequest.getPassword(), usuario.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        // Create response without password
        return new LoginResponse(
                usuario.getId(),
                usuario.getName(),
                usuario.getEmail(),
                usuario.getPhone(),
                usuario.getRegistrationDate(),
                usuario.getRole(),
                usuario.getSubscriptionPlan(),
                usuario.getSubscriptionStatus(),
                usuario.getSubscriptionEndDate(),
                usuario.getIsSuspended(),
                usuario.getIsDeleted());
    }

    /**
     * GET ALL USERS
     */
    public List<Usuario> getAllUsers() {
        return usuarioRepository.findAll();
    }

    /**
     * GET USER BY ID
     */
    public Optional<Usuario> getUserById(Long id) {
        return usuarioRepository.findById(id);
    }

    /**
     * GET USER BY EMAIL
     */
    public Optional<Usuario> getUserByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    /**
     * UPDATE USER PROFILE
     * Updates name and phone only (not email or password)
     */
    public LoginResponse updateUserProfile(Long id, UpdateProfileRequest updateRequest) {
        // Find user or throw exception
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        // Check if user is deleted
        if (usuario.getIsDeleted()) {
            throw new RuntimeException("Cannot update deleted account");
        }

        // Check if user is suspended
        if (usuario.getIsSuspended()) {
            throw new RuntimeException("Cannot update suspended account");
        }

        // Update only name and phone
        if (updateRequest.getName() != null && !updateRequest.getName().trim().isEmpty()) {
            usuario.setName(updateRequest.getName());
        }

        if (updateRequest.getPhone() != null) {
            usuario.setPhone(updateRequest.getPhone());
        }

        // Save changes
        usuarioRepository.save(usuario);

        // Return updated user data without password
        return new LoginResponse(
                usuario.getId(),
                usuario.getName(),
                usuario.getEmail(),
                usuario.getPhone(),
                usuario.getRegistrationDate(),
                usuario.getRole(),
                usuario.getSubscriptionPlan(),
                usuario.getSubscriptionStatus(),
                usuario.getSubscriptionEndDate(),
                usuario.getIsSuspended(),
                usuario.getIsDeleted());
    }

    /**
     * GET SUBSCRIPTION DETAILS FOR A USER
     */
    public SubscriptionResponse getSubscription(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        normalizeSubscriptionState(usuario);
        usuarioRepository.save(usuario);

        return mapSubscriptionResponse(usuario);
    }

    /**
     * CHANGE SUBSCRIPTION PLAN (simulates successful payment for paid plans)
     */
    public SubscriptionResponse updateSubscriptionPlan(Long id, SubscriptionPlan plan) {
        if (plan == null) {
            throw new RuntimeException("Plan is required");
        }

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        LocalDateTime now = LocalDateTime.now();

        if (plan == SubscriptionPlan.FREE) {
            usuario.setSubscriptionPlan(SubscriptionPlan.FREE);
            usuario.setSubscriptionStatus(SubscriptionStatus.NONE);
            usuario.setSubscriptionStartDate(null);
            usuario.setSubscriptionEndDate(null);
            usuario.setGracePeriodEndDate(null);
        } else {
            usuario.setSubscriptionPlan(plan);
            usuario.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
            usuario.setSubscriptionStartDate(now);
            usuario.setSubscriptionEndDate(now.plusMonths(1));
            usuario.setGracePeriodEndDate(null);
        }

        usuarioRepository.save(usuario);
        return mapSubscriptionResponse(usuario);
    }

    /**
     * RENEW CURRENT PAID PLAN FOR ONE MORE MONTH
     */
    public SubscriptionResponse renewSubscription(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        normalizeSubscriptionState(usuario);

        if (usuario.getSubscriptionPlan() == SubscriptionPlan.FREE) {
            throw new RuntimeException("Free plan cannot be renewed. Choose a paid plan first");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime baseDate = usuario.getSubscriptionEndDate();
        if (baseDate == null || baseDate.isBefore(now)) {
            baseDate = now;
            usuario.setSubscriptionStartDate(now);
        }

        usuario.setSubscriptionEndDate(baseDate.plusMonths(1));
        usuario.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
        usuario.setGracePeriodEndDate(null);

        usuarioRepository.save(usuario);
        return mapSubscriptionResponse(usuario);
    }

    /**
     * CANCEL SUBSCRIPTION AND RETURN TO FREE PLAN
     */
    public SubscriptionResponse cancelSubscription(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        usuario.setSubscriptionPlan(SubscriptionPlan.FREE);
        usuario.setSubscriptionStatus(SubscriptionStatus.CANCELLED);
        usuario.setSubscriptionStartDate(null);
        usuario.setSubscriptionEndDate(null);
        usuario.setGracePeriodEndDate(null);

        usuarioRepository.save(usuario);
        return mapSubscriptionResponse(usuario);
    }

    /**
     * LIST ALL AVAILABLE SUBSCRIPTION PLANS
     */
    public List<SubscriptionPlanOptionResponse> getAvailableSubscriptionPlans() {
        return List.of(
                new SubscriptionPlanOptionResponse(
                        SubscriptionPlan.FREE,
                        FREE_LIMIT,
                        BigDecimal.ZERO,
                        "Free plan with up to 2 active publications"),
                new SubscriptionPlanOptionResponse(
                        SubscriptionPlan.SELLER_BASIC,
                        BASIC_LIMIT,
                        BigDecimal.valueOf(5),
                        "Basic seller plan with up to 10 active publications"),
                new SubscriptionPlanOptionResponse(
                        SubscriptionPlan.SELLER_PRO,
                        PRO_LIMIT,
                        BigDecimal.valueOf(10),
                        "Pro seller plan with up to 30 active publications"),
                new SubscriptionPlanOptionResponse(
                        SubscriptionPlan.BUSINESS_UNLIMITED,
                        UNLIMITED_LIMIT,
                        BigDecimal.valueOf(25),
                        "Business plan with unlimited active publications"));
    }

    /**
     * CHANGE USER PASSWORD
     * Requires current password for security
     */
    public void changePassword(Long id, ChangePasswordRequest changePasswordRequest) {
        // Find user or throw exception
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        // Check if user is deleted
        if (usuario.getIsDeleted()) {
            throw new RuntimeException("Cannot change password for deleted account");
        }

        // Check if user is suspended
        if (usuario.getIsSuspended()) {
            throw new RuntimeException("Cannot change password for suspended account");
        }

        // Verify current password
        if (!passwordEncoder.matches(changePasswordRequest.getCurrentPassword(), usuario.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // Validate new password
        if (changePasswordRequest.getNewPassword() == null ||
                changePasswordRequest.getNewPassword().length() < 6) {
            throw new RuntimeException("New password must be at least 6 characters");
        }

        // Encrypt and save new password
        usuario.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
        usuarioRepository.save(usuario);
    }

    /**
     * UPDATE USER (LEGACY - Keep for backward compatibility)
     * 
     * @deprecated Use updateUserProfile instead
     */
    @Deprecated
    public Usuario updateUser(Long id, Usuario usuarioDetails) {
        // Find user or throw exception
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        // Update fields
        usuario.setName(usuarioDetails.getName());
        usuario.setEmail(usuarioDetails.getEmail());
        usuario.setPhone(usuarioDetails.getPhone());

        // Save changes
        return usuarioRepository.save(usuario);
    }

    /**
     * DELETE USER
     */
    public void deleteUser(Long id) {
        // Check if user exists
        if (!usuarioRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        usuarioRepository.deleteById(id);
    }

    /**
     * COUNT USERS
     */
    public long countUsers() {
        return usuarioRepository.count();
    }

    private void normalizeSubscriptionState(Usuario usuario) {
        SubscriptionPlan plan = usuario.getSubscriptionPlan();

        if (plan == null || plan == SubscriptionPlan.FREE) {
            usuario.setSubscriptionPlan(SubscriptionPlan.FREE);
            if (usuario.getSubscriptionStatus() != SubscriptionStatus.CANCELLED) {
                usuario.setSubscriptionStatus(SubscriptionStatus.NONE);
            }
            usuario.setSubscriptionStartDate(null);
            usuario.setSubscriptionEndDate(null);
            usuario.setGracePeriodEndDate(null);
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endDate = usuario.getSubscriptionEndDate();

        if (endDate == null) {
            usuario.setSubscriptionStatus(SubscriptionStatus.EXPIRED);
            usuario.setSubscriptionPlan(SubscriptionPlan.FREE);
            usuario.setSubscriptionStartDate(null);
            usuario.setSubscriptionEndDate(null);
            usuario.setGracePeriodEndDate(null);
            return;
        }

        if (!now.isAfter(endDate)) {
            usuario.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
            usuario.setGracePeriodEndDate(null);
            return;
        }

        LocalDateTime graceEnd = usuario.getGracePeriodEndDate();
        if (graceEnd == null) {
            graceEnd = endDate.plusDays(GRACE_DAYS);
            usuario.setGracePeriodEndDate(graceEnd);
        }

        if (!now.isAfter(graceEnd)) {
            usuario.setSubscriptionStatus(SubscriptionStatus.GRACE_PERIOD);
            return;
        }

        usuario.setSubscriptionStatus(SubscriptionStatus.EXPIRED);
        usuario.setSubscriptionPlan(SubscriptionPlan.FREE);
        usuario.setSubscriptionStartDate(null);
        usuario.setSubscriptionEndDate(null);
        usuario.setGracePeriodEndDate(null);
    }

    private SubscriptionResponse mapSubscriptionResponse(Usuario usuario) {
        SubscriptionResponse response = new SubscriptionResponse();
        response.setUserId(usuario.getId());
        response.setPlan(usuario.getSubscriptionPlan());
        response.setStatus(usuario.getSubscriptionStatus());
        response.setMaxActivePublications(getPublicationLimitByPlan(usuario.getSubscriptionPlan()));
        response.setMonthlyPriceUsd(getPriceByPlan(usuario.getSubscriptionPlan()));
        response.setSubscriptionStartDate(usuario.getSubscriptionStartDate());
        response.setSubscriptionEndDate(usuario.getSubscriptionEndDate());
        response.setGracePeriodEndDate(usuario.getGracePeriodEndDate());
        response.setCanCreateMorePublications(true);
        return response;
    }

    private Integer getPublicationLimitByPlan(SubscriptionPlan plan) {
        if (plan == null) {
            return 2;
        }
        switch (plan) {
            case SELLER_BASIC:
                return 10;
            case SELLER_PRO:
                return 30;
            case BUSINESS_UNLIMITED:
                return UNLIMITED_LIMIT;
            case FREE:
            default:
                return 2;
        }
    }

    private BigDecimal getPriceByPlan(SubscriptionPlan plan) {
        if (plan == null) {
            return BigDecimal.ZERO;
        }
        switch (plan) {
            case SELLER_BASIC:
                return BigDecimal.valueOf(5);
            case SELLER_PRO:
                return BigDecimal.valueOf(10);
            case BUSINESS_UNLIMITED:
                return BigDecimal.valueOf(25);
            case FREE:
            default:
                return BigDecimal.ZERO;
        }
    }
}
