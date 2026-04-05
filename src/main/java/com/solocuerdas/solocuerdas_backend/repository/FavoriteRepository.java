package com.solocuerdas.solocuerdas_backend.repository;

import com.solocuerdas.solocuerdas_backend.model.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    boolean existsByUserIdAndPublicationId(Long userId, Long publicationId);

    Optional<Favorite> findByUserIdAndPublicationId(Long userId, Long publicationId);

    List<Favorite> findByUserIdOrderByCreatedAtDesc(Long userId);

    long countByPublicationId(Long publicationId);

    void deleteByUserIdAndPublicationId(Long userId, Long publicationId);
}
