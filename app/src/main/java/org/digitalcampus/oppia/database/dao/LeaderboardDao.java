package org.digitalcampus.oppia.database.dao;

import androidx.room.Dao;
import androidx.room.Query;

import org.digitalcampus.oppia.model.db_model.Leaderboard;

import java.util.List;

@Dao
public interface LeaderboardDao extends BaseDao<Leaderboard> {


    @Query("SELECT * FROM leaderboard")
    List<Leaderboard> getAll();

    @Query("SELECT * FROM leaderboard WHERE username = :username")
    Leaderboard getLeaderboard(String username);

}
