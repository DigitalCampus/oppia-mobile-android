package org.digitalcampus.oppia.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ActivityCount {

    private Map<String, Integer> typeCount = new LinkedHashMap<>();

    public static ActivityCount initialize(List<ActivityType> activityTypes) {
        ActivityCount activityCount = new ActivityCount();
        for (ActivityType activityType : activityTypes) {
            activityCount.getTypeCount().put(activityType.getType(), 0);
        }
        return activityCount;
    }

    public Map<String, Integer> getTypeCount() {
        return typeCount;
    }

    public void setTypeCount(Map<String, Integer> typeCount) {
        this.typeCount = typeCount;
    }

    public void incrementNumberActivityType(String type) {
        int incrementedActivitiesNumber = typeCount.get(type) + 1;
        typeCount.put(type, incrementedActivitiesNumber);
    }

    public int getValueForType(String type) {
        Integer value = typeCount.get(type);
        return value != null ? value : 0;
    }

    public boolean hasValidEvent(String event) {
        return typeCount.containsKey(event);
    }
}
