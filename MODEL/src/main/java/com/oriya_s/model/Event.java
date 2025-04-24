package com.oriya_s.model;

import java.util.Objects;

public class Event {
    private String name;
    private String description;
    private String date;
    private String visibility;
    private String imageUri;

    public Event(String name, String description, String date, String visibility, String imageUri) {
        this.name = name;
        this.description = description;
        this.date = date;
        this.visibility = visibility;
        this.imageUri = imageUri;
    }

    // 🧾 Getters & Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public String getImageUri() {
        return imageUri;
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
                Objects.equals(imageUri, event.imageUri);
    }
}
