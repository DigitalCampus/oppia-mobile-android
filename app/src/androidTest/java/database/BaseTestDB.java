package database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.test.mock.MockContext;

import androidx.test.espresso.internal.inject.TargetContext;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.GamificationEvent;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.model.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

/**
 * Extend this class in database-related test classes to facilitate
 * the use of TestDBHelper automating setUp() and tearDown() methods
 *
 * For classes which cannot extend this one, use TestDBHelper directly
 * and call setUp() and tearDown() methods.
 */
@RunWith(AndroidJUnit4.class)
public abstract class BaseTestDB {


    private TestDBHelper testDBHelper;
    private Context context;

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        testDBHelper = new TestDBHelper(context);
        testDBHelper.setUp();
    }

    @After
    public void tearDown() throws Exception {
        testDBHelper.tearDown();
    }

    public DbHelper getDbHelper() {
        return testDBHelper.getDbHelper();
    }

    public TestDataManager getTestDataManager() {
        return testDBHelper.getTestDataManager();
    }

    public Context getContext() {
        return context;
    }

}
