package testFiles.model.quiz;

import static junit.framework.Assert.assertFalse;

import org.digitalcampus.mobile.quiz.Quiz;
import org.junit.Test;

import testFiles.utils.UnitTestsFileUtils;

public class ExceptionsTest {

    private static final String INVALID_JSON = "quizzes/quiz_invalid.json";
    private static final String DEFAULT_LANG = "en";

    @Test
    public void test_invalidJson() throws Exception {
        String quizInvalidContent = UnitTestsFileUtils.readFileFromTestResources(INVALID_JSON);
        Quiz quizInvalid = new Quiz();
        boolean result = quizInvalid.load(quizInvalidContent, DEFAULT_LANG);
        assertFalse(result);
    }
}
