package org.digitalcampus.oppia.model.db_model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_preference", indices = {@Index(name = "idx", value = {"username", "preference"}, unique = true)})
public class UserPreference {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    private @NonNull long id;
    private @NonNull String username;
    private @NonNull String preference;
    private String value;

    public UserPreference(@NonNull String username, @NonNull String preference, String value) {
        this.username = username;
        this.preference = preference;
        this.value = value;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPreference() {
        return preference;
    }

    public void setPreference(String preference) {
        this.preference = preference;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
