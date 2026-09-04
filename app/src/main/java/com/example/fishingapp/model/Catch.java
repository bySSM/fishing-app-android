package com.example.fishingapp.model;

import com.example.fishingapp.utils.Config;

public class Catch {

    private Long id;
    private String fishType;
    private Double weight;
    private Double length;
    private Double latitude;
    private Double longitude;
    private String bait;
    private String description;
    private String photoUrl;
    private String createdAt;
    private Integer likesCount;
    private Boolean isLocationHidden;
    private String username;

    /*
     * Локальное состояние Android.
     *
     * Backend это поле не возвращает.
     * Оно используется только для отображения:
     * лайкнул ли текущий пользователь этот улов.
     */
    private Boolean likedByCurrentUser = false;


    // ============================================================
    // Getters / Setters
    // ============================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getFishType() {
        return fishType;
    }

    public void setFishType(String fishType) {
        this.fishType = fishType;
    }


    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }


    public Double getLength() {
        return length;
    }

    public void setLength(Double length) {
        this.length = length;
    }


    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }


    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }


    public String getBait() {
        return bait;
    }

    public void setBait(String bait) {
        this.bait = bait;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }


    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }


    public Integer getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(Integer likesCount) {
        this.likesCount = likesCount;
    }


    public Boolean getIsLocationHidden() {
        return isLocationHidden;
    }

    public void setIsLocationHidden(Boolean isLocationHidden) {
        this.isLocationHidden = isLocationHidden;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    // ============================================================
    // Current user like state
    // ============================================================

    public Boolean getLikedByCurrentUser() {
        return likedByCurrentUser;
    }

    public void setLikedByCurrentUser(Boolean likedByCurrentUser) {
        this.likedByCurrentUser = likedByCurrentUser;
    }


    // ============================================================
    // Photo URL
    // ============================================================

    public String getFullPhotoUrl() {

        if (photoUrl != null && !photoUrl.isEmpty()) {
            return Config.BASE_URL + photoUrl;
        }

        return null;
    }
}