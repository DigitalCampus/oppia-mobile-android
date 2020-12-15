package database;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.test.mock.MockContext;

import androidx.test.espresso.internal.inject.TargetContext;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.oppia.application.App;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.di.AppComponent;
import org.digitalcampus.oppia.di.AppModule;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.GamificationEvent;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.model.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;

import it.cosenonjaviste.daggermock.DaggerMockRule;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

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

    @Mock
    public SharedPreferences prefs;
    @Mock
    SharedPreferences.Editor editor;

    @Rule
    public DaggerMockRule<AppComponent> daggerRule =
            new DaggerMockRule<>(AppComponent.class, new AppModule((App) InstrumentationRegistry.getInstrumentation()
                    .getTargetContext()
                    .getApplicationContext())).set(
                    component -> {
                        App app =
                                (App) InstrumentationRegistry.getInstrumentation()
                                        .getTargetContext()
                                        .getApplicationContext();
                        app.setComponent(component);
                    });

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        testDBHelper = new TestDBHelper(context);
        testDBHelper.setUp();

        initMockEditor();
    }

    @After
    public void tearDown() throws Exception {
        testDBHelper.tearDown();
    }

    private void initMockEditor() {
        when(editor.putString(anyString(), anyString())).thenReturn(editor);
        when(editor.putLong(anyString(), anyLong())).thenReturn(editor);
        when(editor.putBoolean(anyString(), anyBoolean())).thenReturn(editor);
        when(editor.putInt(anyString(), anyInt())).thenReturn(editor);
        when(prefs.edit()).thenReturn(editor);
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
