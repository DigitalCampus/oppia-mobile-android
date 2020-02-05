package org.digitalcampus.oppia.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import org.digitalcampus.oppia.model.UserCustomField;

@Database(entities = {UserCustomField.class}, version = DbHelper.DB_VERSION, exportSchema = false)
public abstract class MyDatabase extends RoomDatabase {

//    public abstract UserPreferenceDao userPreferenceDao();
    public abstract UserCustomFieldDao userCustomFieldDao();


    public static final Migration MIGRATION_34_35 = new Migration(39,40) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {


        }
    };

}
