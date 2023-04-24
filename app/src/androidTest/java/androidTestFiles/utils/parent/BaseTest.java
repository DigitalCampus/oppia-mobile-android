package androidTestFiles.utils.parent;

import java.io.File;

public class BaseTest {

    public static final String PATH_TESTS = "tests";

    public static final String PATH_COMMON_TESTS = PATH_TESTS + "/common";

    public static final String PATH_COURSES_TESTS = PATH_TESTS + "/courses";
    public static final String PATH_COURSES_XML_TESTS = PATH_TESTS + "/courses/xml";
    public static final String PATH_COURSES_MEDIA_TESTS = PATH_TESTS + "/courses/media";
    public static final String PATH_COURSES_QUIZ_RESULTS_TESTS = PATH_TESTS + "/courses/quiz_results";
    public static final String PATH_COURSES_SKIP_LOGIC_TESTS = PATH_TESTS + "/courses/skip_logic";
    public static final String PATH_COURSES_TOPICS_PASSWORD_PROTECT_TESTS = PATH_TESTS + "/courses/topics_password_protect";

    public static final String PATH_CUSTOM_FIELDS_TESTS = PATH_TESTS + "/customFields";
    public static final String PATH_TAGS_TESTS = PATH_TESTS + "/tags";
    public static final String PATH_QUIZZES = PATH_TESTS + "/quizzes";
    public static final String PATH_RESPONSES = PATH_TESTS + "/responses";



    public static final String COURSE_TEST = "test_course.zip";
    public static final String COURSE_TEST_2 = "test_course_2.zip";

    public static final String COURSE_FEEDBACK = "course-with-feedback.zip";
    public static final String COURSE_QUIZ = "course-with-quiz.zip";
    public static final String COURSE_QUIZ_SHORTNAME = "course-with-quiz";
    public static final String COURSE_SINGLE_PAGE = "single-page-course.zip";

    public static final String CORRECT_COURSE = "Correct_Course.zip";
    public static final String EXISTING_COURSE = "Existing_Course.zip";
    public static final String UPDATED_COURSE = "Updated_Course.zip";
    public static final String INCORRECT_COURSE = "Incorrect_Course.zip";
    public static final String NOXML_COURSE = "NoXML_Course.zip";
    public static final String MALFORMEDXML_COURSE = "MalformedXML_Course.zip";
    public static final String INSECURE_COURSE = "Insecure_Course.zip";
    
    public static final String CORRECT_XML = "correct_course.xml";
    public static final String INCORRECT_XML = "incorrect_xml.xml";

    public static final String COURSE_WITH_NO_MEDIA = "Course_with_no_media.zip";
    public static final String COURSE_WITH_MEDIA_1 = "Course_with_media_1.zip";
    public static final String COURSE_WITH_MEDIA_2 = "Course_with_media_2.zip";
    public static final String MEDIA_FILE_VIDEO_TEST_1 = "video-test-1.mp4";
    public static final String MEDIA_FILE_VIDEO_TEST_2 = "video-test-2.mp4";

    public static final String COURSE_QUIZ_SHOW_ALL = "show_all.zip";
    public static final String COURSE_QUIZ_HIDE_LATER = "hide_later.zip";
    public static final String COURSE_QUIZ_HIDE_AT_END = "hide_at_end.zip";
    public static final String COURSE_QUIZ_HIDE_AT_END_AND_LATER = "hide_at_end_and_later.zip";

    public static final String COURSE_SKIP_LOGIC = "course-skip-logic.zip";

    public static final String COURSE_PASSWORD_PROTECT = "password-protect-initial.zip";
    public static final String COURSE_PASSWORD_PROTECT_UPDATE = "password-protect-update.zip";

    public static final String TAGS_LIVE_DRAFT_RESPONSE = PATH_TAGS_TESTS + File.separator + "all_statuses_types.json";
    public static final String TAGS_NO_COURSE_STATUSES_FIELD_RESPONSE = PATH_TAGS_TESTS + File.separator + "no_course_statuses_field.json";
}
