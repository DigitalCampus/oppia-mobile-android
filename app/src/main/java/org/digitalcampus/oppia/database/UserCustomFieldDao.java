package org.digitalcampus.oppia.database;

import androidx.room.Dao;
import androidx.room.Query;

import org.digitalcampus.oppia.model.db_model.UserCustomField;

import java.util.List;

@Dao
public interface UserCustomFieldDao {


    @Query("SELECT * FROM user_custom_field")
    List<UserCustomField> getAll();

}
