package com.oriya_s.model;

import java.util.Objects;

public class Event {
    private String name;
    private String description;
    private String date;
    private String visibility;
    private String imageUri;
    private String videoUri;

    public Event(String name, String description, String date, String visibility, String imageUri) {
        this.name = name;
        this.description = description;
        this.date = date;
        this.visibility = visibility;
        this.imageUri = imageUri;
    }

    // Video setter
    public void setVideoUri(String videoUri) {
        this.videoUri = videoUri;
    }

    public String getVideoUri() {
        return videoUri;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getDate() {
        return date;
    }

    public String getVisibility() {
        return visibility;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Event)) return false;
        Event event = (Event) o;
        return Objects.equals(name, event.name) &&
                Objects.equals(description, event.description) &&
                Objects.equals(date, event.date) &&
                Objects.equals(visibility, event.visibility) &&
                Objects.equals(imageUri, event.imageUri) &&
                Objects.equals(videoUri, event.videoUri);
    }
}