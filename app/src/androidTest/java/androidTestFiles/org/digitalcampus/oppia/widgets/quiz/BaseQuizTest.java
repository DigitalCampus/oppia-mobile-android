package androidTestFiles.org.digitalcampus.oppia.widgets.quiz;

import android.content.Context;
import android.os.Bundle;

import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.oppia.activity.CourseActivity;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.model.QuizAttemptRepository;
import org.digitalcampus.oppia.model.QuizStats;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;

import androidTestFiles.TestRules.DaggerInjectMockUITest;
import androidTestFiles.Utils.FileUtils;

import static org.mockito.Matchers.any;

public abstract class BaseQuizTest extends DaggerInjectMockUITest {


    @Mock
    QuizAttemptRepository attemptsRepository;
    private QuizStats stats;

    protected Activity act;
    protected Bundle args;


    @Before
    public void setup() throws Exception {
        // Setting up before every test
        act = new Activity();
        String quizContent = FileUtils.getStringFromFile(
                InstrumentationRegistry.getInstrumentation().getContext(),
                getQuizContentFile());

        ArrayList<Lang> contents = new ArrayList<>();
        contents.add(new Lang("en", quizContent));
        act.setContents(contents);

        args = new Bundle();
        args.putSerializable(Activity.TAG, act);
        args.putSerializable(Course.TAG, new Course(""));
        args.putBoolean(CourseActivity.BASELINE_TAG, false);

        stats = new QuizStats();
        Mockito.doAnswer((Answer<QuizStats>) invocation -> stats).when(attemptsRepository).getQuizAttemptStats(any(Context.class), any());

    }


    protected abstract String getQuizContentFile();
}
