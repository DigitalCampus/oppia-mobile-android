package org.digitalcampus.oppia.model;

import androidx.annotation.NonNull;

import java.util.Map;

public class CustomValue<T> {

    private T value;


    public CustomValue(T value) {
        setValue(value);
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        if (value instanceof String || value instanceof Boolean || value instanceof Integer || value instanceof Float) {
            this.value = value;
        } else {
            throw new IllegalArgumentException("value must one of these types: String, int, float or boolean");
        }
    }

    public static <V> V getSecureValue(Map<String, CustomValue> customFieldsMap, String key) {
        if (customFieldsMap.containsKey(key)) {
            return ((CustomValue<V>) customFieldsMap.get(key)).getValue();
        } else {
            return null;
        }
    }

    @NonNull
    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
