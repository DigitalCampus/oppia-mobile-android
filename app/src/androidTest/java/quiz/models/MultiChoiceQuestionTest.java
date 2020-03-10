package quiz.models;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.mobile.quiz.Quiz;
import org.digitalcampus.mobile.quiz.model.QuizQuestion;
import org.digitalcampus.mobile.quiz.model.questiontypes.MultiChoice;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class MultiChoiceQuestionTest {

    private static final String MULTICHOICE_NOFEEDBACK_JSON = "quizzes/multichoice_no_feedback.json";
    private static final String MULTICHOICE_WITHFEEDBACK_JSON = "quizzes/multichoice_with_feedback.json";
    private static final String DEFAULT_LANG = "en";
    private Quiz quizNoFeedback;
    private Quiz quizWithFeedback;

    @Before
    public void setup() throws Exception {
        // Setting up before every test
        String quizNoFeedbackContent = Utils.FileUtils.getStringFromFile(
                InstrumentationRegistry.getInstrumentation().getContext(), MULTICHOICE_NOFEEDBACK_JSON);
        quizNoFeedback = new Quiz();
        quizNoFeedback.load(quizNoFeedbackContent, DEFAULT_LANG);

        String quizWithFeedbackContent = Utils.FileUtils.getStringFromFile(
                InstrumentationRegistry.getInstrumentation().getContext(), MULTICHOICE_WITHFEEDBACK_JSON);
        quizWithFeedback = new Quiz();
        quizWithFeedback.load(quizWithFeedbackContent, DEFAULT_LANG);
    }

    /*
    No Feedback
     */
    // correct answer
    @Test
    public void test_correctNoFeedback() throws Exception {
        QuizQuestion quizQuestion = quizNoFeedback.getCurrentQuestion();
        assertTrue(quizQuestion instanceof MultiChoice);

        // check before response added
        JSONObject responseJson = quizQuestion.responsesToJSON();
        assertTrue(responseJson.has("question_id"));
        assertTrue(responseJson.has("score"));
        assertTrue(responseJson.has("text"));

        assertEquals(19761, responseJson.get("question_id"));
        assertEquals(0.0, responseJson.get("score"));
        assertEquals("", responseJson.get("text"));


        ArrayList<String> userResponses = new ArrayList<>();
        userResponses.add("Berlin");
        quizQuestion.setUserResponses(userResponses);
        assertEquals("", quizQuestion.getFeedback(DEFAULT_LANG));
        assertEquals((float) 1, quizQuestion.getUserscore());
        assertEquals(100, quizQuestion.getScoreAsPercent());

        // check json response object
        responseJson = quizQuestion.responsesToJSON();
        assertTrue(responseJson.has("question_id"));
        assertTrue(responseJson.has("score"));
        assertTrue(responseJson.has("text"));

        assertEquals(19761, responseJson.get("question_id"));
        assertEquals(1.0, responseJson.get("score"));
        assertEquals("Berlin", responseJson.get("text"));

        // check for whole quiz
        assertEquals((float) 0, quizNoFeedback.getUserscore());
        quizNoFeedback.mark(DEFAULT_LANG);
        assertEquals((float) 1, quizNoFeedback.getUserscore());
    }

    // incorrect answer (valid selection)
    @Test
    public void test_incorrectNoFeedback()throws Exception {
        QuizQuestion quizQuestion = quizNoFeedback.getCurrentQuestion();
        assertTrue(quizQuestion instanceof MultiChoice);

        ArrayList<String> userResponses = new ArrayList<>();
        userResponses.add("Bonn");
        quizQuestion.setUserResponses(userResponses);
        assertEquals("", quizQuestion.getFeedback(DEFAULT_LANG));
        assertEquals((float) 0, quizQuestion.getUserscore());
        assertEquals(0, quizQuestion.getScoreAsPercent());

        // check json response object
        JSONObject responseJson = quizQuestion.responsesToJSON();
        assertTrue(responseJson.has("question_id"));
        assertTrue(responseJson.has("score"));
        assertTrue(responseJson.has("text"));

        assertEquals(19761, responseJson.get("question_id"));
        assertEquals(0.0, responseJson.get("score"));
        assertEquals("Bonn", responseJson.get("text"));

        // check for whole quiz
        assertEquals((float) 0, quizNoFeedback.getUserscore());
        quizNoFeedback.mark(DEFAULT_LANG);
        assertEquals((float) 0, quizNoFeedback.getUserscore());
    }

    // incorrect answer (invalid selection)
    @Test
    public void test_incorrectNoFeedbackInvalidOption()throws Exception {
        QuizQuestion quizQuestion = quizNoFeedback.getCurrentQuestion();
        assertTrue(quizQuestion instanceof MultiChoice);

        ArrayList<String> userResponses = new ArrayList<>();
        userResponses.add("somewhere");
        quizQuestion.setUserResponses(userResponses);
        assertEquals("", quizQuestion.getFeedback(DEFAULT_LANG));
        assertEquals((float) 0, quizQuestion.getUserscore());
        assertEquals(0, quizQuestion.getScoreAsPercent());

        // check json response object
        JSONObject responseJson = quizQuestion.responsesToJSON();
        assertTrue(responseJson.has("question_id"));
        assertTrue(responseJson.has("score"));
        assertTrue(responseJson.has("text"));

        assertEquals(19761, responseJson.get("question_id"));
        assertEquals(0.0, responseJson.get("score"));
        assertEquals("somewhere", responseJson.get("text"));

        // check for whole quiz
        assertEquals((float) 0, quizNoFeedback.getUserscore());
        quizNoFeedback.mark(DEFAULT_LANG);
        assertEquals((float) 0, quizNoFeedback.getUserscore());
    }

    /*
    With feedback
     */

    // correct answer

    @Test
    public void test_correctWithFeedback()throws Exception {
        QuizQuestion quizQuestion = quizWithFeedback.getCurrentQuestion();
        assertTrue(quizQuestion instanceof MultiChoice);

        ArrayList<String> userResponses = new ArrayList<>();
        userResponses.add("Red");
        quizQuestion.setUserResponses(userResponses);
        assertEquals("correct", quizQuestion.getFeedback(DEFAULT_LANG));
        assertEquals((float) 1, quizQuestion.getUserscore());
        assertEquals(100, quizQuestion.getScoreAsPercent());

        // check json response object
        JSONObject responseJson = quizQuestion.responsesToJSON();
        assertTrue(responseJson.has("question_id"));
        assertTrue(responseJson.has("score"));
        assertTrue(responseJson.has("text"));

        assertEquals(19769, responseJson.get("question_id"));
        assertEquals(1.0, responseJson.get("score"));
        assertEquals("Red", responseJson.get("text"));

        // check for whole quiz
        assertEquals((float) 0, quizWithFeedback.getUserscore());
        quizWithFeedback.mark(DEFAULT_LANG);
        assertEquals((float) 1, quizWithFeedback.getUserscore());
    }
    // incorrect answer (valid selection)
    @Test
    public void test_incorrectWithFeedback()throws Exception {
        QuizQuestion quizQuestion = quizWithFeedback.getCurrentQuestion();
        assertTrue(quizQuestion instanceof MultiChoice);

        ArrayList<String> userResponses = new ArrayList<>();
        userResponses.add("Black");
        quizQuestion.setUserResponses(userResponses);
        assertEquals("try again", quizQuestion.getFeedback(DEFAULT_LANG));
        assertEquals((float) 0, quizQuestion.getUserscore());
        assertEquals(0, quizQuestion.getScoreAsPercent());

        // check json response object
        JSONObject responseJson = quizQuestion.responsesToJSON();
        assertTrue(responseJson.has("question_id"));
        assertTrue(responseJson.has("score"));
        assertTrue(responseJson.has("text"));

        assertEquals(19769, responseJson.get("question_id"));
        assertEquals(0.0, responseJson.get("score"));
        assertEquals("Black", responseJson.get("text"));

        // check for whole quiz
        assertEquals((float) 0, quizWithFeedback.getUserscore());
        quizWithFeedback.mark(DEFAULT_LANG);
        assertEquals((float) 0, quizWithFeedback.getUserscore());
    }

    // incorrect answer (invalid selection)
    @Test
    public void test_incorrectWithFeedbackInvalid()throws Exception {
        QuizQuestion quizQuestion = quizWithFeedback.getCurrentQuestion();
        assertTrue(quizQuestion instanceof MultiChoice);

        ArrayList<String> userResponses = new ArrayList<>();
        userResponses.add("Purple");
        quizQuestion.setUserResponses(userResponses);
        // no feedback as invalid selection
        assertEquals("", quizQuestion.getFeedback(DEFAULT_LANG));
        assertEquals((float) 0, quizQuestion.getUserscore());
        assertEquals(0, quizQuestion.getScoreAsPercent());

        // check json response object
        JSONObject responseJson = quizQuestion.responsesToJSON();
        assertTrue(responseJson.has("question_id"));
        assertTrue(responseJson.has("score"));
        assertTrue(responseJson.has("text"));

        assertEquals(19769, responseJson.get("question_id"));
        assertEquals(0.0, responseJson.get("score"));
        assertEquals("Purple", responseJson.get("text"));

        // check for whole quiz
        assertEquals((float) 0, quizWithFeedback.getUserscore());
        quizWithFeedback.mark(DEFAULT_LANG);
        assertEquals((float) 0, quizWithFeedback.getUserscore());
    }

    /*
    Multilang, no feedback
     */


    /*
    Multilang, with feedback
     */
}
