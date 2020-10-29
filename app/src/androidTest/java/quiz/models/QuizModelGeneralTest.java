package quiz.models;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.mobile.quiz.InvalidQuizException;
import org.digitalcampus.mobile.quiz.Quiz;
import org.digitalcampus.mobile.quiz.model.QuizQuestion;
import org.digitalcampus.mobile.quiz.model.questiontypes.MultiChoice;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private static final String DESCRIPTION_JSON = "quizzes/description_question.json";
    private static final String WITH_MAX_ATTEMPTS_JSON = "quizzes/with_max_attempts_quiz.json";
    private static final String NO_MULTILANG_OBJECTS = "quizzes/no_multilang_objects.json";
    private static final String INTEGER_TITLES = "quizzes/integer_titles.json";
    private static final String UNSUPPORTED_QUESTION_TYPE = "quizzes/unsupported_question_type.json";
    private static final String ESSAY_QUESTION_TYPE = "quizzes/essay_question_type.json";

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
    public void generalTest() throws Exception{
        quiz = new Quiz();
        quiz.setMaxscore(10);
        assertEquals(10, quiz.getMaxscore(), 0);
        assertEquals(0, quiz.getMaxAttempts());
        String quizContent = Utils.FileUtils.getStringFromFile(
                InstrumentationRegistry.getInstrumentation().getContext(), DESCRIPTION_JSON);
        quiz = new Quiz();
        quiz.load(quizContent, DEFAULT_LANG);
        assertEquals(0, quiz.getCurrentQuestionNo());
        assertEquals(2, quiz.getTotalNoQuestions());
        assertEquals(false, quiz.getCurrentQuestion().responseExpected());
        quiz.setUrl("https://myurl.com");
        assertEquals("https://myurl.com", quiz.getUrl());
        JSONObject response = quiz.getCurrentQuestion().responsesToJSON();
        assertEquals(19765, response.get(Quiz.JSON_PROPERTY_QUESTION_ID));
        assertEquals(0, response.get(Quiz.JSON_PROPERTY_SCORE));
        quiz.moveNext();
        assertEquals(1, quiz.getCurrentQuestionNo());
        assertEquals(true, quiz.getCurrentQuestion().responseExpected());

        // moving back and forth
        quiz.moveNext();
        assertEquals(2, quiz.getCurrentQuestionNo());
        quiz.moveNext();
        assertEquals(2, quiz.getCurrentQuestionNo());
        quiz.moveNext();
        assertEquals(2, quiz.getCurrentQuestionNo());
        quiz.movePrevious();
        assertEquals(1, quiz.getCurrentQuestionNo());
        quiz.movePrevious();
        assertEquals(0, quiz.getCurrentQuestionNo());
        quiz.movePrevious();
        assertEquals(0, quiz.getCurrentQuestionNo());
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
    public void test_getCurrentQuestionType() throws Exception {
        assertTrue(quiz.getCurrentQuestion() instanceof MultiChoice);
    }


    // invalid quiz json
    @Test
    public void test_invalidQuizJson() throws Exception {
        quiz = new Quiz();
        String quizContent = Utils.FileUtils.getStringFromFile(
                InstrumentationRegistry.getInstrumentation().getContext(), QUIZ_INVALID_JSON);
        boolean result = quiz.load(quizContent, DEFAULT_LANG);
        assertFalse(result);
    }

    // pass threshold
    @Test
    public void test_getPassThreshold() {
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

    // max attempts
    @Test
    public void maxAttemptsTest() throws Exception {
        quiz = new Quiz();
        String quizContent = Utils.FileUtils.getStringFromFile(
                InstrumentationRegistry.getInstrumentation().getContext(), WITH_MAX_ATTEMPTS_JSON);
        quiz.load(quizContent, DEFAULT_LANG);
        assertEquals(true, quiz.limitAttempts());
    }

    // title etc not in json format
    @Test
    public void nonMultiLangTest() throws Exception {
        quiz = new Quiz();
        String quizContent = Utils.FileUtils.getStringFromFile(
                InstrumentationRegistry.getInstrumentation().getContext(), NO_MULTILANG_OBJECTS);
        quiz.load(quizContent, DEFAULT_LANG);
        assertEquals("Questions", quiz.getTitle(DEFAULT_LANG));
    }

    // title etc in integer format
    @Test
    public void integerFormatTitlesTest() throws Exception {
        quiz = new Quiz();
        String quizContent = Utils.FileUtils.getStringFromFile(
                InstrumentationRegistry.getInstrumentation().getContext(), INTEGER_TITLES);
        quiz.load(quizContent, DEFAULT_LANG);
        assertEquals("unknown", quiz.getTitle(DEFAULT_LANG));
    }

    // title etc in integer format
    @Test
    public void unsupportedQuestionTypeTest() throws Exception {
        quiz = new Quiz();
        String quizContent = Utils.FileUtils.getStringFromFile(
                InstrumentationRegistry.getInstrumentation().getContext(), UNSUPPORTED_QUESTION_TYPE);
        quiz.load(quizContent, DEFAULT_LANG);
        assertEquals("Questions", quiz.getTitle(DEFAULT_LANG));
        assertEquals(1, quiz.getTotalNoQuestions());
    }

    // essay question type
    @Test
    public void essayQuestionTypeTest() throws Exception {
        quiz = new Quiz();
        String quizContent = Utils.FileUtils.getStringFromFile(
                InstrumentationRegistry.getInstrumentation().getContext(), ESSAY_QUESTION_TYPE);
        quiz.load(quizContent, DEFAULT_LANG);
        assertEquals("Questions", quiz.getTitle(DEFAULT_LANG));
        assertEquals(1, quiz.getTotalNoQuestions());
        List<String> userResponses = new ArrayList<String>();
        userResponses.add("alex");
        quiz.getCurrentQuestion().setUserResponses(userResponses);
        assertEquals(0, quiz.getUserscore(),0);
        quiz.getCurrentQuestion().mark(DEFAULT_LANG);
        assertEquals(0, quiz.getUserscore(),0);
    }

    // quizProps test
    @Test
    public void questionPropsTest(){
        try {
            QuizQuestion q = quiz.getCurrentQuestion();
            Map<String, String> myMap = q.getProps();
            assertEquals("1", myMap.get("maxscore"));
            assertEquals("1", myMap.get("shuffleanswers"));
        } catch (InvalidQuizException iqe){
            Assert.fail("questionPropsTest failed");
        }
    }

}
