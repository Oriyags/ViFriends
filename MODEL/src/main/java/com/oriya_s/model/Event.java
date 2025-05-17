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
    private String creatorName;
    private String creatorAvatar;
    private String creatorId;

    public Event() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getVisibility() { return visibility; }
    public void setVisibility(String visibility) { this.visibility = visibility; }

    public String getImageUri() { return imageUri; }
    public void setImageUri(String imageUri) { this.imageUri = imageUri; }

    public String getVideoUri() { return videoUri; }
    public void setVideoUri(String videoUri) { this.videoUri = videoUri; }

    public List<String> getVisibleTo() { return visibleTo; }
    public void setVisibleTo(List<String> visibleTo) { this.visibleTo = visibleTo; }

    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }

    public String getCreatorAvatar() { return creatorAvatar; }
    public void setCreatorAvatar(String creatorAvatar) { this.creatorAvatar = creatorAvatar; }

    public String getCreatorId() { return creatorId; }
    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Event)) return false;
        Event event = (Event) o;
        return Objects.equals(id, event.id) &&
                Objects.equals(name, event.name) &&
                Objects.equals(description, event.description) &&
                Objects.equals(date, event.date) &&
                Objects.equals(visibility, event.visibility) &&
                Objects.equals(imageUri, event.imageUri) &&
                Objects.equals(videoUri, event.videoUri) &&
                Objects.equals(creatorName, event.creatorName) &&
                Objects.equals(creatorAvatar, event.creatorAvatar) &&
                Objects.equals(creatorId, event.creatorId);
    }
}
