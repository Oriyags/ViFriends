package com.oriya_s.model;

import com.oriya_s.model.BASE.BaseEntity;

import java.io.Serializable;
import java.util.Objects;

public class Friend extends BaseEntity implements Serializable {
    private String FriendID;
    private String UserID;

    public Friend() {}

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Friend)) return false;
        if (!super.equals(o)) return false;
        Friend friend = (Friend) o;
        return Objects.equals(FriendID, friend.FriendID) && Objects.equals(UserID, friend.UserID);
    }
}
