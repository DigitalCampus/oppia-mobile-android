package database;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.GamificationEvent;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.model.User;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class BaseDBTests {

    private Context context;
    private DbHelper dbHelper;

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        dbHelper = DbHelper.getInMemoryInstance(context);
        dbHelper.getReadableDatabase(); // To force migration if needed
    }

    @After
    public void tearDown() throws Exception {
        dbHelper.resetDatabase();
    }

    public DbHelper getDbHelper() {
        return dbHelper;
    }

    public Context getContext() {
        return context;
    }


    // add users
    public void addUsers(){
        User u = new User();
        u.setUserId(1);
        u.setPassword("password");
        u.setUsername("user1");
        u.setFirstname("User");
        u.setLastname("One");
        dbHelper.addOrUpdateUser(u);

        u = new User();
        u.setUserId(2);
        u.setPassword("password");
        u.setUsername("user2");
        u.setFirstname("User");
        u.setLastname("Two");
        dbHelper.addOrUpdateUser(u);
    }

    // courses
    public void addCourses(){
        Course c = new Course();
        c.setCourseId(100);
        c.setShortname("my-course");
        Lang l = new Lang("en", "course title");
        ArrayList langList = new ArrayList<>();
        langList.add(l);
        c.setTitles(langList);
        dbHelper.addOrUpdateCourse(c);
    }



    // activities/sections

    public void addActivityGamification() {

        List<GamificationEvent> gamificationEvents = new ArrayList<>();
        GamificationEvent ge = new GamificationEvent();
        ge.setEvent("completed");
        ge.setCompleted(true);
        ge.setPoints(10);
        gamificationEvents.add(ge);

        dbHelper.insertActivityGamification(123, gamificationEvents);
    }

    public void addCourseGamification() {

        List<GamificationEvent> gamificationEvents = new ArrayList<>();
        GamificationEvent ge = new GamificationEvent();
        ge.setEvent("downloaded");
        ge.setCompleted(true);
        ge.setPoints(50);
        gamificationEvents.add(ge);

        dbHelper.insertCourseGamification(1234, gamificationEvents);
    }

    // quizzes

    // trackers


    // quiz attempts
}
