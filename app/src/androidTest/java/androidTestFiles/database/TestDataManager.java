package androidTestFiles.database;

import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.GamificationEvent;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.model.QuizAttempt;
import org.digitalcampus.oppia.model.TrackerLog;
import org.digitalcampus.oppia.model.User;

import java.util.ArrayList;
import java.util.Arrays;
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
    public void addCourse() {
        addCourse(1, "my-course");
    }

    public void addCourse(int courseId, String shortname){
        Course c = new Course();
        c.setCourseId(courseId);
        c.setShortname(shortname);
        Lang l = new Lang("en", "course title");
        ArrayList langList = new ArrayList<>();
        langList.add(l);
        c.setTitles(langList);
        dbHelper.addOrUpdateCourse(c);
    }



    // activities/sections

    public void addActivity(String digest, int courseId) {
        Activity activity = new Activity();
        activity.setDigest(digest);
        activity.setCourseId(courseId);
        dbHelper.insertActivities(Arrays.asList(activity));
    }

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

    public void addQuizAttempts(int num) {

        addUsers();
        addCourse();

        for (int i = 0; i < num; i++) {
            QuizAttempt qa = new QuizAttempt();
            qa.setCourseId(1);
            qa.setUserId(1);
            qa.setData("[{\"some\": \"data\"}]");
            dbHelper.insertQuizAttempt(qa);
        }

    }

    public void addQuizAttemptsWithNullData(int num) {

        addUsers();
        addCourse();

        for (int i = 0; i < num; i++) {
            QuizAttempt qa = new QuizAttempt();
            qa.setCourseId(1);
            qa.setUserId(1);
            dbHelper.insertQuizAttempt(qa);
        }

    }

    public void addTrackers(int num) {

        addUsers();
        addCourse();

        List<TrackerLog> trackerList = new ArrayList<>();

        for (int i = 0; i < num; i++) {
            TrackerLog trackerLog = new TrackerLog();
            trackerLog.setCompleted(true);
            trackerLog.setCourseId(1);
            trackerLog.setUserId(1);
            trackerLog.setDigest("test_digest");

            trackerList.add(trackerLog);
        }

        dbHelper.insertTrackers(trackerList);
    }

    // quizzes



}
