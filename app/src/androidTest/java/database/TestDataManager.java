package database;

import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.GamificationEvent;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Use this class to insert test data.
 */
public class TestDataManager {


    private final DbHelper dbHelper;

    public TestDataManager(DbHelper dbHelper) {
        this.dbHelper = dbHelper;
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
