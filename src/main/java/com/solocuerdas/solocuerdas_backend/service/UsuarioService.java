package com.solocuerdas.solocuerdas_backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solocuerdas.solocuerdas_backend.dto.ChangePasswordRequest;
import com.solocuerdas.solocuerdas_backend.dto.LoginRequest;
import com.solocuerdas.solocuerdas_backend.dto.LoginResponse;
import com.solocuerdas.solocuerdas_backend.dto.PublicationResponse;
import com.solocuerdas.solocuerdas_backend.dto.SubscriptionPlanOptionResponse;
import com.solocuerdas.solocuerdas_backend.dto.SubscriptionResponse;
import com.solocuerdas.solocuerdas_backend.dto.UpdateProfileRequest;
import com.solocuerdas.solocuerdas_backend.exception.PublicationLimitConflictException;
import com.solocuerdas.solocuerdas_backend.model.Publication;
import com.solocuerdas.solocuerdas_backend.model.PublicationStatus;
import com.solocuerdas.solocuerdas_backend.model.SubscriptionPlan;
import com.solocuerdas.solocuerdas_backend.model.SubscriptionStatus;
import com.solocuerdas.solocuerdas_backend.model.Usuario;
import com.solocuerdas.solocuerdas_backend.repository.PublicationRepository;
import com.solocuerdas.solocuerdas_backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    private static final int FREE_LIMIT = 3;
    private static final int HOBBY_LIMIT = 15;
    private static final int UNLIMITED_LIMIT = -1;
    private static final int GRACE_DAYS = 5;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PublicationRepository publicationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

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
     * If the user has more active publications than the FREE limit allows,
     * throws PublicationLimitConflictException so the frontend can ask which
     * publications to deactivate. Use confirmCancelSubscription() to proceed.
     */
    public SubscriptionResponse cancelSubscription(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        List<Publication> activePublications = publicationRepository
                .findByUserAndStatus(usuario, PublicationStatus.ACTIVE);

        int extraPosts = usuario.getExtraPostsPurchased() != null ? usuario.getExtraPostsPurchased() : 0;
        int freeAllowed = FREE_LIMIT + extraPosts;

        if (activePublications.size() > freeAllowed) {
            List<PublicationResponse> dtos = activePublications.stream()
                    .map(this::mapPublicationToResponse)
                    .collect(Collectors.toList());
            throw new PublicationLimitConflictException(dtos, freeAllowed);
        }

        usuario.setSubscriptionPlan(SubscriptionPlan.FREE);
        usuario.setSubscriptionStatus(SubscriptionStatus.CANCELLED);
        usuario.setSubscriptionStartDate(null);
        usuario.setSubscriptionEndDate(null);
        usuario.setGracePeriodEndDate(null);

        usuarioRepository.save(usuario);
        return mapSubscriptionResponse(usuario);
    }

    /**
     * CONFIRM SUBSCRIPTION CANCELLATION WITH PUBLICATION DEACTIVATION
     * Deactivates the specified publications (sets them to PAUSED) and then
     * cancels the subscription. The caller must provide enough IDs so that
     * the remaining active count is within the FREE plan limit.
     */
    public SubscriptionResponse confirmCancelSubscription(Long id, List<Long> publicationIdsToDeactivate) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        if (publicationIdsToDeactivate == null || publicationIdsToDeactivate.isEmpty()) {
            throw new RuntimeException("publicationIdsToDeactivate must not be empty");
        }

        long activeCount = publicationRepository.countByUserAndStatus(usuario, PublicationStatus.ACTIVE);
        long remainingAfterDeactivation = activeCount - publicationIdsToDeactivate.size();
        int extraPosts = usuario.getExtraPostsPurchased() != null ? usuario.getExtraPostsPurchased() : 0;
        int freeAllowed = FREE_LIMIT + extraPosts;

        if (remainingAfterDeactivation > freeAllowed) {
            throw new RuntimeException(
                    "Not enough publications selected for deactivation. "
                            + "You need to deactivate at least " + (activeCount - freeAllowed)
                            + " publication(s), but only " + publicationIdsToDeactivate.size() + " were provided.");
        }

        for (Long pubId : publicationIdsToDeactivate) {
            Publication publication = publicationRepository.findById(pubId)
                    .orElseThrow(() -> new RuntimeException("Publication not found with id: " + pubId));

            if (!publication.getUser().getId().equals(id)) {
                throw new RuntimeException("Publication " + pubId + " does not belong to this user");
            }

            if (publication.getStatus() == PublicationStatus.ACTIVE) {
                publication.setStatus(PublicationStatus.PAUSED);
                publicationRepository.save(publication);
            }
        }

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
                        "Free plan with up to 3 active publications"),
                new SubscriptionPlanOptionResponse(
                        SubscriptionPlan.HOBBY,
                        HOBBY_LIMIT,
                        BigDecimal.valueOf(5),
                        "Hobby plan with up to 15 active publications"),
                new SubscriptionPlanOptionResponse(
                        SubscriptionPlan.BUSINESS,
                        UNLIMITED_LIMIT,
                        BigDecimal.valueOf(18),
                        "Business plan with unlimited active publications"));
    }

    /**
     * REGISTER / UPDATE EXPO PUSH TOKEN
     * Pass null to clear the token (e.g. on logout).
     */
    public void registerPushToken(Long id, String token) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        usuario.setExpoPushToken(token);
        usuarioRepository.save(usuario);
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
            return FREE_LIMIT;
        }
        switch (plan) {
            case HOBBY:
                return HOBBY_LIMIT;
            case BUSINESS:
                return UNLIMITED_LIMIT;
            case FREE:
            default:
                return FREE_LIMIT;
        }
    }

    private BigDecimal getPriceByPlan(SubscriptionPlan plan) {
        if (plan == null) {
            return BigDecimal.ZERO;
        }
        switch (plan) {
            case HOBBY:
                return BigDecimal.valueOf(5);
            case BUSINESS:
                return BigDecimal.valueOf(18);
            case FREE:
            default:
                return BigDecimal.ZERO;
        }
    }

    private PublicationResponse mapPublicationToResponse(Publication publication) {
        PublicationResponse response = new PublicationResponse();
        response.setId(publication.getId());
        response.setTitle(publication.getTitle());
        response.setDescription(publication.getDescription());
        response.setPrice(publication.getPrice());
        response.setCategory(publication.getCategory());
        response.setCondition(publication.getCondition());
        response.setBrand(publication.getBrand());
        response.setYear(publication.getYear());
        response.setLocation(publication.getLocation());
        response.setStatus(publication.getStatus());
        response.setUserId(publication.getUser().getId());
        response.setUserName(publication.getUser().getName());
        response.setCreatedAt(publication.getCreatedAt());
        response.setUpdatedAt(publication.getUpdatedAt());
        response.setSoldAt(publication.getSoldAt());
        response.setViewsCount(publication.getViewsCount());
        try {
            if (publication.getImages() != null) {
                response.setImages(objectMapper.readValue(publication.getImages(),
                        new TypeReference<List<String>>() {
                        }));
            }
        } catch (Exception e) {
            response.setImages(new ArrayList<>());
        }
        return response;
    }
}
