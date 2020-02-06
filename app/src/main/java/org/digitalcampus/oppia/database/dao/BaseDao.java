package org.digitalcampus.oppia.database.dao;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Update;

import java.util.List;

import static androidx.room.OnConflictStrategy.REPLACE;

public interface BaseDao<T> {

    @Insert(onConflict = REPLACE)
    void insertAll(List<T> list);

    @Insert(onConflict = REPLACE)
    void insert(T item);

    @Update
    void update(T item);

    @Update
    void updateAll(List<T> list);

    @Delete
    void delete(T item);

    @Delete
    void deleteAll(List<T> list);
}
