package com.oriya_s.model;

import com.oriya_s.model.BASE.BaseEntity;

import java.io.Serializable;
import java.util.Objects;

public class GroupMember extends BaseEntity implements Serializable {
    private String name;
    private String GroupID;
    private String UserID;

    public GroupMember() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroupID() {
        return GroupID;
    }

    public void setGroupID(String groupID) {
        GroupID = groupID;
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
        if (!(o instanceof GroupMember)) return false;
        if (!super.equals(o)) return false;
        GroupMember that = (GroupMember) o;
        return Objects.equals(name, that.name) && Objects.equals(GroupID, that.GroupID) && Objects.equals(UserID, that.UserID);
    }
}
