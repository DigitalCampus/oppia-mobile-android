package testFiles.model.quiz;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import org.digitalcampus.mobile.quiz.Quiz;
import org.digitalcampus.mobile.quiz.model.QuizQuestion;
import org.digitalcampus.mobile.quiz.model.questiontypes.ShortAnswer;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import testFiles.utils.BaseTest;
import testFiles.utils.UnitTestsFileUtils;

public class ShortAnswerQuestionTest {

    private static final String SHORTANSWER_NOFEEDBACK_JSON = BaseTest.PATH_QUIZZES + "/shortanswer_no_feedback.json";
    private static final String SHORTANSWER_WITH_FEEDBACK_JSON = BaseTest.PATH_QUIZZES + "/shortanswer_with_feedback.json";
    private static final String DEFAULT_LANG = "en";
    private Quiz quizNoFeedback;
    private Quiz quizWithFeedback;


    @Before
    public void setup() throws Exception {
        // Setting up before every test
        String quizNoFeedbackContent = UnitTestsFileUtils.readFileFromTestResources(SHORTANSWER_NOFEEDBACK_JSON);
        quizNoFeedback = new Quiz();
        quizNoFeedback.load(quizNoFeedbackContent, DEFAULT_LANG);


        String quizWithFeedbackContent = UnitTestsFileUtils.readFileFromTestResources(SHORTANSWER_WITH_FEEDBACK_JSON);
        quizWithFeedback = new Quiz();
        quizWithFeedback.load(quizWithFeedbackContent, DEFAULT_LANG);
    }

    /*
     no feedback
     */
    // correct
    @Test
    public void test_correctNoFeedback()throws Exception {
        QuizQuestion quizQuestion = quizNoFeedback.getCurrentQuestion();
        assertTrue(quizQuestion instanceof ShortAnswer);

        // check before response added
        JSONObject responseJson = quizQuestion.responsesToJSON();

        assertTrue(responseJson.has("question_id"));
        assertTrue(responseJson.has("score"));
        assertTrue(responseJson.has("text"));

        assertEquals(19775, responseJson.get("question_id"));
        assertEquals(0.0, responseJson.get("score"));
        assertEquals("", responseJson.get("text"));

        ArrayList<String> userResponses = new ArrayList<>();
        userResponses.add("madrid");
        quizQuestion.setUserResponses(userResponses);
        assertEquals("", quizQuestion.getFeedback(DEFAULT_LANG));
        assertEquals((float) 1, quizQuestion.getUserscore());
        assertEquals(100, quizQuestion.getScoreAsPercent());

        // check json response object
        responseJson = quizQuestion.responsesToJSON();
        assertTrue(responseJson.has("question_id"));
        assertTrue(responseJson.has("score"));
        assertTrue(responseJson.has("text"));

        assertEquals(19775, responseJson.get("question_id"));
        assertEquals(1.0, responseJson.get("score"));
        assertEquals("madrid", responseJson.get("text"));

        // check for whole quiz
        assertEquals((float) 0, quizNoFeedback.getUserscore());
        quizNoFeedback.mark(DEFAULT_LANG);
        assertEquals((float) 1, quizNoFeedback.getUserscore());
    }

    // case difference
    @Test
    public void test_correctCaseInsenstiveNoFeedback()throws Exception {
        QuizQuestion quizQuestion = quizNoFeedback.getCurrentQuestion();
        assertTrue(quizQuestion instanceof ShortAnswer);

        ArrayList<String> userResponses = new ArrayList<>();
        userResponses.add("MADRID");
        quizQuestion.setUserResponses(userResponses);
        assertEquals("", quizQuestion.getFeedback(DEFAULT_LANG));
        assertEquals((float) 1, quizQuestion.getUserscore());
        assertEquals(100, quizQuestion.getScoreAsPercent());

        // check json response object
        JSONObject responseJson = quizQuestion.responsesToJSON();
        assertTrue(responseJson.has("question_id"));
        assertTrue(responseJson.has("score"));
        assertTrue(responseJson.has("text"));

        assertEquals(19775, responseJson.get("question_id"));
        assertEquals(1.0, responseJson.get("score"));
        assertEquals("MADRID", responseJson.get("text"));

        // check for whole quiz
        assertEquals((float) 0, quizNoFeedback.getUserscore());
        quizNoFeedback.mark(DEFAULT_LANG);
        assertEquals((float) 1, quizNoFeedback.getUserscore());
    }

    // incorrect
    @Test
    public void test_incorrectNoFeedback()throws Exception {
        QuizQuestion quizQuestion = quizNoFeedback.getCurrentQuestion();
        assertTrue(quizQuestion instanceof ShortAnswer);

        ArrayList<String> userResponses = new ArrayList<>();
        userResponses.add("barcelona");
        quizQuestion.setUserResponses(userResponses);
        assertEquals("", quizQuestion.getFeedback(DEFAULT_LANG));
        assertEquals((float) 0, quizQuestion.getUserscore());
        assertEquals(0, quizQuestion.getScoreAsPercent());

        // check json response object
        JSONObject responseJson = quizQuestion.responsesToJSON();
        assertTrue(responseJson.has("question_id"));
        assertTrue(responseJson.has("score"));
        assertTrue(responseJson.has("text"));

        assertEquals(19775, responseJson.get("question_id"));
        assertEquals(0.0, responseJson.get("score"));
        assertEquals("barcelona", responseJson.get("text"));

        // check for whole quiz
        assertEquals((float) 0, quizNoFeedback.getUserscore());
        quizNoFeedback.mark(DEFAULT_LANG);
        assertEquals((float) 0, quizNoFeedback.getUserscore());
    }

    /*
    with feedback
     */

    // correct
    @Test
    public void test_correctWithFeedback()throws Exception {
        QuizQuestion quizQuestion = quizWithFeedback.getCurrentQuestion();
        assertTrue(quizQuestion instanceof ShortAnswer);

        ArrayList<String> userResponses = new ArrayList<>();
        userResponses.add("Rome");
        quizQuestion.setUserResponses(userResponses);
        assertEquals("correct", quizQuestion.getFeedback(DEFAULT_LANG));
        assertEquals((float) 1, quizQuestion.getUserscore());
        assertEquals(100, quizQuestion.getScoreAsPercent());

        // check json response object
        JSONObject responseJson = quizQuestion.responsesToJSON();
        assertTrue(responseJson.has("question_id"));
        assertTrue(responseJson.has("score"));
        assertTrue(responseJson.has("text"));

        assertEquals(19776, responseJson.get("question_id"));
        assertEquals(1.0, responseJson.get("score"));
        assertEquals("Rome", responseJson.get("text"));

        // check for whole quiz
        assertEquals((float) 0, quizWithFeedback.getUserscore());
        quizWithFeedback.mark(DEFAULT_LANG);
        assertEquals((float) 1, quizWithFeedback.getUserscore());
    }

    // incorrect
    @Test
    public void test_incorrectWithFeedback()throws Exception {
        QuizQuestion quizQuestion = quizWithFeedback.getCurrentQuestion();
        assertTrue(quizQuestion instanceof ShortAnswer);

        ArrayList<String> userResponses = new ArrayList<>();
        userResponses.add("Milan");
        quizQuestion.setUserResponses(userResponses);
        assertEquals("wrong", quizQuestion.getFeedback(DEFAULT_LANG));
        assertEquals((float) 0, quizQuestion.getUserscore());
        assertEquals(0, quizQuestion.getScoreAsPercent());

        // check json response object
        JSONObject responseJson = quizQuestion.responsesToJSON();
        assertTrue(responseJson.has("question_id"));
        assertTrue(responseJson.has("score"));
        assertTrue(responseJson.has("text"));

        assertEquals(19776, responseJson.get("question_id"));
        assertEquals(0.0, responseJson.get("score"));
        assertEquals("Milan", responseJson.get("text"));

        // check for whole quiz
        assertEquals((float) 0, quizWithFeedback.getUserscore());
        quizWithFeedback.mark(DEFAULT_LANG);
        assertEquals((float) 0, quizWithFeedback.getUserscore());
    }
}
