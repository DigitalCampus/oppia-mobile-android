package org.digitalcampus.oppia.model.db_model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_custom_field")
public class UserCustomField {


    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    private @NonNull long id;
    private @NonNull String username;

    @ColumnInfo(name = "field_key")
    private @NonNull String fieldKey;

    @ColumnInfo(name = "value_str")
    private String valueStr;

    @ColumnInfo(name = "value_int")
    private Integer valueInt;

    @ColumnInfo(name = "value_bool")
    private Boolean valueBool;

    @ColumnInfo(name = "value_float")
    private Float valueFloat;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getUsername() {
        return username;
    }

    public void setUsername(@NonNull String username) {
        this.username = username;
    }

    @NonNull
    public String getFieldKey() {
        return fieldKey;
    }

    public void setFieldKey(@NonNull String fieldKey) {
        this.fieldKey = fieldKey;
    }

    @NonNull
    public String getValueStr() {
        return valueStr;
    }

    public void setValueStr(@NonNull String valueStr) {
        this.valueStr = valueStr;
    }

    @NonNull
    public Integer getValueInt() {
        return valueInt;
    }

    public void setValueInt(@NonNull Integer valueInt) {
        this.valueInt = valueInt;
    }

    @NonNull
    public Boolean getValueBool() {
        return valueBool;
    }

    public void setValueBool(@NonNull Boolean valueBool) {
        this.valueBool = valueBool;
    }

    @NonNull
    public Float getValueFloat() {
        return valueFloat;
    }

    public void setValueFloat(@NonNull Float valueFloat) {
        this.valueFloat = valueFloat;
    }
}
