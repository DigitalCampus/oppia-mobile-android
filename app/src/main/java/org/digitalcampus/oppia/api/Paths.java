package org.digitalcampus.oppia.api;

public class Paths {

    // server path vars - new version
    public static final String OPPIAMOBILE_API = "api/v2/";
    public static final String LEADERBOARD_PATH = OPPIAMOBILE_API + "leaderboard/";
    public static final String SERVER_AWARDS_PATH = OPPIAMOBILE_API + "awards/";
    public static final String TRACKER_PATH = OPPIAMOBILE_API + "tracker/";
    public static final String SERVER_TAG_PATH = OPPIAMOBILE_API + "tag/";
    public static final String SERVER_COURSES_PATH = OPPIAMOBILE_API + "course/";
    public static final String COURSE_ACTIVITY_PATH = SERVER_COURSES_PATH + "%s/activity/";
    public static final String COURSE_INFO_PATH = SERVER_COURSES_PATH + "%s";
    public static final String QUIZ_SUBMIT_PATH = OPPIAMOBILE_API + "quizattempt/";
    public static final String RESET_PATH = OPPIAMOBILE_API + "reset/";
    public static final String REGISTER_PATH = OPPIAMOBILE_API + "register/";
    public static final String LOGIN_PATH = OPPIAMOBILE_API + "user/";
    public static final String ACTIVITYLOG_PATH = "api/activitylog/";
    public static final String SERVER_INFO_PATH = "server/";
    public static final String UPDATE_PROFILE_PATH = OPPIAMOBILE_API + "profileupdate/";

    private Paths() {
        throw new IllegalStateException("Utility class");
    }

}
