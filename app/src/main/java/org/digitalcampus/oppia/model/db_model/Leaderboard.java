package org.digitalcampus.oppia.model.db_model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import org.digitalcampus.oppia.database.converters.TimestampConverter;
import org.joda.time.DateTime;

@Entity(tableName = "leaderboard")
public class Leaderboard  implements Comparable<Leaderboard>{

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    private long id;

    private String username;
    private String fullname;
    private Integer points = 0;

    @TypeConverters(TimestampConverter.class)
    private DateTime lastupdate = new DateTime();

    @Ignore
    private boolean isUser;


    @Ignore
    public Leaderboard(@NonNull String username, String fullname, Integer points, DateTime lastupdate) {
        this.username = username;
        this.fullname = fullname;
        this.points = points;
        this.lastupdate = lastupdate;
    }

    @Ignore
    public Leaderboard(@NonNull String username, String fullname, Integer points, String lastupdateStr) {
        this.username = username;
        this.fullname = fullname;
        this.points = points;
        setLastupdateStr(lastupdateStr);
    }

    public Leaderboard() {
    }

    @Override
    public int compareTo(@NonNull Leaderboard other) {
        if (this.points.equals(other.points))
            return 0;
        else if (this.points < other.points)
            return 1;
        else
            return -1;
    }

    public void setLastupdateStr(String string) {
        setLastupdate(TimestampConverter.fromTimestamp(string));
    }


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

    public DateTime getLastupdate() {
        return lastupdate;
    }

    public void setLastupdate(DateTime lastupdate) {
        this.lastupdate = lastupdate;
    }

    public boolean isUser() {
        return isUser;
    }

    public void setUser(boolean user) {
        isUser = user;
    }

}
