package org.digitalcampus.oppia.model;

import androidx.annotation.NonNull;

public class District {

    private long id;
    private long countyId;
    private String name;

    public District(long id, long countyId, String name) {
        this.id = id;
        this.countyId = countyId;
        this.name = name;
    }

    public District(String name) {
        this.name = name;
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCountyId() {
        return countyId;
    }

    public void setCountyId(long countyId) {
        this.countyId = countyId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
