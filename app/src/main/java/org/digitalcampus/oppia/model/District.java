package org.digitalcampus.oppia.model;

import androidx.annotation.NonNull;

public class District {

    private long id;
    private long countryId;
    private String name;

    public District(long id, long countryId, String name) {
        this.id = id;
        this.countryId = countryId;
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

    public long getCountryId() {
        return countryId;
    }

    public void setCountryId(long countryId) {
        this.countryId = countryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
