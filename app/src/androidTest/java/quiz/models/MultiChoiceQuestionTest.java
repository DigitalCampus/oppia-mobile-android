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
import java.util.List;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class MultiChoiceQuestionTest {

    private static final String MULTICHOICE_QUIZ_JSON = "quizzes/multichoice_nofeedback.json";
    private static final String DEFAULT_LANG = "en";
    private Quiz quiz;

    @Before
    public void setup() throws Exception {
        // Setting up before every test
        String quizContent = Utils.FileUtils.getStringFromFile(
                InstrumentationRegistry.getInstrumentation().getContext(), MULTICHOICE_QUIZ_JSON);
        quiz = new Quiz();
        quiz.load(quizContent, DEFAULT_LANG);
    }

    /*
    No Feedback
     */
    // correct answer
    @Test
    public void test_correctNoFeedback()throws Exception {
        QuizQuestion quizQuestion = quiz.getCurrentQuestion();
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
        assertEquals((float) 0, quiz.getUserscore());
        quiz.mark(DEFAULT_LANG);
        assertEquals((float) 1, quiz.getUserscore());
    }

    // incorrect answer (valid selection)
    @Test
    public void test_incorrectNoFeedback()throws Exception {
        QuizQuestion quizQuestion = quiz.getCurrentQuestion();
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
        assertEquals((float) 0, quiz.getUserscore());
        quiz.mark(DEFAULT_LANG);
        assertEquals((float) 0, quiz.getUserscore());
    }

    // incorrect answer (invalid selection)
    @Test
    public void test_incorrectNoFeedbackInvalidOption()throws Exception {
        QuizQuestion quizQuestion = quiz.getCurrentQuestion();
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
        assertEquals((float) 0, quiz.getUserscore());
        quiz.mark(DEFAULT_LANG);
        assertEquals((float) 0, quiz.getUserscore());
    }

    /*
    With feedback
     */

    /*
    Multilang, no feedback
     */


    /*
    Multilang, with feedback
     */
}
