package database;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.GamificationEvent;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.model.User;
import org.junit.After;
import org.junit.Before;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to manage In Memory database instance
 */
public class TestDBHelper {

    private Context context;
    private DbHelper dbHelper;
    private TestDataManager testDataManager;

    public TestDBHelper(Context context) {
        this.context = context;
    }

    public TestDBHelper setUp() {

        DbHelper.clearInstance();
        dbHelper = DbHelper.getInMemoryInstance(context);
        dbHelper.getReadableDatabase(); // To force migration if needed

        testDataManager = new TestDataManager(dbHelper);

        return this;
    }

    public void tearDown() {

        if (dbHelper == null) {
            throw new IllegalStateException("dbHelper is null. Did you add super.setUp()??");
        }

        dbHelper.resetDatabase();
        DbHelper.clearInstance();
    }


    public DbHelper getDbHelper() {
        return dbHelper;
    }

    public TestDataManager getTestDataManager() {
        return testDataManager;
    }

}
