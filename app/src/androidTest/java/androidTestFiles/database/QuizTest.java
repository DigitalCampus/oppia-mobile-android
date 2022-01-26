package androidTestFiles.database;

import android.Manifest;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.digitalcampus.oppia.model.QuizAttempt;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class QuizTest extends BaseTestDB {
    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    @Test
    public void getUnexportedQuizAttempts() {

        getTestDataManager().addQuizAttempts(1);

        List<QuizAttempt> quizAttempts = getDbHelper().getUnexportedQuizAttempts(1);
        assertEquals(1, quizAttempts.size());

    }

    @Test
    public void getGlobalQuizAttempts() {

        // TODO - add some test data here to really check
        List<QuizAttempt> quizAttempts = getDbHelper().getGlobalQuizAttempts(1, "en");
        assertEquals(0, quizAttempts.size());

    }

    // TODO getQuizAttempt
    // TODO getAllQuizAttemtps
    // TODO getQuizAttempts
    // TODO updateQuizAttempt
    // TODO insertQuizAttempts
    // TODO getUnsentQuizAttempts
    // TODO getUnexportedQuizAttempts
    // TODO markQuizSubmitted
    // TODO deleteQuizAttempts
    // TODO isQuizFirstAttempt
    // TODO isQuizFirstAttemptToday
}
