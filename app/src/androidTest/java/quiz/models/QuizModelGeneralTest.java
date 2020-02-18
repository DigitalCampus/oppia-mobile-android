package quiz.models;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.mobile.quiz.Quiz;
import org.digitalcampus.mobile.quiz.model.questiontypes.MultiChoice;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class QuizModelGeneralTest {

    private static final String DEFAULT_LANG = "en";
    private static final String MULTICHOICE_QUIZ_JSON = "quizzes/multichoice_no_feedback.json";
    private static final String QUIZ_INVALID_JSON = "quizzes/quiz_invalid.json";
    private static final String QUIZ_RANDOM_JSON = "quizzes/quiz_random_selection.json";
    private static final String DESCRIPTION_QUIZ_JSON = "quizzes/quiz_random_selection.json";
    private static final String MULTILANG_QUIZ_TITLE_JSON = "quizzes/multilang_quiz_title.json";
    private Quiz quiz;

    @Before
    public void setup() throws Exception {
        // Setting up before every test
        String quizContent = Utils.FileUtils.getStringFromFile(
                InstrumentationRegistry.getInstrumentation().getContext(), MULTICHOICE_QUIZ_JSON);
        quiz = new Quiz();
        quiz.load(quizContent, DEFAULT_LANG);
    }

    @Test
    public void test_loads()throws Exception {
        quiz = new Quiz();
        String quizContent = Utils.FileUtils.getStringFromFile(
                InstrumentationRegistry.getInstrumentation().getContext(), MULTICHOICE_QUIZ_JSON);
        boolean result = quiz.load(quizContent, DEFAULT_LANG);
        assertTrue(result);
    }

    @Test
    public void test_getId()throws Exception {
        assertEquals(3485, quiz.getID());
    }

    @Test
    public void test_getTitle()throws Exception {
        assertEquals("Multi choice - no feedback", quiz.getTitle(DEFAULT_LANG));
        assertEquals("Multi choice - no feedback", quiz.getTitle("es"));
    }

    @Test
    public void test_mark()throws Exception {
        quiz.mark(DEFAULT_LANG);
        assertEquals((float) 0, quiz.getUserscore());
    }

    // no questions
    @Test
    public void test_noQuestions()throws Exception {
        assertEquals(1, quiz.getTotalNoQuestions());
    }

    // get questions
    @Test
    public void test_getQuestions()throws Exception {
        assertEquals(1, quiz.getQuestions().size());
    }

    // current question type
    @Test
    public void test_getCurrentQuestionType()throws Exception {
        assertTrue(quiz.getCurrentQuestion() instanceof MultiChoice);
    }


    // invalid quiz json
    @Test
    public void test_invalidQuizJson()throws Exception {
        quiz = new Quiz();
        String quizContent = Utils.FileUtils.getStringFromFile(
                InstrumentationRegistry.getInstrumentation().getContext(), QUIZ_INVALID_JSON);
        boolean result = quiz.load(quizContent, DEFAULT_LANG);
        assertFalse(result);
    }

    // pass threshold
    @Test
    public void test_getPassThreshold()throws Exception {
        assertEquals(80, quiz.getPassThreshold());
    }

    // show feedback
    @Test
    public void test_getShowFeedback()throws Exception {
        assertEquals(Quiz.SHOW_FEEDBACK_AT_END, quiz.getShowFeedback());
    }

    // with random select
    @Test
    public void test_randomSelect()throws Exception {
        quiz = new Quiz();
        String quizContent = Utils.FileUtils.getStringFromFile(
                InstrumentationRegistry.getInstrumentation().getContext(), QUIZ_RANDOM_JSON);
        boolean result = quiz.load(quizContent, DEFAULT_LANG);
        assertTrue(result);
        assertEquals((float) 2, quiz.getMaxscore());
    }

    // with description question
    @Test
    public void test_descriptionQuestion()throws Exception {
        quiz = new Quiz();
        String quizContent = Utils.FileUtils.getStringFromFile(
                InstrumentationRegistry.getInstrumentation().getContext(), DESCRIPTION_QUIZ_JSON);
        boolean result = quiz.load(quizContent, DEFAULT_LANG);
        assertTrue(result);
        assertEquals(2, quiz.getTotalNoQuestions());
    }

    // multilang titles
    @Test
    public void test_multilangQuizTitle()throws Exception {
        quiz = new Quiz();
        String quizContent = Utils.FileUtils.getStringFromFile(
                InstrumentationRegistry.getInstrumentation().getContext(), MULTILANG_QUIZ_TITLE_JSON);
        quiz.load(quizContent, DEFAULT_LANG);
        assertEquals("Questions", quiz.getTitle(DEFAULT_LANG));
        assertEquals("Kysymykset", quiz.getTitle("fi"));
        // TODO this next assert should be updated when OPPIA-240 is fixed - it should fall back to the English
        assertEquals("Kysymykset", quiz.getTitle("es"));
    }

    // getResultObject
}
