package androidTestFiles.org.digitalcampus.oppia.activity;

import android.Manifest;
import android.content.Context;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

@RunWith(AndroidJUnit4.class)
public class QuizAttemptActivityTest {
    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    private Context context;

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();

    }


    @Test
    public void testActivityOpenWithBundle() {
        // TODO stub - to complete, this is not working yet, gives a NullPointerException
        /*
        DbHelper db = DbHelper.getInstance(context);
        db.getReadableDatabase(); // To force migration if needed;
        TestData td = new TestData();
        td.load(db);
        QuizAttempt qa = new QuizAttempt();
        qa.setSectionTitle("my section");
        qa.setQuizTitle("my quiz");
        qa.setCourseId(100);
        qa.setUserId(1);

        Intent intent = new Intent();
        intent.putExtra(QuizAttempt.TAG, qa);

        quizAttemptActivityTestRule.launchActivity(intent);
        */

    }
}
