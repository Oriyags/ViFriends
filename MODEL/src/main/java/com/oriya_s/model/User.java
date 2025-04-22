package com.oriya_s.model;

import com.oriya_s.model.BASE.BaseEntity;

import java.io.Serializable;
import java.util.Objects;

public class User extends BaseEntity implements Serializable {
    private String username;
    private String email;
    private String phoneNumber;
    private String profilePicture;
    private String bio;
    private String locationID;
    private String password;
    private String photo;
    private long DOB;

    public User() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getLocationID() {
        return locationID;
    }

    public void setLocationID(String locationID) {
        this.locationID = locationID;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public long getDOB() {
        return DOB;
    }

    public void setDOB(long DOB) {
        this.DOB = DOB;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        if (!super.equals(o)) return false;
        User user = (User) o;
        return DOB == user.DOB && Objects.equals(username, user.username) && Objects.equals(email, user.email) && Objects.equals(phoneNumber, user.phoneNumber) && Objects.equals(profilePicture, user.profilePicture) && Objects.equals(bio, user.bio) && Objects.equals(locationID, user.locationID) && Objects.equals(password, user.password) && Objects.equals(photo, user.photo);
    }
}