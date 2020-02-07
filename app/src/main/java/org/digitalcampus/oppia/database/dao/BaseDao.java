package org.digitalcampus.oppia.database.dao;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Update;

import java.util.List;

import static androidx.room.OnConflictStrategy.REPLACE;

public interface BaseDao<T> {

    @Insert(onConflict = REPLACE)
    void insertAll(List<T> list);

    /**
     * If the item is replaced, a new _id will be assigned automatically. It is internally a delete/insert operation
     * @return the row id assigned
     */
    @Insert(onConflict = REPLACE)
    long insert(T item);

    /**
     * @return number of rows updated
     */
    @Update
    int update(T... item);

    /**
     * @return number of rows updated
     */
    @Update
    int updateAll(List<T> list);

    @Delete
    int delete(T... item);

    @Delete
    int deleteAll(List<T> list);
}
