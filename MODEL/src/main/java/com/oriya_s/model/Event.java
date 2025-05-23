package com.oriya_s.model;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class Event implements Serializable {
    private String id;
    private String name;
    private String description;
    private String date;
    private String visibility;
    private String imageUri;
    private String videoUri;
    private List<String> visibleTo;
    private List<String> acceptedUserIds;
    private String creatorName;
    private String creatorAvatar;
    private String creatorId;

    private double latitude;
    private double longitude;
    private String address;

    public Event() {
    }

    // ID
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // Name
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Description
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Date
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    // Visibility
    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    // Media
    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getVideoUri() {
        return videoUri;
    }

    public void setVideoUri(String videoUri) {
        this.videoUri = videoUri;
    }

    // Friends
    public List<String> getVisibleTo() {
        return visibleTo;
    }

    public void setVisibleTo(List<String> visibleTo) {
        this.visibleTo = visibleTo;
    }

    public List<String> getAcceptedUserIds() {
        return acceptedUserIds;
    }

    public void setAcceptedUserIds(List<String> acceptedUserIds) {
        this.acceptedUserIds = acceptedUserIds;
    }

    // Creator Info
    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public String getCreatorAvatar() {
        return creatorAvatar;
    }

    public void setCreatorAvatar(String creatorAvatar) {
        this.creatorAvatar = creatorAvatar;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    // Location
    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    // Equality (based on ID)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Event)) return false;
        Event event = (Event) o;
        return Objects.equals(id, event.id);
    }
}