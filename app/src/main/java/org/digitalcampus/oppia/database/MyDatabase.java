package org.digitalcampus.oppia.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import org.digitalcampus.oppia.database.dao.LeaderboardDao;
import org.digitalcampus.oppia.database.dao.UserCustomFieldDao;
import org.digitalcampus.oppia.database.dao.UserPreferenceDao;
import org.digitalcampus.oppia.model.db_model.Leaderboard;
import org.digitalcampus.oppia.model.db_model.UserCustomField;
import org.digitalcampus.oppia.model.db_model.UserPreference;

@Database(version = MyDatabase.DB_VERSION_ROOM, exportSchema = false,
        entities = {
                UserCustomField.class,
                UserPreference.class,
                Leaderboard.class})
public abstract class MyDatabase extends RoomDatabase {

    public static final int DB_VERSION_ROOM = 2;
    public static final String DB_NAME_ROOM = "oppia.db";

    public abstract UserPreferenceDao userPreferenceDao();

    public abstract UserCustomFieldDao userCustomFieldDao();

    public abstract LeaderboardDao leaderboardDao();

    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {

            database.execSQL("ALTER TABLE leaderboard ADD position INTEGER DEFAULT 0;");

        }
    };

    // To include new DB migrations, create the Migration object and include it in this array
    public static final Migration[] MIGRATIONS = new Migration[]{
            MIGRATION_1_2
    };

}
