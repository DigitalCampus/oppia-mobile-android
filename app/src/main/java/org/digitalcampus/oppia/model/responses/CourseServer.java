package org.digitalcampus.oppia.model.responses;

import android.text.TextUtils;

import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.utils.TextUtilsJava;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CourseServer {


    private int id;
    private String shortname;
    private Double version;
    private String organisation;
    private String author;
    private Map<String, String> title = new HashMap<>();
    private Map<String, String> description = new HashMap<>();

    private int priority = 0;

    private String status = Course.STATUS_LIVE;

    private boolean isRestricted = false;
    private List<Integer> cohorts;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getShortname() {
        return shortname;
    }

    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    public Double getVersion() {
        return version;
    }

    public void setVersion(Double version) {
        this.version = version;
    }

    public String getOrganisation() {
        return organisation;
    }

    public void setOrganisation(String organisation) {
        this.organisation = organisation;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Map<String, String> getTitle() {
        return title;
    }

    public void setTitle(Map<String, String> title) {
        this.title = title;
    }

    public Map<String, String> getDescription() {
        return description;
    }

    public void setDescription(Map<String, String> description) {
        this.description = description;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean hasStatus(String status) {
        return TextUtilsJava.equals(this.status, status);
    }

    public boolean isRestricted() {
        return isRestricted;
    }

    public void setRestricted(boolean isRestricted) {
        this.isRestricted = isRestricted;
    }

    public List<Integer> getRestrictedCohorts() {
        return cohorts;
    }

    public void setRestrictedCohorts(List<Integer> cohorts) {
        this.cohorts = cohorts;
    }
}
