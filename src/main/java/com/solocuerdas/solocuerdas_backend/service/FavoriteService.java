package com.solocuerdas.solocuerdas_backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solocuerdas.solocuerdas_backend.dto.FavoriteStatusResponse;
import com.solocuerdas.solocuerdas_backend.dto.PublicationResponse;
import com.solocuerdas.solocuerdas_backend.model.Favorite;
import com.solocuerdas.solocuerdas_backend.model.Publication;
import com.solocuerdas.solocuerdas_backend.model.PublicationStatus;
import com.solocuerdas.solocuerdas_backend.model.Usuario;
import com.solocuerdas.solocuerdas_backend.repository.FavoriteRepository;
import com.solocuerdas.solocuerdas_backend.repository.PublicationRepository;
import com.solocuerdas.solocuerdas_backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * FAVORITE SERVICE
 * Business logic for user favorites.
 */
@Service
public class FavoriteService {

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private PublicationRepository publicationRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public FavoriteStatusResponse addToFavorites(Long publicationId, Long userId) {
        long publicationIdValue = Objects.requireNonNull(publicationId, "Publication ID is required");
        long userIdValue = Objects.requireNonNull(userId, "User ID is required");

        Usuario user = usuarioRepository.findById(userIdValue)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Publication publication = publicationRepository.findById(publicationIdValue)
                .orElseThrow(() -> new RuntimeException("Publication not found"));

        if (publication.getUser().getId().equals(userIdValue)) {
            throw new RuntimeException("You cannot favorite your own publication");
        }

        if (publication.getStatus() == PublicationStatus.DELETED) {
            throw new RuntimeException("Publication is not available");
        }

        if (!favoriteRepository.existsByUserIdAndPublicationId(userIdValue, publicationIdValue)) {
            Favorite favorite = new Favorite(user, publication);
            favoriteRepository.save(favorite);
        }

        long favoritesCount = favoriteRepository.countByPublicationId(publicationIdValue);
        return new FavoriteStatusResponse(publicationIdValue, userIdValue, true, favoritesCount);
    }

    @Transactional
    public FavoriteStatusResponse removeFromFavorites(Long publicationId, Long userId) {
        long publicationIdValue = Objects.requireNonNull(publicationId, "Publication ID is required");
        long userIdValue = Objects.requireNonNull(userId, "User ID is required");

        publicationRepository.findById(publicationIdValue)
                .orElseThrow(() -> new RuntimeException("Publication not found"));

        favoriteRepository.deleteByUserIdAndPublicationId(userIdValue, publicationIdValue);

        long favoritesCount = favoriteRepository.countByPublicationId(publicationIdValue);
        return new FavoriteStatusResponse(publicationIdValue, userIdValue, false, favoritesCount);
    }

    @Transactional(readOnly = true)
    public FavoriteStatusResponse getFavoriteStatus(Long publicationId, Long userId) {
        long publicationIdValue = Objects.requireNonNull(publicationId, "Publication ID is required");
        long userIdValue = Objects.requireNonNull(userId, "User ID is required");

        publicationRepository.findById(publicationIdValue)
                .orElseThrow(() -> new RuntimeException("Publication not found"));

        boolean isFavorite = favoriteRepository.existsByUserIdAndPublicationId(userIdValue, publicationIdValue);
        long favoritesCount = favoriteRepository.countByPublicationId(publicationIdValue);

        return new FavoriteStatusResponse(publicationIdValue, userIdValue, isFavorite, favoritesCount);
    }

    @Transactional(readOnly = true)
    public List<PublicationResponse> getMyFavorites(Long userId) {
        long userIdValue = Objects.requireNonNull(userId, "User ID is required");

        usuarioRepository.findById(userIdValue)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Favorite> favorites = favoriteRepository.findByUserIdOrderByCreatedAtDesc(userIdValue);

        return favorites.stream()
                .map(Favorite::getPublication)
                .filter(publication -> publication.getStatus() != PublicationStatus.DELETED)
                .map(this::mapToPublicationResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public FavoriteStatusResponse toggleFavorite(Long publicationId, Long userId) {
        long publicationIdValue = Objects.requireNonNull(publicationId, "Publication ID is required");
        long userIdValue = Objects.requireNonNull(userId, "User ID is required");

        Usuario user = usuarioRepository.findById(userIdValue)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Publication publication = publicationRepository.findById(publicationIdValue)
                .orElseThrow(() -> new RuntimeException("Publication not found"));

        if (publication.getUser().getId().equals(userIdValue)) {
            throw new RuntimeException("You cannot favorite your own publication");
        }

        if (publication.getStatus() == PublicationStatus.DELETED) {
            throw new RuntimeException("Publication is not available");
        }

        boolean isFavorite;
        Favorite existingFavorite = favoriteRepository.findByUserIdAndPublicationId(userIdValue, publicationIdValue)
                .orElse(null);

        if (existingFavorite != null) {
            favoriteRepository.delete(existingFavorite);
            isFavorite = false;
        } else {
            Favorite favorite = new Favorite(user, publication);
            favoriteRepository.save(favorite);
            isFavorite = true;
        }

        long favoritesCount = favoriteRepository.countByPublicationId(publicationIdValue);
        return new FavoriteStatusResponse(publicationIdValue, userIdValue, isFavorite, favoritesCount);
    }

    private PublicationResponse mapToPublicationResponse(Publication publication) {
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
        response.setImages(getImagesList(publication));
        response.setStatus(publication.getStatus());
        response.setUserId(publication.getUser().getId());
        response.setUserName(publication.getUser().getName());
        response.setCreatedAt(publication.getCreatedAt());
        response.setUpdatedAt(publication.getUpdatedAt());
        response.setSoldAt(publication.getSoldAt());
        response.setViewsCount(publication.getViewsCount());
        return response;
    }

    private List<String> getImagesList(Publication publication) {
        try {
            String imagesJson = publication.getImages();
            if (imagesJson == null || imagesJson.isEmpty()) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(imagesJson, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

}
