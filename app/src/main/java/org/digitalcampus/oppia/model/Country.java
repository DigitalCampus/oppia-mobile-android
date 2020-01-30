package org.digitalcampus.oppia.model;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class Country {

    private long id;
    private String name;
    private List<District> districts;

    public Country(long id, String name, List<District> districts) {
        this.id = id;
        this.name = name;
        this.districts = districts;
    }

    public Country(String name) {
        this.name = name;
    }

    public static List<Country> getDemoCountries() {
        List<Country> countries = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {

            List<District> districts = new ArrayList<>();
            for (int j = 1; j <= 4; j++) {
                districts.add(new District(j, i, "District " + (i + j)));
            }

            countries.add(new Country(i, "Country " + i, districts));
        }

        return countries;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<District> getDistricts() {
        return districts;
    }

    public void setDistricts(List<District> districts) {
        this.districts = districts;
    }
}
