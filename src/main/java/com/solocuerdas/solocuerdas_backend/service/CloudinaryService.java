package com.solocuerdas.solocuerdas_backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * CLOUDINARY SERVICE
 * Handles image upload and deletion with Cloudinary
 */
@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    @Value("${cloudinary.folder}")
    private String folder;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_FORMATS = { "jpg", "jpeg", "png", "webp" };

    /**
     * Upload image to Cloudinary
     * 
     * @param file The image file to upload
     * @return The URL of the uploaded image
     * @throws IOException              if upload fails
     * @throws IllegalArgumentException if file validation fails
     */
    public String uploadImage(MultipartFile file) throws IOException {
        // Validate file
        validateFile(file);

        // Upload to Cloudinary
        @SuppressWarnings("unchecked")
        Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap(
                        "folder", folder,
                        "resource_type", "image",
                        "quality", "auto",
                        "fetch_format", "auto"));

        // Return secure URL
        return (String) uploadResult.get("secure_url");
    }

    /**
     * Delete image from Cloudinary
     * 
     * @param imageUrl The URL of the image to delete
     * @throws IOException if deletion fails
     */
    public void deleteImage(String imageUrl) throws IOException {
        // Extract public_id from URL
        String publicId = extractPublicId(imageUrl);

        if (publicId != null) {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        }
    }

    /**
     * Validate uploaded file
     * - Check size (max 5MB)
     * - Check format (jpg, png, webp)
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Check size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds 5MB limit");
        }

        // Check format
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new IllegalArgumentException("Invalid filename");
        }

        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        boolean isValidFormat = false;
        for (String format : ALLOWED_FORMATS) {
            if (format.equals(extension)) {
                isValidFormat = true;
                break;
            }
        }

        if (!isValidFormat) {
            throw new IllegalArgumentException("Invalid file format. Allowed: jpg, jpeg, png, webp");
        }
    }

    /**
     * Extract public_id from Cloudinary URL
     * Example:
     * https://res.cloudinary.com/demo/image/upload/v1234567/solocuerdas/publications/abc123.jpg
     * Returns: solocuerdas/publications/abc123
     */
    private String extractPublicId(String imageUrl) {
        if (imageUrl == null || !imageUrl.contains("cloudinary.com")) {
            return null;
        }

        try {
            // Find the part after "/upload/"
            int uploadIndex = imageUrl.indexOf("/upload/");
            if (uploadIndex == -1)
                return null;

            String afterUpload = imageUrl.substring(uploadIndex + 8);

            // Remove version (v1234567/)
            int slashIndex = afterUpload.indexOf("/");
            if (slashIndex != -1) {
                afterUpload = afterUpload.substring(slashIndex + 1);
            }

            // Remove file extension
            int dotIndex = afterUpload.lastIndexOf(".");
            if (dotIndex != -1) {
                afterUpload = afterUpload.substring(0, dotIndex);
            }

            return afterUpload;
        } catch (Exception e) {
            return null;
        }
    }
}
