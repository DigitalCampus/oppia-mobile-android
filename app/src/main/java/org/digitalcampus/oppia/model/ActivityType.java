package org.digitalcampus.oppia.model;

import java.util.ArrayList;
import java.util.List;

public class ActivityType {

    public static final String ALL = "all";

    private String name;
    private String type;
    private int color;
    private boolean enabled;

    private List<Integer> values = new ArrayList<>();

    public ActivityType(String name, String type, int color, boolean enabled) {
        this.name = name;
        this.type = type;
        this.color = color;
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Integer> getValues() {
        return values;
    }

    public void setValues(List<Integer> values) {
        this.values = values;
    }
}
