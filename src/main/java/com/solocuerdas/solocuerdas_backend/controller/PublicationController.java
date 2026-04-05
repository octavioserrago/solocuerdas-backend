package com.solocuerdas.solocuerdas_backend.controller;

import com.solocuerdas.solocuerdas_backend.dto.CreatePublicationRequest;
import com.solocuerdas.solocuerdas_backend.dto.FavoriteStatusResponse;
import com.solocuerdas.solocuerdas_backend.dto.PublicationResponse;
import com.solocuerdas.solocuerdas_backend.dto.UpdatePublicationRequest;
import com.solocuerdas.solocuerdas_backend.model.Category;
import com.solocuerdas.solocuerdas_backend.model.PublicationStatus;
import com.solocuerdas.solocuerdas_backend.service.FavoriteService;
import com.solocuerdas.solocuerdas_backend.service.PublicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * CONTROLLER - REST API endpoints for Publication management
 * 
 * Base URL: http://localhost:8080/api/publications
 */
@RestController
@RequestMapping("/api/publications")
public class PublicationController {

    @Autowired
    private PublicationService publicationService;

    @Autowired
    private FavoriteService favoriteService;

    /**
     * CREATE PUBLICATION
     * POST /api/publications
     * Body: CreatePublicationRequest JSON
     * Header: X-User-Id (temporary - will be replaced with JWT auth)
     */
    @PostMapping
    public ResponseEntity<?> createPublication(
            @RequestBody CreatePublicationRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        try {
            PublicationResponse response = publicationService.createPublication(request, userId);
            return new ResponseEntity<>(response, HttpStatus.CREATED); // 201
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * GET ALL ACTIVE PUBLICATIONS
     * GET /api/publications
     */
    @GetMapping
    public ResponseEntity<List<PublicationResponse>> getAllActivePublications() {
        List<PublicationResponse> publications = publicationService.getAllActivePublications();
        return new ResponseEntity<>(publications, HttpStatus.OK); // 200
    }

    /**
     * GET PUBLICATION BY ID
     * GET /api/publications/1
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getPublicationById(@PathVariable Long id) {
        try {
            PublicationResponse response = publicationService.getPublicationById(id);
            return new ResponseEntity<>(response, HttpStatus.OK); // 200
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error: " + e.getMessage()); // 404
        }
    }

    /**
     * UPDATE PUBLICATION
     * PUT /api/publications/1
     * Body: UpdatePublicationRequest JSON
     * Header: X-User-Id
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePublication(
            @PathVariable Long id,
            @RequestBody UpdatePublicationRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        try {
            PublicationResponse response = publicationService.updatePublication(id, request, userId);
            return new ResponseEntity<>(response, HttpStatus.OK); // 200
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * DELETE PUBLICATION
     * DELETE /api/publications/1
     * Header: X-User-Id
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePublication(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        try {
            publicationService.deletePublication(id, userId);
            return ResponseEntity.ok("Publication deleted successfully"); // 200
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * GET MY PUBLICATIONS
     * GET /api/publications/my-publications
     * Header: X-User-Id
     */
    @GetMapping("/my-publications")
    public ResponseEntity<List<PublicationResponse>> getMyPublications(
            @RequestHeader("X-User-Id") Long userId) {
        List<PublicationResponse> publications = publicationService.getMyPublications(userId);
        return new ResponseEntity<>(publications, HttpStatus.OK); // 200
    }

    /**
     * ADD TO FAVORITES
     * POST /api/publications/{id}/favorite
     * Header: X-User-Id
     */
    @PostMapping("/{id}/favorite")
    public ResponseEntity<?> addToFavorites(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        try {
            FavoriteStatusResponse response = favoriteService.addToFavorites(id, userId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * REMOVE FROM FAVORITES
     * DELETE /api/publications/{id}/favorite
     * Header: X-User-Id
     */
    @DeleteMapping("/{id}/favorite")
    public ResponseEntity<?> removeFromFavorites(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        try {
            FavoriteStatusResponse response = favoriteService.removeFromFavorites(id, userId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * GET FAVORITE STATUS FOR AUTHENTICATED USER
     * GET /api/publications/{id}/favorite/status
     * Header: X-User-Id
     */
    @GetMapping("/{id}/favorite/status")
    public ResponseEntity<?> getFavoriteStatus(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        try {
            FavoriteStatusResponse response = favoriteService.getFavoriteStatus(id, userId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * TOGGLE FAVORITE
     * POST /api/publications/{id}/favorite/toggle
     * Header: X-User-Id
     */
    @PostMapping("/{id}/favorite/toggle")
    public ResponseEntity<?> toggleFavorite(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        try {
            FavoriteStatusResponse response = favoriteService.toggleFavorite(id, userId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * GET MY FAVORITES
     * GET /api/publications/my-favorites
     * Header: X-User-Id
     */
    @GetMapping("/my-favorites")
    public ResponseEntity<?> getMyFavorites(@RequestHeader("X-User-Id") Long userId) {
        try {
            List<PublicationResponse> favorites = favoriteService.getMyFavorites(userId);
            return new ResponseEntity<>(favorites, HttpStatus.OK);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * GET PUBLICATIONS BY USER
     * GET /api/publications/user/1
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PublicationResponse>> getPublicationsByUser(@PathVariable Long userId) {
        List<PublicationResponse> publications = publicationService.getPublicationsByUser(userId);
        return new ResponseEntity<>(publications, HttpStatus.OK); // 200
    }

    /**
     * CHANGE PUBLICATION STATUS
     * PATCH /api/publications/1/status
     * Body: { "status": "PAUSED" } or "ACTIVE" or "SOLD"
     * Header: X-User-Id
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> changeStatus(
            @PathVariable Long id,
            @RequestParam PublicationStatus status,
            @RequestHeader("X-User-Id") Long userId) {
        try {
            PublicationResponse response = publicationService.changeStatus(id, status, userId);
            return new ResponseEntity<>(response, HttpStatus.OK); // 200
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * UPLOAD IMAGES TO PUBLICATION
     * POST /api/publications/1/images
     * Form-data: files (multiple files)
     * Header: X-User-Id
     */
    @PostMapping("/{id}/images")
    public ResponseEntity<?> uploadImages(
            @PathVariable Long id,
            @RequestParam("files") List<MultipartFile> files,
            @RequestHeader("X-User-Id") Long userId) {
        try {
            PublicationResponse response = publicationService.uploadImages(id, files, userId);
            return new ResponseEntity<>(response, HttpStatus.OK); // 200
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * DELETE IMAGE FROM PUBLICATION
     * DELETE /api/publications/1/images
     * Body: { "imageUrl": "https://..." }
     * Header: X-User-Id
     */
    @DeleteMapping("/{id}/images")
    public ResponseEntity<?> deleteImage(
            @PathVariable Long id,
            @RequestParam String imageUrl,
            @RequestHeader("X-User-Id") Long userId) {
        try {
            PublicationResponse response = publicationService.deleteImage(id, imageUrl, userId);
            return new ResponseEntity<>(response, HttpStatus.OK); // 200
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * SEARCH PUBLICATIONS
     * GET /api/publications/search?keyword=fender
     */
    @GetMapping("/search")
    public ResponseEntity<List<PublicationResponse>> searchPublications(@RequestParam String keyword) {
        List<PublicationResponse> publications = publicationService.searchPublications(keyword);
        return new ResponseEntity<>(publications, HttpStatus.OK); // 200
    }

    /**
     * FILTER BY CATEGORY
     * GET /api/publications/category/ELECTRIC_GUITAR
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<PublicationResponse>> getPublicationsByCategory(@PathVariable Category category) {
        List<PublicationResponse> publications = publicationService.getPublicationsByCategory(category);
        return new ResponseEntity<>(publications, HttpStatus.OK); // 200
    }
}
