package androidTestFiles.database.sampledata;

import android.content.Context;

import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.exception.UserNotFoundException;
import org.digitalcampus.oppia.model.TrackerLog;
import org.digitalcampus.oppia.model.User;

import java.util.ArrayList;
import java.util.List;

public class TrackerData {

    public static void loadDataAllActivitiesCourseComplete(Context ctx){

        DbHelper db = DbHelper.getInstance(ctx);

        long courseId = db.getCourseIdByShortname(CourseData.TEST_COURSE_1);
        User user = null;
        try {
            user = db.getUser(UserData.TEST_USER_1);
        } catch (UserNotFoundException e) {
            e.printStackTrace();
        }

        TrackerLog t1 = new TrackerLog();
        t1.setCompleted(true);
        t1.setCourseId(courseId);
        t1.setUserId(user.getUserId());
        t1.setDigest(CourseData.TEST_DIGEST_1);

        TrackerLog t2 = new TrackerLog();
        t2.setCompleted(true);
        t2.setCourseId(courseId);
        t2.setUserId(user.getUserId());
        t2.setDigest(CourseData.TEST_DIGEST_2);

        TrackerLog t3 = new TrackerLog();
        t3.setCompleted(true);
        t3.setCourseId(courseId);
        t3.setUserId(user.getUserId());
        t3.setDigest(CourseData.TEST_DIGEST_3);

        TrackerLog t4 = new TrackerLog();
        t4.setCompleted(true);
        t4.setCourseId(courseId);
        t4.setUserId(user.getUserId());
        t4.setDigest(CourseData.TEST_DIGEST_4);

        TrackerLog t5 = new TrackerLog();
        t5.setCompleted(true);
        t5.setCourseId(courseId);
        t5.setUserId(user.getUserId());
        t5.setDigest(CourseData.TEST_DIGEST_5);

        TrackerLog t6 = new TrackerLog();
        t6.setCompleted(true);
        t6.setCourseId(courseId);
        t6.setUserId(user.getUserId());
        t6.setDigest(CourseData.TEST_DIGEST_6);

        List<TrackerLog> trackerList = new ArrayList<TrackerLog>();
        trackerList.add(t1);
        trackerList.add(t2);
        trackerList.add(t3);
        trackerList.add(t4);
        trackerList.add(t5);
        trackerList.add(t6);

        db.insertTrackers(trackerList);
    }

    public static void loadDataAllActivitiesCourseNotComplete(Context ctx){

        DbHelper db = DbHelper.getInstance(ctx);

        long courseId = db.getCourseIdByShortname(CourseData.TEST_COURSE_1);
        User user = null;
        try {
            user = db.getUser(UserData.TEST_USER_1);
        } catch (UserNotFoundException e) {
            e.printStackTrace();
        }

        TrackerLog t1 = new TrackerLog();
        t1.setCompleted(false);
        t1.setCourseId(courseId);
        t1.setUserId(user.getUserId());
        t1.setDigest(CourseData.TEST_DIGEST_1);

        TrackerLog t2 = new TrackerLog();
        t2.setCompleted(true);
        t2.setCourseId(courseId);
        t2.setUserId(user.getUserId());
        t2.setDigest(CourseData.TEST_DIGEST_2);

        TrackerLog t3 = new TrackerLog();
        t3.setCompleted(true);
        t3.setCourseId(courseId);
        t3.setUserId(user.getUserId());
        t3.setDigest(CourseData.TEST_DIGEST_3);

        TrackerLog t4 = new TrackerLog();
        t4.setCompleted(true);
        t4.setCourseId(courseId);
        t4.setUserId(user.getUserId());
        t4.setDigest(CourseData.TEST_DIGEST_4);

        TrackerLog t5 = new TrackerLog();
        t5.setCompleted(true);
        t5.setCourseId(courseId);
        t5.setUserId(user.getUserId());
        t5.setDigest(CourseData.TEST_DIGEST_5);

        TrackerLog t6 = new TrackerLog();
        t6.setCompleted(true);
        t6.setCourseId(courseId);
        t6.setUserId(user.getUserId());
        t6.setDigest(CourseData.TEST_DIGEST_6);

        List<TrackerLog> trackerList = new ArrayList<TrackerLog>();
        trackerList.add(t1);
        trackerList.add(t2);
        trackerList.add(t3);
        trackerList.add(t4);
        trackerList.add(t5);
        trackerList.add(t6);

        db.insertTrackers(trackerList);
    }

    public static void loadDataAllQuizzesCourseComplete(Context ctx){
        DbHelper db = DbHelper.getInstance(ctx);

        long courseId = db.getCourseIdByShortname(CourseData.TEST_COURSE_1);
        User user = null;
        try {
            user = db.getUser(UserData.TEST_USER_1);
        } catch (UserNotFoundException e) {
            e.printStackTrace();
        }

        TrackerLog t1 = new TrackerLog();
        t1.setCompleted(true);
        t1.setCourseId(courseId);
        t1.setUserId(user.getUserId());
        t1.setDigest(CourseData.TEST_DIGEST_4);

        TrackerLog t2 = new TrackerLog();
        t2.setCompleted(true);
        t2.setCourseId(courseId);
        t2.setUserId(user.getUserId());
        t2.setDigest(CourseData.TEST_DIGEST_6);

        TrackerLog t3 = new TrackerLog();
        t3.setCompleted(false);
        t3.setCourseId(courseId);
        t3.setUserId(user.getUserId());
        t3.setDigest(CourseData.TEST_DIGEST_3);

        List<TrackerLog> trackerList = new ArrayList<TrackerLog>();
        trackerList.add(t1);
        trackerList.add(t2);
        trackerList.add(t3);

        db.insertTrackers(trackerList);

    }

    public static void loadDataAllQuizzesCourseNotComplete(Context ctx){
        DbHelper db = DbHelper.getInstance(ctx);

        long courseId = db.getCourseIdByShortname(CourseData.TEST_COURSE_1);
        User user = null;
        try {
            user = db.getUser(UserData.TEST_USER_1);
        } catch (UserNotFoundException e) {
            e.printStackTrace();
        }

        TrackerLog t1 = new TrackerLog();
        t1.setCompleted(true);
        t1.setCourseId(courseId);
        t1.setUserId(user.getUserId());
        t1.setDigest(CourseData.TEST_DIGEST_4);

        TrackerLog t2 = new TrackerLog();
        t2.setCompleted(false);
        t2.setCourseId(courseId);
        t2.setUserId(user.getUserId());
        t2.setDigest(CourseData.TEST_DIGEST_6);

        TrackerLog t3 = new TrackerLog();
        t3.setCompleted(false);
        t3.setCourseId(courseId);
        t3.setUserId(user.getUserId());
        t3.setDigest(CourseData.TEST_DIGEST_3);

        List<TrackerLog> trackerList = new ArrayList<TrackerLog>();
        trackerList.add(t1);
        trackerList.add(t2);
        trackerList.add(t3);

        db.insertTrackers(trackerList);
    }

    public static void loadDataFinalQuizCourseComplete(Context ctx){
        DbHelper db = DbHelper.getInstance(ctx);

        long courseId = db.getCourseIdByShortname(CourseData.TEST_COURSE_1);
        User user = null;
        try {
            user = db.getUser(UserData.TEST_USER_1);
        } catch (UserNotFoundException e) {
            e.printStackTrace();
        }

        TrackerLog t4 = new TrackerLog();
        t4.setCompleted(false);
        t4.setCourseId(courseId);
        t4.setUserId(user.getUserId());
        t4.setDigest(CourseData.TEST_DIGEST_4);

        TrackerLog t6 = new TrackerLog();
        t6.setCompleted(true);
        t6.setCourseId(courseId);
        t6.setUserId(user.getUserId());
        t6.setDigest(CourseData.TEST_DIGEST_6);

        List<TrackerLog> trackerList = new ArrayList<TrackerLog>();
        trackerList.add(t4);
        trackerList.add(t6);

        db.insertTrackers(trackerList);
    }

    public static void loadDataFinalQuizCourseNotComplete(Context ctx){
        DbHelper db = DbHelper.getInstance(ctx);

        long courseId = db.getCourseIdByShortname(CourseData.TEST_COURSE_1);
        User user = null;
        try {
            user = db.getUser(UserData.TEST_USER_1);
        } catch (UserNotFoundException e) {
            e.printStackTrace();
        }

        TrackerLog t4 = new TrackerLog();
        t4.setCompleted(true);
        t4.setCourseId(courseId);
        t4.setUserId(user.getUserId());
        t4.setDigest(CourseData.TEST_DIGEST_4);

        TrackerLog t6 = new TrackerLog();
        t6.setCompleted(false);
        t6.setCourseId(courseId);
        t6.setUserId(user.getUserId());
        t6.setDigest(CourseData.TEST_DIGEST_6);

        List<TrackerLog> trackerList = new ArrayList<TrackerLog>();
        trackerList.add(t4);
        trackerList.add(t6);

        db.insertTrackers(trackerList);
    }

    public static void loadDataAllQuizzesPlusPercentCourseComplete(Context ctx){
        DbHelper db = DbHelper.getInstance(ctx);

        long courseId = db.getCourseIdByShortname(CourseData.TEST_COURSE_1);
        User user = null;
        try {
            user = db.getUser(UserData.TEST_USER_1);
        } catch (UserNotFoundException e) {
            e.printStackTrace();
        }

        TrackerLog t1 = new TrackerLog();
        t1.setCompleted(true);
        t1.setCourseId(courseId);
        t1.setUserId(user.getUserId());
        t1.setDigest(CourseData.TEST_DIGEST_1);

        TrackerLog t2 = new TrackerLog();
        t2.setCompleted(false);
        t2.setCourseId(courseId);
        t2.setUserId(user.getUserId());
        t2.setDigest(CourseData.TEST_DIGEST_2);

        TrackerLog t3 = new TrackerLog();
        t3.setCompleted(true);
        t3.setCourseId(courseId);
        t3.setUserId(user.getUserId());
        t3.setDigest(CourseData.TEST_DIGEST_3);

        TrackerLog t4 = new TrackerLog();
        t4.setCompleted(true);
        t4.setCourseId(courseId);
        t4.setUserId(user.getUserId());
        t4.setDigest(CourseData.TEST_DIGEST_4);

        TrackerLog t5 = new TrackerLog();
        t5.setCompleted(true);
        t5.setCourseId(courseId);
        t5.setUserId(user.getUserId());
        t5.setDigest(CourseData.TEST_DIGEST_5);

        TrackerLog t6 = new TrackerLog();
        t6.setCompleted(true);
        t6.setCourseId(courseId);
        t6.setUserId(user.getUserId());
        t6.setDigest(CourseData.TEST_DIGEST_6);

        List<TrackerLog> trackerList = new ArrayList<TrackerLog>();
        trackerList.add(t1);
        trackerList.add(t2);
        trackerList.add(t3);
        trackerList.add(t4);
        trackerList.add(t5);
        trackerList.add(t6);

        db.insertTrackers(trackerList);
    }

    public static void loadDataAllQuizzesPlusPercentCourseNotComplete(Context ctx){
        DbHelper db = DbHelper.getInstance(ctx);

        long courseId = db.getCourseIdByShortname(CourseData.TEST_COURSE_1);
        User user = null;
        try {
            user = db.getUser(UserData.TEST_USER_1);
        } catch (UserNotFoundException e) {
            e.printStackTrace();
        }

        TrackerLog t1 = new TrackerLog();
        t1.setCompleted(true);
        t1.setCourseId(courseId);
        t1.setUserId(user.getUserId());
        t1.setDigest(CourseData.TEST_DIGEST_1);

        TrackerLog t2 = new TrackerLog();
        t2.setCompleted(false);
        t2.setCourseId(courseId);
        t2.setUserId(user.getUserId());
        t2.setDigest(CourseData.TEST_DIGEST_2);

        TrackerLog t3 = new TrackerLog();
        t3.setCompleted(true);
        t3.setCourseId(courseId);
        t3.setUserId(user.getUserId());
        t3.setDigest(CourseData.TEST_DIGEST_3);

        TrackerLog t4 = new TrackerLog();
        t4.setCompleted(true);
        t4.setCourseId(courseId);
        t4.setUserId(user.getUserId());
        t4.setDigest(CourseData.TEST_DIGEST_4);

        TrackerLog t5 = new TrackerLog();
        t5.setCompleted(true);
        t5.setCourseId(courseId);
        t5.setUserId(user.getUserId());
        t5.setDigest(CourseData.TEST_DIGEST_5);

        TrackerLog t6 = new TrackerLog();
        t6.setCompleted(false);
        t6.setCourseId(courseId);
        t6.setUserId(user.getUserId());
        t6.setDigest(CourseData.TEST_DIGEST_6);

        List<TrackerLog> trackerList = new ArrayList<TrackerLog>();
        trackerList.add(t1);
        trackerList.add(t2);
        trackerList.add(t3);
        trackerList.add(t4);
        trackerList.add(t5);
        trackerList.add(t6);

        db.insertTrackers(trackerList);
    }
}
