package androidTestFiles.database;

import android.content.Context;

import org.digitalcampus.oppia.database.DbHelper;

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
