package org.digitalcampus.oppia.model;

import java.util.Map;

public class CustomValue<T> {

    private T value;


    public CustomValue(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public static <TT> TT getSecureValue(Map<String, CustomValue> customFieldsMap, String key) {
        if (customFieldsMap.containsKey(key)) {
            return ((CustomValue<TT>) customFieldsMap.get(key)).getValue();
        } else {
            return null;
        }
    }
}
