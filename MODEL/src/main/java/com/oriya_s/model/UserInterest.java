package com.oriya_s.model;

import com.oriya_s.model.BASE.BaseEntity;

import java.io.Serializable;
import java.util.Objects;

public class UserInterest extends BaseEntity implements Serializable {
    private String UserID;
    private String InterestID;

    public UserInterest() {}

    public String getUserID() {
        return UserID;
    }

    public void setUserID(String userID) {
        UserID = userID;
    }

    public String getInterestID() {
        return InterestID;
    }

    public void setInterestID(String interestID) {
        InterestID = interestID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserInterest)) return false;
        if (!super.equals(o)) return false;
        UserInterest that = (UserInterest) o;
        return Objects.equals(UserID, that.UserID) && Objects.equals(InterestID, that.InterestID);
    }
}
