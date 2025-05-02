package com.oriya_s.model;

import com.oriya_s.model.BASE.BaseEntity;

import java.io.Serializable;
import java.util.Objects;

public class Friend extends BaseEntity implements Serializable {
    private String FriendID;
    private String UserID;
    private String name;
    private String avatarUrl;

    public Friend() {}

    public Friend(String friendID, String name, String avatarUrl) {
        this.FriendID = friendID;
        this.name = name;
        this.avatarUrl = avatarUrl;
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
        return name;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Friend)) return false;
        if (!super.equals(o)) return false;
        Friend friend = (Friend) o;
        return Objects.equals(FriendID, friend.FriendID)
                && Objects.equals(UserID, friend.UserID)
                && Objects.equals(name, friend.name)
                && Objects.equals(avatarUrl, friend.avatarUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), FriendID, UserID, name, avatarUrl);
    }
}