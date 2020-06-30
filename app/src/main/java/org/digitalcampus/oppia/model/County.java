package org.digitalcampus.oppia.model;

import android.content.Context;

import org.digitalcampus.oppia.database.DbHelper;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class County {

    private String id;
    private String name;
    private List<District> districts;

    public County(String id, String name, List<District> districts) {
        this.id = id;
        this.name = name;
        this.districts = districts;
    }

    public County(String name) {
        this.name = name;
    }

    public static List<County> geCountries(Context ctx) {

        DbHelper db = DbHelper.getInstance(ctx);
        List<CustomField.CollectionItem> countyCollection = db.getCollection("counties");

        List<County> counties = new ArrayList<>();

        for (CustomField.CollectionItem item : countyCollection){
            String countyID = item.getKey();
            List<CustomField.CollectionItem> districtCollection = db.getCollection(countyID);
            List<District> districts = new ArrayList<>();
            for (CustomField.CollectionItem district : districtCollection){
                districts.add(new District(district.getKey(), countyID, district.getLabel()));
            }

            counties.add(new County(countyID, item.getLabel(), districts));
        }

        return counties;
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
