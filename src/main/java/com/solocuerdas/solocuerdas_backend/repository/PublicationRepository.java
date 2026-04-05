package com.solocuerdas.solocuerdas_backend.repository;

import com.solocuerdas.solocuerdas_backend.model.Publication;
import com.solocuerdas.solocuerdas_backend.model.PublicationStatus;
import com.solocuerdas.solocuerdas_backend.model.Category;
import com.solocuerdas.solocuerdas_backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * REPOSITORY - Interface for database operations with Publication
 * 
 * Spring automatically implements basic CRUD operations
 */
@Repository
public interface PublicationRepository extends JpaRepository<Publication, Long> {

    /**
     * Find all publications by user
     */
    List<Publication> findByUser(Usuario user);

    /**
     * Find all publications by user ID
     */
    List<Publication> findByUserId(Long userId);

    /**
     * Find all publications with specific status
     */
    List<Publication> findByStatus(PublicationStatus status);

    /**
     * Find all ACTIVE publications by user (for checking publication limits)
     */
    List<Publication> findByUserAndStatus(Usuario user, PublicationStatus status);

    /**
     * Find ACTIVE publications by user ID and status
     */
    List<Publication> findByUserIdAndStatus(Long userId, PublicationStatus status);

    /**
     * Count ACTIVE publications by user (for role limits)
     */
    long countByUserAndStatus(Usuario user, PublicationStatus status);

    /**
     * Count publications by user ID and status
     */
    long countByUserIdAndStatus(Long userId, PublicationStatus status);

    /**
     * Find all publications by category
     */
    List<Publication> findByCategory(Category category);

    /**
     * Find all ACTIVE publications (visible in marketplace)
     */
    List<Publication> findByStatusOrderByCreatedAtDesc(PublicationStatus status);

    /**
     * Search publications by title or description (case-insensitive)
     */
    @Query("SELECT p FROM Publication p WHERE " +
            "LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Publication> searchByKeyword(@Param("keyword") String keyword);

    /**
     * Find ACTIVE publications by category
     */
    List<Publication> findByStatusAndCategory(PublicationStatus status, Category category);
}
