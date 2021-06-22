package androidTestFiles.database.sampledata;

import android.content.Context;

import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;

import java.util.ArrayList;
import java.util.List;

public class CourseData {

    public final static String TEST_COURSE_1 = "test-course1";

    public final static String TEST_DIGEST_1 = "111111";
    public final static String TEST_DIGEST_2 = "222222";
    public final static String TEST_DIGEST_3 = "333333";
    public final static String TEST_DIGEST_4 = "444444";
    public final static String TEST_DIGEST_5 = "555555";
    public final static String TEST_DIGEST_6 = "666666";

    public static void loadData(Context ctx){

        DbHelper db = DbHelper.getInstance(ctx);

        Course c1 = new Course();
        c1.setShortname(TEST_COURSE_1);
        c1.setDescriptionsFromJSONString("my test course");

        long courseId = db.addOrUpdateCourse(c1);

        Activity a1 = new Activity();
        a1.setActType("page");
        a1.setCourseId(courseId);
        a1.setSectionId(1);
        a1.setActId(1);
        a1.setDigest(TEST_DIGEST_1);
        a1.setTitlesFromJSONString("activity1");

        Activity a2 = new Activity();
        a2.setActType("page");
        a2.setCourseId(courseId);
        a2.setSectionId(1);
        a2.setActId(2);
        a2.setDigest(TEST_DIGEST_2);
        a2.setTitlesFromJSONString("activity2");

        Activity a3 = new Activity();
        a3.setActType("page");
        a3.setCourseId(courseId);
        a3.setSectionId(2);
        a3.setActId(1);
        a3.setDigest(TEST_DIGEST_3);
        a3.setTitlesFromJSONString("activity3");

        Activity a4 = new Activity();
        a4.setActType("quiz");
        a4.setCourseId(courseId);
        a4.setSectionId(2);
        a4.setActId(1);
        a4.setDigest(TEST_DIGEST_4);
        a4.setTitlesFromJSONString("activity4");

        Activity a5 = new Activity();
        a5.setActType("page");
        a5.setCourseId(courseId);
        a5.setSectionId(3);
        a5.setActId(1);
        a5.setDigest(TEST_DIGEST_5);
        a5.setTitlesFromJSONString("activity5");

        Activity a6 = new Activity();
        a6.setActType("quiz");
        a6.setCourseId(courseId);
        a6.setSectionId(3);
        a6.setActId(2);
        a6.setDigest(TEST_DIGEST_6);
        a6.setTitlesFromJSONString("activity6");

        List<Activity> activityList = new ArrayList<Activity>();
        activityList.add(a1);
        activityList.add(a2);
        activityList.add(a3);
        activityList.add(a4);
        activityList.add(a5);
        activityList.add(a6);

        db.insertActivities(activityList);

    }
}
