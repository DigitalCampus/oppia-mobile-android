package org.digitalcampus.oppia.model.db_model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import org.digitalcampus.oppia.database.converters.TimestampConverter;

import java.util.Date;

@Entity(tableName = "leaderboard")
public class Leaderboard {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    private @NonNull long id;

    private @NonNull String username;
    private String fullname;
    private Integer points = 0;

    @TypeConverters(TimestampConverter.class)
    private Date lastupdate = new Date();


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

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public Date getLastupdate() {
        return lastupdate;
    }

    public void setLastupdate(Date lastupdate) {
        this.lastupdate = lastupdate;
    }
}
