package androidTestFiles.quiz.models;

import android.Manifest;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.mobile.quiz.Quiz;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidTestFiles.Utils.FileUtils;
import androidx.test.rule.GrantPermissionRule;

import static junit.framework.Assert.assertFalse;

@RunWith(AndroidJUnit4.class)
public class ExceptionsTest {
    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    private static final String INVALID_JSON = "quizzes/quiz_invalid.json";
    private static final String DEFAULT_LANG = "en";

    @Test
    public void test_invalidJson() throws Exception {
        String quizInvalidContent = FileUtils.getStringFromFile(
                InstrumentationRegistry.getInstrumentation().getContext(), INVALID_JSON);
        Quiz quizInvalid = new Quiz();
        boolean result = quizInvalid.load(quizInvalidContent, DEFAULT_LANG);
        assertFalse(result);
    }
}
