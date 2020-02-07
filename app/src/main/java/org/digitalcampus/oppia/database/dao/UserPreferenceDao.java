package org.digitalcampus.oppia.database.dao;

import androidx.room.Dao;
import androidx.room.Query;

import org.digitalcampus.oppia.model.db_model.UserPreference;

import java.util.List;

@Dao
public interface UserPreferenceDao extends BaseDao<UserPreference> {


    @Query("SELECT * FROM user_preference")
    List<UserPreference> getAll();

    @Query("SELECT * FROM user_preference WHERE username = :username")
    List<UserPreference> getAllForUser(String username);

    @Query("SELECT * FROM user_preference WHERE username = :username AND preference = :preferenceKey")
    UserPreference getUserPreference(String username, String preferenceKey);


}
