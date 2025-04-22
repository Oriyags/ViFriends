package com.oriya_s.model;

import com.oriya_s.model.BASE.BaseEntity;

import java.io.Serializable;
import java.util.Objects;

public class Chat extends BaseEntity implements Serializable {
    private String UserAID;
    private String UserBID;
    private long date;
    private String message;

    public Chat() {}

    public String getUserAID() {
        return UserAID;
    }

    public void setUserAID(String userAID) {
        UserAID = userAID;
    }

    public String getUserBID() {
        return UserBID;
    }

    public void setUserBID(String userBID) {
        UserBID = userBID;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Chat)) return false;
        if (!super.equals(o)) return false;
        Chat chat = (Chat) o;
        return date == chat.date && Objects.equals(UserAID, chat.UserAID) && Objects.equals(UserBID, chat.UserBID) && Objects.equals(message, chat.message);
    }
}