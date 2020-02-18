package quiz.models;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.mobile.quiz.Quiz;
import org.digitalcampus.mobile.quiz.model.QuizQuestion;
import org.digitalcampus.mobile.quiz.model.questiontypes.Numerical;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.DecimalFormat;
import java.util.ArrayList;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertFalse;

@RunWith(AndroidJUnit4.class)
public class NumericQuestionTest {

    private static final String NUMERIC_CLOSE_NOFEEDBACK_JSON = "quizzes/numeric_close_no_feedback.json";
    private static final String NUMERIC_EXACT_NOFEEDBACK_JSON = "quizzes/numeric_exact_no_feedback.json";
    private static final String NUMERIC_ERROR_CHECK_JSON = "quizzes/numeric_error_check.json";
    private static final String NUMERIC_WITH_FEEDBACK_JSON = "quizzes/numeric_with_feedback.json";
    private static final String DEFAULT_LANG = "en";
    private Quiz quizCloseNoFeedback;
    private Quiz quizExactNoFeedback;
    private Quiz quizWithFeedback;
    private static DecimalFormat df = new DecimalFormat("0.00");

    @Before
    public void setup() throws Exception {
        // Setting up before every test
        String quizCloseNoFeedbackContent = Utils.FileUtils.getStringFromFile(
                InstrumentationRegistry.getInstrumentation().getContext(), NUMERIC_CLOSE_NOFEEDBACK_JSON);
        quizCloseNoFeedback = new Quiz();
        quizCloseNoFeedback.load(quizCloseNoFeedbackContent, DEFAULT_LANG);

        String quizExactNoFeedbackContent = Utils.FileUtils.getStringFromFile(
                InstrumentationRegistry.getInstrumentation().getContext(), NUMERIC_EXACT_NOFEEDBACK_JSON);
        quizExactNoFeedback = new Quiz();
        quizExactNoFeedback.load(quizExactNoFeedbackContent, DEFAULT_LANG);

        String quizWithFeedbackContent = Utils.FileUtils.getStringFromFile(
                InstrumentationRegistry.getInstrumentation().getContext(), NUMERIC_WITH_FEEDBACK_JSON);
        quizWithFeedback = new Quiz();
        quizWithFeedback.load(quizWithFeedbackContent, DEFAULT_LANG);
    }

    /*
    No Feedback
     */

    // close - 100% correct
    @Test
    public void test_fullyCorrectNoFeedback()throws Exception {
        QuizQuestion quizQuestion = quizCloseNoFeedback.getCurrentQuestion();
        assertTrue(quizQuestion instanceof Numerical);

        // check before response added
        JSONObject responseJson = quizQuestion.responsesToJSON();

        assertTrue(responseJson.has("question_id"));
        assertTrue(responseJson.has("score"));
        assertFalse(responseJson.has("text"));

        assertEquals(19772, responseJson.get("question_id"));
        assertEquals(0.0, responseJson.get("score"));

        ArrayList<String> userResponses = new ArrayList<>();
        userResponses.add("8848");
        quizQuestion.setUserResponses(userResponses);
        assertEquals("", quizQuestion.getFeedback(DEFAULT_LANG));
        assertEquals((float) 1, quizQuestion.getUserscore());
        assertEquals(100, quizQuestion.getScoreAsPercent());

        // check json response object
        responseJson = quizQuestion.responsesToJSON();
        assertTrue(responseJson.has("question_id"));
        assertTrue(responseJson.has("score"));
        assertTrue(responseJson.has("text"));

        assertEquals(19772, responseJson.get("question_id"));
        assertEquals(1.0, responseJson.get("score"));
        assertEquals("8848", responseJson.get("text"));

        // check for whole quiz
        assertEquals((float) 0, quizCloseNoFeedback.getUserscore());
        quizCloseNoFeedback.mark(DEFAULT_LANG);
        assertEquals((float) 1, quizCloseNoFeedback.getUserscore());
    }

    // close - 60% correct
    @Test
    public void test_partiallyCorrectNoFeedback()throws Exception {
        QuizQuestion quizQuestion = quizCloseNoFeedback.getCurrentQuestion();
        assertTrue(quizQuestion instanceof Numerical);

        ArrayList<String> userResponses = new ArrayList<>();
        userResponses.add("8799");
        quizQuestion.setUserResponses(userResponses);
        assertEquals("", quizQuestion.getFeedback(DEFAULT_LANG));
        assertEquals(df.format(0.6), df.format(quizQuestion.getUserscore()));
        assertEquals(60, quizQuestion.getScoreAsPercent());

        // check json response object
        JSONObject responseJson = quizQuestion.responsesToJSON();
        assertTrue(responseJson.has("question_id"));
        assertTrue(responseJson.has("score"));
        assertTrue(responseJson.has("text"));

        assertEquals(19772, responseJson.get("question_id"));
        assertEquals(df.format(0.6), df.format(responseJson.get("score")));
        assertEquals("8799", responseJson.get("text"));

        // check for whole quiz
        assertEquals((float) 0, quizCloseNoFeedback.getUserscore());
        quizCloseNoFeedback.mark(DEFAULT_LANG);
        assertEquals((float) 0.6, quizCloseNoFeedback.getUserscore());
    }

    // close - null response
    @Test
    public void test_nullResponseNoFeedback()throws Exception {
        QuizQuestion quizQuestion = quizCloseNoFeedback.getCurrentQuestion();
        assertTrue(quizQuestion instanceof Numerical);

        ArrayList<String> userResponses = new ArrayList<>();
        userResponses.add(null);
        quizQuestion.setUserResponses(userResponses);
        assertEquals("", quizQuestion.getFeedback(DEFAULT_LANG));
        assertEquals((float) 0, quizQuestion.getUserscore());
        assertEquals(0, quizQuestion.getScoreAsPercent());

        // check json response object
        JSONObject responseJson = quizQuestion.responsesToJSON();
        assertTrue(responseJson.has("question_id"));
        assertTrue(responseJson.has("score"));
        assertFalse(responseJson.has("text"));

        assertEquals(19772, responseJson.get("question_id"));
        assertEquals(df.format(0), df.format(responseJson.get("score")));

        // check for whole quiz
        assertEquals((float) 0, quizCloseNoFeedback.getUserscore());
        quizCloseNoFeedback.mark(DEFAULT_LANG);
        assertEquals((float) 0, quizCloseNoFeedback.getUserscore());
    }

    // close - incorrect
    @Test
    public void test_incorrectNoFeedback()throws Exception {
        QuizQuestion quizQuestion = quizCloseNoFeedback.getCurrentQuestion();
        assertTrue(quizQuestion instanceof Numerical);

        ArrayList<String> userResponses = new ArrayList<>();
        userResponses.add("8797");
        quizQuestion.setUserResponses(userResponses);
        assertEquals("", quizQuestion.getFeedback(DEFAULT_LANG));
        assertEquals(df.format(0.0), df.format(quizQuestion.getUserscore()));
        assertEquals(0, quizQuestion.getScoreAsPercent());

        // check json response object
        JSONObject responseJson = quizQuestion.responsesToJSON();
        assertTrue(responseJson.has("question_id"));
        assertTrue(responseJson.has("score"));
        assertTrue(responseJson.has("text"));

        assertEquals(19772, responseJson.get("question_id"));
        assertEquals(df.format(0.0), df.format(responseJson.get("score")));
        assertEquals("8797", responseJson.get("text"));

        // check for whole quiz
        assertEquals((float) 0, quizCloseNoFeedback.getUserscore());
        quizCloseNoFeedback.mark(DEFAULT_LANG);
        assertEquals((float) 0, quizCloseNoFeedback.getUserscore());
    }


    // close - non-numeric response
    @Test
    public void test_nonNumericNoFeedback()throws Exception {
        QuizQuestion quizQuestion = quizCloseNoFeedback.getCurrentQuestion();
        assertTrue(quizQuestion instanceof Numerical);

        ArrayList<String> userResponses = new ArrayList<>();
        userResponses.add("eight thousand");
        quizQuestion.setUserResponses(userResponses);
        assertEquals("", quizQuestion.getFeedback(DEFAULT_LANG));
        assertEquals(df.format(0.0), df.format(quizQuestion.getUserscore()));
        assertEquals(0, quizQuestion.getScoreAsPercent());

        // check json response object
        JSONObject responseJson = quizQuestion.responsesToJSON();
        assertTrue(responseJson.has("question_id"));
        assertTrue(responseJson.has("score"));
        assertTrue(responseJson.has("text"));

        assertEquals(19772, responseJson.get("question_id"));
        assertEquals(df.format(0.0), df.format(responseJson.get("score")));
        assertEquals("eight thousand", responseJson.get("text"));

        // check for whole quiz
        assertEquals((float) 0, quizCloseNoFeedback.getUserscore());
        quizCloseNoFeedback.mark(DEFAULT_LANG);
        assertEquals((float) 0, quizCloseNoFeedback.getUserscore());
    }

    // close - float response (not int)
    @Test
    public void test_floatResponseNoFeedback()throws Exception {
        QuizQuestion quizQuestion = quizCloseNoFeedback.getCurrentQuestion();
        assertTrue(quizQuestion instanceof Numerical);

        ArrayList<String> userResponses = new ArrayList<>();
        userResponses.add("8847.12");
        quizQuestion.setUserResponses(userResponses);
        assertEquals("", quizQuestion.getFeedback(DEFAULT_LANG));
        assertEquals(df.format(0.6), df.format(quizQuestion.getUserscore()));
        assertEquals(60, quizQuestion.getScoreAsPercent());

        // check json response object
        JSONObject responseJson = quizQuestion.responsesToJSON();
        assertTrue(responseJson.has("question_id"));
        assertTrue(responseJson.has("score"));
        assertTrue(responseJson.has("text"));

        assertEquals(19772, responseJson.get("question_id"));
        assertEquals(df.format(0.6), df.format(responseJson.get("score")));
        assertEquals("8847.12", responseJson.get("text"));

        // check for whole quiz
        assertEquals((float) 0, quizCloseNoFeedback.getUserscore());
        quizCloseNoFeedback.mark(DEFAULT_LANG);
        assertEquals((float) 0.6, quizCloseNoFeedback.getUserscore());
    }

    // exact - correct
    @Test
    public void test_fullyCorrectExactNoFeedback()throws Exception {
        QuizQuestion quizQuestion = quizExactNoFeedback.getCurrentQuestion();
        assertTrue(quizQuestion instanceof Numerical);

        ArrayList<String> userResponses = new ArrayList<>();
        userResponses.add("8848");
        quizQuestion.setUserResponses(userResponses);
        assertEquals("", quizQuestion.getFeedback(DEFAULT_LANG));
        assertEquals((float) 1, quizQuestion.getUserscore());
        assertEquals(100, quizQuestion.getScoreAsPercent());

        // check json response object
        JSONObject responseJson = quizQuestion.responsesToJSON();
        assertTrue(responseJson.has("question_id"));
        assertTrue(responseJson.has("score"));
        assertTrue(responseJson.has("text"));

        assertEquals(19773, responseJson.get("question_id"));
        assertEquals(1.0, responseJson.get("score"));
        assertEquals("8848", responseJson.get("text"));

        // check for whole quiz
        assertEquals((float) 0, quizExactNoFeedback.getUserscore());
        quizExactNoFeedback.mark(DEFAULT_LANG);
        assertEquals((float) 1, quizExactNoFeedback.getUserscore());
    }

    // exact - incorrect
    @Test
    public void test_incorrectExactNoFeedback()throws Exception {
        QuizQuestion quizQuestion = quizExactNoFeedback.getCurrentQuestion();
        assertTrue(quizQuestion instanceof Numerical);

        ArrayList<String> userResponses = new ArrayList<>();
        userResponses.add("8847");
        quizQuestion.setUserResponses(userResponses);
        assertEquals("", quizQuestion.getFeedback(DEFAULT_LANG));
        assertEquals((float) 0, quizQuestion.getUserscore());
        assertEquals(0, quizQuestion.getScoreAsPercent());

        // check json response object
        JSONObject responseJson = quizQuestion.responsesToJSON();
        assertTrue(responseJson.has("question_id"));
        assertTrue(responseJson.has("score"));
        assertTrue(responseJson.has("text"));

        assertEquals(19773, responseJson.get("question_id"));
        assertEquals(0.0, responseJson.get("score"));
        assertEquals("8847", responseJson.get("text"));

        // check for whole quiz
        assertEquals((float) 0, quizExactNoFeedback.getUserscore());
        quizExactNoFeedback.mark(DEFAULT_LANG);
        assertEquals((float) 0, quizExactNoFeedback.getUserscore());
    }

    /*
    Error/Exception check
     */
    @Test
    public void test_errorCheck()throws Exception {

        String quizErrorCheckContent = Utils.FileUtils.getStringFromFile(
                InstrumentationRegistry.getInstrumentation().getContext(), NUMERIC_ERROR_CHECK_JSON);
        Quiz quizErrorCheck = new Quiz();
        quizErrorCheck.load(quizErrorCheckContent, DEFAULT_LANG);

        QuizQuestion quizQuestion = quizExactNoFeedback.getCurrentQuestion();
        assertTrue(quizQuestion instanceof Numerical);

        ArrayList<String> userResponses = new ArrayList<>();
        userResponses.add("8847");
        quizQuestion.setUserResponses(userResponses);
        assertEquals("", quizQuestion.getFeedback(DEFAULT_LANG));
        assertEquals((float) 0, quizQuestion.getUserscore());
        assertEquals(0, quizQuestion.getScoreAsPercent());

        // check json response object
        JSONObject responseJson = quizQuestion.responsesToJSON();
        assertTrue(responseJson.has("question_id"));
        assertTrue(responseJson.has("score"));
        assertTrue(responseJson.has("text"));

        assertEquals(19773, responseJson.get("question_id"));
        assertEquals(0.0, responseJson.get("score"));
        assertEquals("8847", responseJson.get("text"));

        // check for whole quiz
        assertEquals((float) 0, quizErrorCheck.getUserscore());
        quizErrorCheck.mark(DEFAULT_LANG);
        assertEquals((float) 0, quizErrorCheck.getUserscore());
    }
    /*
    with feedback
     */
    // correct
    @Test
    public void test_correctWithFeedback()throws Exception {
        QuizQuestion quizQuestion = quizWithFeedback.getCurrentQuestion();
        assertTrue(quizQuestion instanceof Numerical);

        ArrayList<String> userResponses = new ArrayList<>();
        userResponses.add("6");
        quizQuestion.setUserResponses(userResponses);
        assertEquals("correct", quizQuestion.getFeedback(DEFAULT_LANG));
        assertEquals((float) 1, quizQuestion.getUserscore());
        assertEquals(100, quizQuestion.getScoreAsPercent());

        // check json response object
        JSONObject responseJson = quizQuestion.responsesToJSON();
        assertTrue(responseJson.has("question_id"));
        assertTrue(responseJson.has("score"));
        assertTrue(responseJson.has("text"));

        assertEquals(19774, responseJson.get("question_id"));
        assertEquals(1.0, responseJson.get("score"));
        assertEquals("6", responseJson.get("text"));

        // check for whole quiz
        assertEquals((float) 0, quizWithFeedback.getUserscore());
        quizWithFeedback.mark(DEFAULT_LANG);
        assertEquals((float) 1, quizWithFeedback.getUserscore());
    }

    // wrong
    @Test
    public void test_incorrectWithFeedback()throws Exception {
        QuizQuestion quizQuestion = quizWithFeedback.getCurrentQuestion();
        assertTrue(quizQuestion instanceof Numerical);

        ArrayList<String> userResponses = new ArrayList<>();
        userResponses.add("7");
        quizQuestion.setUserResponses(userResponses);
        assertEquals("wrong", quizQuestion.getFeedback(DEFAULT_LANG));
        assertEquals((float) 0, quizQuestion.getUserscore());
        assertEquals(0, quizQuestion.getScoreAsPercent());

        // check json response object
        JSONObject responseJson = quizQuestion.responsesToJSON();
        assertTrue(responseJson.has("question_id"));
        assertTrue(responseJson.has("score"));
        assertTrue(responseJson.has("text"));

        assertEquals(19774, responseJson.get("question_id"));
        assertEquals(0.0, responseJson.get("score"));
        assertEquals("7", responseJson.get("text"));

        // check for whole quiz
        assertEquals((float) 0, quizWithFeedback.getUserscore());
        quizWithFeedback.mark(DEFAULT_LANG);
        assertEquals((float) 0, quizWithFeedback.getUserscore());
    }

    /*
    Multilang - no feedback
     */
    // TODO

    /*
    Multilang with feedback
     */
    // TODO
}
