package androidTestFiles.features.quiz;

import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static androidTestFiles.utils.UITestActionsUtils.waitForView;
import static androidTestFiles.utils.matchers.EspressoTestsMatchers.noDrawable;
import static androidTestFiles.utils.matchers.EspressoTestsMatchers.withBitmap;
import static androidTestFiles.utils.parent.BaseTest.PATH_COURSES_TESTS;
import static androidTestFiles.utils.parent.BaseTest.PATH_MEDIA_RESOURCES;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.CourseActivity;
import org.digitalcampus.oppia.activity.MainActivity;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.utils.mediaplayer.VideoPlayerActivity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.io.InputStream;

import androidTestFiles.features.courseMedia.CourseMediaBaseTest;
import androidTestFiles.utils.TestUtils;
import androidTestFiles.utils.UITestActionsUtils;
import androidTestFiles.utils.parent.BaseTest;

@RunWith(AndroidJUnit4.class)
public class QuizMediaUiTest extends CourseMediaBaseTest {

    private final String PATH_COURSE_QUIZ_MEDIA = PATH_COURSES_TESTS + "/quiz_media";
    private final String FILENAME_QUIZ_IMAGES_CORRECT = "quiz-images-correct.zip";
    private final String FILENAME_QUIZ_IMAGES_INCORRECT = "quiz-images-incorrect.zip";
    private final String FILENAME_QUIZ_AUDIO = "quiz-audio.zip";
    private final String FILENAME_TEST_AUDIO_CORRECT = "test-audio.mp3";
    private final String FILENAME_TEST_AUDIO_INCORRECT = "test-audio-incorrect.mp3";
    private final String FILENAME_QUIZ_VIDEO = "quiz-video.zip";
    public static final String FILENAME_TEST_VIDEO_CORRECT = BaseTest.MEDIA_FILE_VIDEO_TEST_1;
    public static final String FILENAME_TEST_VIDEO_INCORRECT = "incorrect-" + BaseTest.MEDIA_FILE_VIDEO_TEST_1;

    @Mock
    SharedPreferences prefs;
    @Mock
    SharedPreferences.Editor editor;


    @Before
    public void setUp() throws Exception {
        super.setUp();
        initMockEditor();
        when(prefs.getBoolean(eq(PrefsActivity.PREF_SHOW_GAMIFICATION_EVENTS), anyBoolean())).thenReturn(false);
        when(prefs.getBoolean(eq(PrefsActivity.PREF_START_COURSEINDEX_COLLAPSED), anyBoolean())).thenReturn(false);
    }

    private void initMockEditor() {
        when(prefs.edit()).thenReturn(editor);
        when(editor.putString(anyString(), anyString())).thenReturn(editor);
        when(editor.putLong(anyString(), anyLong())).thenReturn(editor);
        when(editor.putBoolean(anyString(), anyBoolean())).thenReturn(editor);
        when(editor.putInt(anyString(), anyInt())).thenReturn(editor);
    }

    private void navigateToQuiz() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            UITestActionsUtils.clickRecyclerViewPosition(R.id.recycler_courses, 0);
            UITestActionsUtils.clickRecyclerViewPosition(R.id.recycler_course_sections, 2);
            UITestActionsUtils.clickViewWithText(R.string.quiz_attempts_take_quiz);
        }
    }

    @Test
    public void checkQuestionWithImageCorrect() throws Exception {

        installCourse(PATH_COURSE_QUIZ_MEDIA, FILENAME_QUIZ_IMAGES_CORRECT);
        navigateToQuiz();

        InputStream is = InstrumentationRegistry.getInstrumentation().getContext()
                .getResources().getAssets().open(PATH_MEDIA_RESOURCES + "/quiz_image.png");
        Bitmap expectedBitmap = BitmapFactory.decodeStream(is);
        waitForView(withId(R.id.question_image_image))
                .check(matches(withBitmap(expectedBitmap)));
    }


    @Test
    public void checkQuestionWithImageIncorrect() throws Exception {

        installCourse(PATH_COURSE_QUIZ_MEDIA, FILENAME_QUIZ_IMAGES_INCORRECT);
        navigateToQuiz();
        waitForView(withId(R.id.question_image_image))
                .check(matches(noDrawable()));
    }

    @Test
    public void checkQuestionWithAudioCorrect() throws Exception {

        installCourse(PATH_COURSE_QUIZ_MEDIA, FILENAME_QUIZ_AUDIO);
        copyMediaFromAssets(FILENAME_TEST_AUDIO_CORRECT);
        navigateToQuiz();
        waitForView(withId(R.id.question_image_image)).perform(click());
        assertEquals(VideoPlayerActivity.class, TestUtils.getCurrentActivity().getClass());

    }

    @Test
    public void checkQuestionWithAudioIncorrect() throws Exception {

        installCourse(PATH_COURSE_QUIZ_MEDIA, FILENAME_QUIZ_AUDIO);
        copyMediaFromAssets(FILENAME_TEST_AUDIO_INCORRECT);
        navigateToQuiz();
        waitForView(withId(R.id.question_image_image)).perform(click());
        assertEquals(CourseActivity.class, TestUtils.getCurrentActivity().getClass());

    }

    @Test
    public void checkQuestionWithVideoCorrect() throws Exception {

        installCourse(PATH_COURSE_QUIZ_MEDIA, FILENAME_QUIZ_VIDEO);
        copyMediaFromAssets(FILENAME_TEST_VIDEO_CORRECT);
        navigateToQuiz();
        waitForView(withId(R.id.question_image_image)).perform(click());
        assertEquals(VideoPlayerActivity.class, TestUtils.getCurrentActivity().getClass());

    }

    @Test
    public void checkQuestionWithVideoIncorrect() throws Exception {

        installCourse(PATH_COURSE_QUIZ_MEDIA, FILENAME_QUIZ_VIDEO);
        copyMediaFromAssets(FILENAME_TEST_VIDEO_INCORRECT);
        navigateToQuiz();
        waitForView(withId(R.id.question_image_image)).perform(click());
        assertEquals(CourseActivity.class, TestUtils.getCurrentActivity().getClass());

    }
}
