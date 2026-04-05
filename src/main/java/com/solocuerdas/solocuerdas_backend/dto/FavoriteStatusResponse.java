package com.solocuerdas.solocuerdas_backend.dto;

/**
 * DTO used by favorites endpoints.
 */
public class FavoriteStatusResponse {

    private Long publicationId;
    private Long userId;
    private boolean favorite;
    private long favoritesCount;

    public FavoriteStatusResponse() {
    }

    public FavoriteStatusResponse(Long publicationId, Long userId, boolean favorite, long favoritesCount) {
        this.publicationId = publicationId;
        this.userId = userId;
        this.favorite = favorite;
        this.favoritesCount = favoritesCount;
    }

    public Long getPublicationId() {
        return publicationId;
    }

    public void setPublicationId(Long publicationId) {
        this.publicationId = publicationId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public long getFavoritesCount() {
        return favoritesCount;
    }

    public void setFavoritesCount(long favoritesCount) {
        this.favoritesCount = favoritesCount;
    }
}
