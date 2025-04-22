package com.oriya_s.model;

import com.oriya_s.model.BASE.BaseEntity;

import java.io.Serializable;
import java.util.Objects;

public class Interest extends BaseEntity implements Serializable {
    private String name;

    public Interest() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Interest)) return false;
        if (!super.equals(o)) return false;
        Interest interest = (Interest) o;
        return Objects.equals(name, interest.name);
    }
}
