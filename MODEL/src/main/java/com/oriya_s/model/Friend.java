package com.oriya_s.model;

import com.oriya_s.model.BASE.BaseEntity;
import java.io.Serializable;
import java.util.Objects;

public class Friend extends BaseEntity implements Serializable {
    private String FriendID;
    private String UserID;
    private String name;
    private String avatarUrl;
    private String status; // "pending" or "accepted"

    public Friend() {}

    public Friend(String friendID, String name, String avatarUrl, String userID, String status) {
        this.FriendID = friendID;
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.UserID = userID;
        this.status = status;
    }

    public String getFriendID() {
        return FriendID;
    }

    public void setFriendID(String friendID) {
        FriendID = friendID;
    }

    public String getUserID() {
        return UserID;
    }

    public void setUserID(String userID) {
        UserID = userID;
    }

    public String getName() {
        return name != null ? name : ""; // Ensure non-null for dialog display
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Friend)) return false;
        if (!super.equals(o)) return false;
        Friend friend = (Friend) o;
        return Objects.equals(FriendID, friend.FriendID)
                && Objects.equals(UserID, friend.UserID)
                && Objects.equals(name, friend.name)
                && Objects.equals(avatarUrl, friend.avatarUrl)
                && Objects.equals(status, friend.status);
    }
}