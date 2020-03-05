package quiz.models;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.mobile.quiz.Quiz;
import org.digitalcampus.mobile.quiz.model.QuizQuestion;
import org.digitalcampus.mobile.quiz.model.questiontypes.Matching;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.DecimalFormat;
import java.util.ArrayList;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class MatchingQuestionTest {

    private static final String MATCHING_DEFAULT_FEEDBACK_JSON = "quizzes/matching_default_feedback.json";
    private static final String MATCHING_WITH_FEEDBACK_JSON = "quizzes/matching_with_feedback.json";
    private static final String DEFAULT_LANG = "en";
    private Quiz quizDefaultFeedback;
    private Quiz quizWithFeedback;

    private static DecimalFormat df = new DecimalFormat("0.00");

    @Before
    public void setup() throws Exception {
        // Setting up before every test
        String quizDefaultFeedbackContent = Utils.FileUtils.getStringFromFile(
                InstrumentationRegistry.getInstrumentation().getContext(), MATCHING_DEFAULT_FEEDBACK_JSON);
        quizDefaultFeedback = new Quiz();
        quizDefaultFeedback.load(quizDefaultFeedbackContent, DEFAULT_LANG);

        String quizWithFeedbackContent = Utils.FileUtils.getStringFromFile(
                InstrumentationRegistry.getInstrumentation().getContext(), MATCHING_WITH_FEEDBACK_JSON);
        quizWithFeedback = new Quiz();
        quizWithFeedback.load(quizWithFeedbackContent, DEFAULT_LANG);
    }

    /*
    Default Feedback
     */
    // correct answer
    @Test
    public void test_correctNoFeedback()throws Exception {
        QuizQuestion quizQuestion = quizDefaultFeedback.getCurrentQuestion();
        assertTrue(quizQuestion instanceof Matching);

        // check before response added
        JSONObject responseJson = quizQuestion.responsesToJSON();
        assertTrue(responseJson.has("question_id"));
        assertTrue(responseJson.has("score"));
        assertTrue(responseJson.has("text"));

        assertEquals(19778, responseJson.get("question_id"));
        assertEquals(0.0, responseJson.get("score"));
        assertEquals("", responseJson.get("text"));


        ArrayList<String> userResponses = new ArrayList<>();
        userResponses.add("Rock beats|Scissors");
        userResponses.add("Paper beats|Rock");
        userResponses.add("Scissors beat|Paper");
        quizQuestion.setUserResponses(userResponses);
        assertEquals("Your answer is correct.", quizQuestion.getFeedback(DEFAULT_LANG));
        assertEquals(df.format(0.9999), df.format(quizQuestion.getUserscore()));
        assertEquals(99, quizQuestion.getScoreAsPercent());

        // check json response object
        responseJson = quizQuestion.responsesToJSON();
        assertTrue(responseJson.has("question_id"));
        assertTrue(responseJson.has("score"));
        assertTrue(responseJson.has("text"));

        assertEquals(19778, responseJson.get("question_id"));
        assertEquals(df.format(1.0), df.format(responseJson.get("score")));
        assertEquals("Rock beats|Scissors||Paper beats|Rock||Scissors beat|Paper||", responseJson.get("text"));

        // check for whole quiz
        assertEquals((float) 0, quizDefaultFeedback.getUserscore());
        quizDefaultFeedback.mark(DEFAULT_LANG);
        assertEquals(df.format(0.9999), df.format(quizDefaultFeedback.getUserscore()));
    }

    // partially correct
    @Test
    public void test_partialCorrectNoFeedback()throws Exception {
        QuizQuestion quizQuestion = quizDefaultFeedback.getCurrentQuestion();
        assertTrue(quizQuestion instanceof Matching);

        ArrayList<String> userResponses = new ArrayList<>();
        userResponses.add("Rock beats|Scissors");
        userResponses.add("Paper beats|Scissors");
        userResponses.add("Scissors beat|Scissors");
        quizQuestion.setUserResponses(userResponses);
        assertEquals("Your answer is partially correct.", quizQuestion.getFeedback(DEFAULT_LANG));
        assertEquals(df.format(0.3333), df.format(quizQuestion.getUserscore()));
        assertEquals(33, quizQuestion.getScoreAsPercent());

        // check json response object
        JSONObject responseJson = quizQuestion.responsesToJSON();
        assertTrue(responseJson.has("question_id"));
        assertTrue(responseJson.has("score"));
        assertTrue(responseJson.has("text"));

        assertEquals(19778, responseJson.get("question_id"));
        assertEquals(df.format(0.33), df.format(responseJson.get("score")));
        assertEquals("Rock beats|Scissors||Paper beats|Scissors||Scissors beat|Scissors||", responseJson.get("text"));

        // check for whole quiz
        assertEquals((float) 0, quizDefaultFeedback.getUserscore());
        quizDefaultFeedback.mark(DEFAULT_LANG);
    }


    // no correct

    @Test
    public void test_noCorrectNoFeedback()throws Exception {
        QuizQuestion quizQuestion = quizDefaultFeedback.getCurrentQuestion();
        assertTrue(quizQuestion instanceof Matching);

        ArrayList<String> userResponses = new ArrayList<>();
        userResponses.add("Rock beats|Rock");
        userResponses.add("Paper beats|Paper");
        userResponses.add("Scissors beat|Scissors");
        quizQuestion.setUserResponses(userResponses);
        assertEquals("Your answer is incorrect.", quizQuestion.getFeedback(DEFAULT_LANG));
        assertEquals(df.format(0), df.format(quizQuestion.getUserscore()));
        assertEquals(0, quizQuestion.getScoreAsPercent());

        // check json response object
        JSONObject responseJson = quizQuestion.responsesToJSON();
        assertTrue(responseJson.has("question_id"));
        assertTrue(responseJson.has("score"));
        assertTrue(responseJson.has("text"));

        assertEquals(19778, responseJson.get("question_id"));
        assertEquals(df.format(0), df.format(responseJson.get("score")));
        assertEquals("Rock beats|Rock||Paper beats|Paper||Scissors beat|Scissors||", responseJson.get("text"));

        // check for whole quiz
        assertEquals((float) 0, quizDefaultFeedback.getUserscore());
        quizDefaultFeedback.mark(DEFAULT_LANG);
    }
}
