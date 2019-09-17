package org.digitalcampus.oppia.model;

import androidx.annotation.NonNull;

public class LeaderboardPosition implements Comparable<LeaderboardPosition>{
    private String username;
    private String fullname;
    private int points;
    private boolean isUser;


    public LeaderboardPosition(){

    }

    public LeaderboardPosition(String username, String fullname, int points){
        this.username = username;
        this.fullname = fullname;
        this.points = points;
    }

    @Override
    public int compareTo(@NonNull LeaderboardPosition other) {
        if (this.points == other.points)
            return 0;
        else if (this.points < other.points)
            return 1;
        else
            return -1;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public boolean isUser() {
        return isUser;
    }

    public void setUser(boolean user) {
        isUser = user;
    }


}
