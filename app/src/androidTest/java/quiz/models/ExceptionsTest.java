package quiz.models;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.mobile.quiz.Quiz;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertFalse;

@RunWith(AndroidJUnit4.class)
public class ExceptionsTest {

    private static final String INVALID_JSON = "quizzes/quiz_invalid.json";
    private static final String DEFAULT_LANG = "en";

    @Test
    public void test_invalidJson() throws Exception {
        String quizInvalidContent = Utils.FileUtils.getStringFromFile(
                InstrumentationRegistry.getInstrumentation().getContext(), INVALID_JSON);
        Quiz quizInvalid = new Quiz();
        boolean result = quizInvalid.load(quizInvalidContent, DEFAULT_LANG);
        assertFalse(result);
    }
}
