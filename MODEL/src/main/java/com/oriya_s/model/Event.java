package com.oriya_s.model;

import java.io.Serializable;
import java.util.Objects;

public class Event implements Serializable {

    private String id; // 🆕 מזהה Firebase
    private String name;
    private String description;
    private String date;
    private String visibility;
    private String imageUri;
    private String videoUri;

    public Event() {}

    public Event(String name, String description, String date, String visibility, String imageUri) {
        this.name = name;
        this.description = description;
        this.date = date;
        this.visibility = visibility;
        this.imageUri = imageUri;
    }

    // 🆕 Getter & Setter for ID
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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
                Objects.equals(videoUri, event.videoUri);
    }
}