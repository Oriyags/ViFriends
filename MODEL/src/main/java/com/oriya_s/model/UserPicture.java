package com.oriya_s.model;

import com.oriya_s.model.BASE.BaseEntity;

import java.io.Serializable;
import java.util.Objects;

public class UserPicture extends BaseEntity implements Serializable {
    private String picture;
    private String UserID;
    private long   date;
    private String description;

    public UserPicture() {}

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getUserID() {
        return UserID;
    }

    public void setUserID(String userID) {
        UserID = userID;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserPicture)) return false;
        if (!super.equals(o)) return false;
        UserPicture that = (UserPicture) o;
        return date == that.date && Objects.equals(picture, that.picture) && Objects.equals(UserID, that.UserID) && Objects.equals(description, that.description);
    }
}
