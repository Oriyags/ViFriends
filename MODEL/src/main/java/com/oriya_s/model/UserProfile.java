package com.oriya_s.model;

import com.google.firebase.firestore.PropertyName;

public class UserProfile {

    @PropertyName("username")
    private String username;

    @PropertyName("avatarUrl")
    private String avatarUrl;

    public UserProfile() {}

    public UserProfile(String username, String avatarUrl) {
        this.username = username;
        this.avatarUrl = avatarUrl;
    }

    @PropertyName("username")
    public String getUsername() {
        return username != null ? username : "(no name)";
    }

    @PropertyName("username")
    public void setUsername(String username) {
        this.username = username;
    }

    @PropertyName("avatarUrl")
    public String getAvatarUrl() {
        return avatarUrl;
    }

    @PropertyName("avatarUrl")
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    @Override
    public String toString() {
        return "UserProfile{" +
                "username='" + username + '\'' +
                ", avatarUrl='" + avatarUrl + '\'' +
                '}';
    }
}