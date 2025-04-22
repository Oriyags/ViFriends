package com.oriya_s.model;

import com.oriya_s.model.BASE.BaseEntity;

import java.io.Serializable;
import java.util.Objects;

public class Location extends BaseEntity implements Serializable {
    private String name;

    public Location() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Location)) return false;
        if (!super.equals(o)) return false;
        Location location = (Location) o;
        return Objects.equals(name, location.name);
    }
}