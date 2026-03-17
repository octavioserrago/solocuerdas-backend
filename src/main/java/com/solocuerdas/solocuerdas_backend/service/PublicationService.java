package com.solocuerdas.solocuerdas_backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solocuerdas.solocuerdas_backend.dto.CreatePublicationRequest;
import com.solocuerdas.solocuerdas_backend.dto.PublicationResponse;
import com.solocuerdas.solocuerdas_backend.dto.UpdatePublicationRequest;
import com.solocuerdas.solocuerdas_backend.model.*;
import com.solocuerdas.solocuerdas_backend.repository.PublicationRepository;
import com.solocuerdas.solocuerdas_backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * PUBLICATION SERVICE
 * Business logic for publications (listings)
 */
@Service
public class PublicationService {

    @Autowired
    private PublicationRepository publicationRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final int MAX_IMAGES = 5;

    /**
     * CREATE PUBLICATION
     * Validates role limits and creates new publication
     */
    public PublicationResponse createPublication(CreatePublicationRequest request, Long userId) {
        // Find user
        Usuario user = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Validate publication limit by role
        validatePublicationLimit(user);

        // Validate required fields
        validatePublicationData(request);

        // Create publication
        Publication publication = new Publication(
                request.getTitle(),
                request.getDescription(),
                request.getPrice(),
                request.getCategory(),
                request.getCondition(),
                request.getLocation(),
                user);

        publication.setBrand(request.getBrand());
        publication.setYear(request.getYear());

        // Save publication
        Publication savedPublication = publicationRepository.save(publication);

        return mapToResponse(savedPublication);
    }

    /**
     * UPDATE PUBLICATION
     * Only owner can update
     */
    public PublicationResponse updatePublication(Long publicationId, UpdatePublicationRequest request, Long userId) {
        Publication publication = publicationRepository.findById(publicationId)
                .orElseThrow(() -> new RuntimeException("Publication not found"));

        // Check ownership
        if (!publication.getUser().getId().equals(userId)) {
            throw new RuntimeException("You don't have permission to update this publication");
        }

        // Update fields if provided
        if (request.getTitle() != null) {
            publication.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            publication.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            publication.setPrice(request.getPrice());
        }
        if (request.getCategory() != null) {
            publication.setCategory(request.getCategory());
        }
        if (request.getCondition() != null) {
            publication.setCondition(request.getCondition());
        }
        if (request.getBrand() != null) {
            publication.setBrand(request.getBrand());
        }
        if (request.getYear() != null) {
            publication.setYear(request.getYear());
        }
        if (request.getLocation() != null) {
            publication.setLocation(request.getLocation());
        }

        Publication updatedPublication = publicationRepository.save(publication);
        return mapToResponse(updatedPublication);
    }

    /**
     * DELETE PUBLICATION (soft delete)
     * Only owner or admin can delete
     */
    public void deletePublication(Long publicationId, Long userId) {
        Publication publication = publicationRepository.findById(publicationId)
                .orElseThrow(() -> new RuntimeException("Publication not found"));

        // Check ownership (admins can be checked in controller)
        if (!publication.getUser().getId().equals(userId)) {
            throw new RuntimeException("You don't have permission to delete this publication");
        }

        // Delete images from Cloudinary
        try {
            List<String> imageUrls = getImagesList(publication);
            for (String imageUrl : imageUrls) {
                cloudinaryService.deleteImage(imageUrl);
            }
        } catch (Exception e) {
            // Log error but continue with deletion
            System.err.println("Error deleting images: " + e.getMessage());
        }

        // Soft delete
        publication.delete();
        publicationRepository.save(publication);
    }

    /**
     * GET ALL ACTIVE PUBLICATIONS
     */
    public List<PublicationResponse> getAllActivePublications() {
        List<Publication> publications = publicationRepository
                .findByStatusOrderByCreatedAtDesc(PublicationStatus.ACTIVE);
        return publications.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * GET PUBLICATION BY ID
     * Increments view count
     */
    public PublicationResponse getPublicationById(Long id) {
        Publication publication = publicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Publication not found"));

        // Increment views
        publication.incrementViews();
        publicationRepository.save(publication);

        return mapToResponse(publication);
    }

    /**
     * GET MY PUBLICATIONS
     */
    public List<PublicationResponse> getMyPublications(Long userId) {
        List<Publication> publications = publicationRepository.findByUserId(userId);
        return publications.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * GET PUBLICATIONS BY USER
     */
    public List<PublicationResponse> getPublicationsByUser(Long userId) {
        List<Publication> publications = publicationRepository.findByUserIdAndStatus(userId, PublicationStatus.ACTIVE);
        return publications.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * CHANGE PUBLICATION STATUS
     */
    public PublicationResponse changeStatus(Long publicationId, PublicationStatus newStatus, Long userId) {
        Publication publication = publicationRepository.findById(publicationId)
                .orElseThrow(() -> new RuntimeException("Publication not found"));

        // Check ownership
        if (!publication.getUser().getId().equals(userId)) {
            throw new RuntimeException("You don't have permission to change this publication status");
        }

        publication.setStatus(newStatus);
        if (newStatus == PublicationStatus.SOLD) {
            publication.markAsSold();
        }

        Publication updatedPublication = publicationRepository.save(publication);
        return mapToResponse(updatedPublication);
    }

    /**
     * UPLOAD IMAGES TO PUBLICATION
     */
    public PublicationResponse uploadImages(Long publicationId, List<MultipartFile> files, Long userId)
            throws IOException {
        Publication publication = publicationRepository.findById(publicationId)
                .orElseThrow(() -> new RuntimeException("Publication not found"));

        // Check ownership
        if (!publication.getUser().getId().equals(userId)) {
            throw new RuntimeException("You don't have permission to upload images to this publication");
        }

        // Get current images
        List<String> currentImages = getImagesList(publication);

        // Validate total images count
        if (currentImages.size() + files.size() > MAX_IMAGES) {
            throw new RuntimeException("Maximum " + MAX_IMAGES + " images allowed per publication");
        }

        // Upload each image
        for (MultipartFile file : files) {
            String imageUrl = cloudinaryService.uploadImage(file);
            currentImages.add(imageUrl);
        }

        // Save images as JSON
        publication.setImages(objectMapper.writeValueAsString(currentImages));
        Publication updatedPublication = publicationRepository.save(publication);

        return mapToResponse(updatedPublication);
    }

    /**
     * DELETE IMAGE FROM PUBLICATION
     */
    public PublicationResponse deleteImage(Long publicationId, String imageUrl, Long userId) throws IOException {
        Publication publication = publicationRepository.findById(publicationId)
                .orElseThrow(() -> new RuntimeException("Publication not found"));

        // Check ownership
        if (!publication.getUser().getId().equals(userId)) {
            throw new RuntimeException("You don't have permission to delete images from this publication");
        }

        // Get current images
        List<String> currentImages = getImagesList(publication);

        // Remove image
        if (!currentImages.remove(imageUrl)) {
            throw new RuntimeException("Image not found in publication");
        }

        // Delete from Cloudinary
        cloudinaryService.deleteImage(imageUrl);

        // Save updated images
        publication.setImages(objectMapper.writeValueAsString(currentImages));
        Publication updatedPublication = publicationRepository.save(publication);

        return mapToResponse(updatedPublication);
    }

    /**
     * SEARCH PUBLICATIONS
     */
    public List<PublicationResponse> searchPublications(String keyword) {
        List<Publication> publications = publicationRepository.searchByKeyword(keyword);
        return publications.stream()
                .filter(p -> p.getStatus() == PublicationStatus.ACTIVE)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * FILTER BY CATEGORY
     */
    public List<PublicationResponse> getPublicationsByCategory(Category category) {
        List<Publication> publications = publicationRepository
                .findByStatusAndCategory(PublicationStatus.ACTIVE, category);
        return publications.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ============ HELPER METHODS ============

    /**
     * Validate publication limit based on user role
     */
    private void validatePublicationLimit(Usuario user) {
        long activePublications = publicationRepository.countByUserAndStatus(user, PublicationStatus.ACTIVE);

        switch (user.getRole()) {
            case USER:
                if (activePublications >= 2) {
                    throw new RuntimeException("USER role limit: maximum 2 active publications");
                }
                break;
            case VENDEDOR:
                if (activePublications >= 10) {
                    throw new RuntimeException("VENDEDOR role limit: maximum 10 active publications");
                }
                break;
            case PRO_SELLER:
                // No limit for PRO_SELLER
                break;
            default:
                // ADMIN, MODERATOR, SUPER_ADMIN have no limits
                break;
        }
    }

    /**
     * Validate publication data
     */
    private void validatePublicationData(CreatePublicationRequest request) {
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new RuntimeException("Title is required");
        }
        if (request.getTitle().length() < 10 || request.getTitle().length() > 100) {
            throw new RuntimeException("Title must be between 10 and 100 characters");
        }
        if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
            throw new RuntimeException("Description is required");
        }
        if (request.getPrice() == null || request.getPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Price must be greater than 0");
        }
        if (request.getCategory() == null) {
            throw new RuntimeException("Category is required");
        }
        if (request.getCondition() == null) {
            throw new RuntimeException("Condition is required");
        }
        if (request.getLocation() == null || request.getLocation().trim().isEmpty()) {
            throw new RuntimeException("Location is required");
        }
    }

    /**
     * Get images list from JSON string
     */
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

    /**
     * Map Publication entity to PublicationResponse DTO
     */
    private PublicationResponse mapToResponse(Publication publication) {
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
}
