package com.oriya_s.model;

import java.util.Objects;

public class Event {
    public String name;
    public String description;
    public String date;
    public String visibility;

    public Event(String name, String description, String date, String visibility) {
        this.name = name;
        this.description = description;
        this.date = date;
        this.visibility = visibility;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Event)) return false;
        Event event = (Event) o;
        return Objects.equals(name, event.name) && Objects.equals(description, event.description) && Objects.equals(date, event.date) && Objects.equals(visibility, event.visibility);
    }
}
