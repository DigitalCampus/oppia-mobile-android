package testFiles.features;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

import org.digitalcampus.mobile.quiz.Quiz;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class CheckMustShowQuizResultsTest {

    private Quiz createQuiz(Boolean showAtEnd, Boolean showLater) throws JSONException {

        JSONObject jObjQuiz = new JSONObject();
        jObjQuiz.put(Quiz.JSON_PROPERTY_ID, 1);
        jObjQuiz.put(Quiz.JSON_PROPERTY_TITLE, "test");
        jObjQuiz.put(Quiz.JSON_PROPERTY_QUESTIONS, new JSONArray());

        JSONObject jObjProps = new JSONObject();

        if (showAtEnd != null) {
            jObjProps.put("immediate_whether_correct", showAtEnd);
        }

        if (showLater != null) {
            jObjProps.put("later_whether_correct", showLater);
        }

        jObjQuiz.put(Quiz.JSON_PROPERTY_PROPS, jObjProps);

        Quiz quiz = new Quiz();
        boolean loadOk = quiz.load(jObjQuiz.toString(), "");
        assertTrue(loadOk);

        return quiz;
    }

    @Test
    public void showAtEndIfValueTrue()throws Exception {
        Quiz quiz = createQuiz(true, null);
        boolean mustShow = quiz.mustShowQuizResultsAtEnd();
        assertThat(mustShow, equalTo(true));
    }

    @Test
    public void showAtEndIfValueMissing()throws Exception {
        Quiz quiz = createQuiz(null, null);
        boolean mustShow = quiz.mustShowQuizResultsAtEnd();
        assertThat(mustShow, equalTo(true));
    }

    @Test
    public void dontShowAtEndIfValueFalse()throws Exception {
        Quiz quiz = createQuiz(false, null);
        boolean mustShow = quiz.mustShowQuizResultsAtEnd();
        assertThat(mustShow, equalTo(false));
    }

    @Test
    public void showLaterIfValueTrue()throws Exception {
        Quiz quiz = createQuiz(null, true);
        boolean mustShow = quiz.mustShowQuizResultsLater();
        assertThat(mustShow, equalTo(true));
    }

    @Test
    public void showLaterIfValueMissing()throws Exception {
        Quiz quiz = createQuiz(null, null);
        boolean mustShow = quiz.mustShowQuizResultsLater();
        assertThat(mustShow, equalTo(true));
    }

    @Test
    public void dontShowLaterIfValueFalse()throws Exception {
        Quiz quiz = createQuiz(null, false);
        boolean mustShow = quiz.mustShowQuizResultsLater();
        assertThat(mustShow, equalTo(false));
    }
}
