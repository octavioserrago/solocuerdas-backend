package com.solocuerdas.solocuerdas_backend.service;

import com.solocuerdas.solocuerdas_backend.dto.ChangePasswordRequest;
import com.solocuerdas.solocuerdas_backend.dto.LoginRequest;
import com.solocuerdas.solocuerdas_backend.dto.LoginResponse;
import com.solocuerdas.solocuerdas_backend.dto.UpdateProfileRequest;
import com.solocuerdas.solocuerdas_backend.model.Usuario;
import com.solocuerdas.solocuerdas_backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
                usuario.getIsSuspended(),
                usuario.getIsDeleted());
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
}
