package com.solocuerdas.solocuerdas_backend.repository;

import com.solocuerdas.solocuerdas_backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * REPOSITORY - Interface for database operations with Usuario
 * 
 * JpaRepository<Usuario, Long> means:
 * - Usuario: The entity we're working with
 * - Long: The type of the ID field (Usuario has Long id)
 * 
 * Spring automatically implements this interface with all basic CRUD
 * operations:
 * - save(usuario) -> INSERT or UPDATE
 * - findById(id) -> SELECT by ID
 * - findAll() -> SELECT all users
 * - deleteById(id) -> DELETE by ID
 * - count() -> COUNT users
 * - existsById(id) -> Check if exists
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * CUSTOM METHOD: Find user by email
     * Spring automatically generates the SQL query from the method name!
     * "findBy" + "Email" -> SELECT * FROM users WHERE email = ?
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * CUSTOM METHOD: Check if email already exists
     * "existsBy" + "Email" -> SELECT COUNT(*) FROM users WHERE email = ?
     */
    boolean existsByEmail(String email);
}
