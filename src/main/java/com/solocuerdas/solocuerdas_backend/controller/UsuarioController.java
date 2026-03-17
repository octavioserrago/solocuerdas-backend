package com.solocuerdas.solocuerdas_backend.controller;

import com.solocuerdas.solocuerdas_backend.dto.ChangePasswordRequest;
import com.solocuerdas.solocuerdas_backend.dto.LoginRequest;
import com.solocuerdas.solocuerdas_backend.dto.LoginResponse;
import com.solocuerdas.solocuerdas_backend.dto.UpdateProfileRequest;
import com.solocuerdas.solocuerdas_backend.model.Usuario;
import com.solocuerdas.solocuerdas_backend.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CONTROLLER - REST API endpoints for User management
 * 
 * This receives HTTP requests and returns responses
 * Base URL: http://localhost:8080/api/users
 */
@RestController
@RequestMapping("/api/users")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    /**
     * CREATE USER (REGISTER)
     * POST /api/users/register
     * Body: { "name": "John", "email": "john@example.com", "password": "123456" }
     */
    @PostMapping("/register")
    public ResponseEntity<?> createUser(@RequestBody Usuario usuario) {
        try {
            Usuario newUser = usuarioService.createUser(usuario);
            // Return user without password
            newUser.setPassword(null);
            return new ResponseEntity<>(newUser, HttpStatus.CREATED); // 201
        } catch (RuntimeException e) {
            // Return error message so we can see what's wrong
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * LOGIN USER
     * POST /api/users/login
     * Body: { "email": "john@example.com", "password": "123456" }
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse response = usuarioService.authenticateUser(loginRequest);
            return new ResponseEntity<>(response, HttpStatus.OK); // 200
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Error: " + e.getMessage()); // 401
        }
    }

    /**
     * GET ALL USERS
     * GET /api/users
     */
    @GetMapping
    public ResponseEntity<List<Usuario>> getAllUsers() {
        List<Usuario> users = usuarioService.getAllUsers();
        return new ResponseEntity<>(users, HttpStatus.OK); // 200
    }

    /**
     * GET USER BY ID
     * GET /api/users/1
     */
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> getUserById(@PathVariable Long id) {
        return usuarioService.getUserById(id)
                .map(user -> new ResponseEntity<>(user, HttpStatus.OK)) // 200
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND)); // 404
    }

    /**
     * GET USER BY EMAIL
     * GET /api/users/email/john@example.com
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<Usuario> getUserByEmail(@PathVariable String email) {
        return usuarioService.getUserByEmail(email)
                .map(user -> new ResponseEntity<>(user, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * UPDATE USER PROFILE
     * PUT /api/users/{id}/profile
     * Body: { "name": "John Updated", "phone": "123456" }
     */
    @PutMapping("/{id}/profile")
    public ResponseEntity<?> updateUserProfile(@PathVariable Long id, @RequestBody UpdateProfileRequest updateRequest) {
        try {
            LoginResponse updatedUser = usuarioService.updateUserProfile(id, updateRequest);
            return new ResponseEntity<>(updatedUser, HttpStatus.OK); // 200
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * CHANGE USER PASSWORD
     * PUT /api/users/{id}/password
     * Body: { "currentPassword": "old123", "newPassword": "new456" }
     */
    @PutMapping("/{id}/password")
    public ResponseEntity<?> changePassword(@PathVariable Long id,
            @RequestBody ChangePasswordRequest changePasswordRequest) {
        try {
            usuarioService.changePassword(id, changePasswordRequest);
            return ResponseEntity.ok().body("Password changed successfully"); // 200
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * UPDATE USER (LEGACY - deprecated)
     * PUT /api/users/1
     * Body: { "name": "John Updated", "email": "john@example.com", "phone":
     * "123456" }
     * 
     * @deprecated Use /api/users/{id}/profile instead
     */
    @Deprecated
    @PutMapping("/{id}")
    public ResponseEntity<Usuario> updateUser(@PathVariable Long id, @RequestBody Usuario usuario) {
        try {
            Usuario updatedUser = usuarioService.updateUser(id, usuario);
            return new ResponseEntity<>(updatedUser, HttpStatus.OK);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * DELETE USER
     * DELETE /api/users/1
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        try {
            usuarioService.deleteUser(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * COUNT USERS
     * GET /api/users/count
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countUsers() {
        long count = usuarioService.countUsers();
        return new ResponseEntity<>(count, HttpStatus.OK);
    }
}
