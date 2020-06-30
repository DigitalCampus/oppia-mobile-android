package org.digitalcampus.oppia.model;

import androidx.annotation.NonNull;

public class District {

    private String id;
    private String countyId;
    private String name;

    public District(String id, String countyId, String name) {
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCountyId() {
        return countyId;
    }

    public void setCountyId(String countyId) {
        this.countyId = countyId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
