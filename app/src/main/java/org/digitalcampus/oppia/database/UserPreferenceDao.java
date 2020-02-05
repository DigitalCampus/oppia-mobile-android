package org.digitalcampus.oppia.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import org.digitalcampus.oppia.model.db_model.UserPreference;

import java.util.List;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface UserPreferenceDao {


    @Query("SELECT * FROM user_preference")
    List<UserPreference> getAll();

    @Query("SELECT * FROM user_preference WHERE username = :username")
    List<UserPreference> getAllForUser(String username);

    @Query("SELECT * FROM user_preference WHERE username = :username AND preference = :preferenceKey")
    UserPreference getPref(String username, String preferenceKey);

    @Insert(onConflict = REPLACE)
    void insertAll(List<UserPreference> userPrefs);

    @Insert(onConflict = REPLACE)
    void insert(UserPreference userPref);

    @Update
    void update(UserPreference userPref);

    @Update
    void updateAll(List<UserPreference> userPrefs);

    @Delete
    void delete(UserPreference userPref);

    @Delete
    void deleteAll(List<UserPreference> userPrefs);

}
