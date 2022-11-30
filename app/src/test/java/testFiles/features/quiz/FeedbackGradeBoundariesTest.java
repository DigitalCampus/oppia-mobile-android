package testFiles.features.quiz;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import org.digitalcampus.mobile.quiz.Quiz;
import org.json.JSONObject;
import org.junit.Test;
import org.mockito.Mockito;

import testFiles.utils.UnitTestsFileUtils;

public class FeedbackGradeBoundariesTest {

    private static final String FEEDBACK_JSON_FILE = "quizzes/feedback_grade_boundaries.json";
    private static final String FEEDBACK_MESSAGE_25 = "Quiz score is between 0% and 49%. MaxQuizScore: 8.0; UserScore: 2.0; QuizScore: 25.0%";
    private static final String FEEDBACK_MESSAGE_75 = "Quiz score is between 50% and 99%. MaxQuizScore: 8.0; UserScore: 6.0; QuizScore: 75.0%";
    private static final String FEEDBACK_MESSAGE_100 = "Quiz score is 100%. MaxQuizScore: 8.0; UserScore: 8.0; QuizScore: 100.0%";

    @Test
    public void testCorrectFeedbackMessageIsShown() throws Exception {
        Quiz quiz = Mockito.spy(new Quiz());
        String quizTxt = UnitTestsFileUtils.readFileFromTestResources(FEEDBACK_JSON_FILE);
        JSONObject jObjQuiz = new JSONObject(quizTxt);
        quiz.load(jObjQuiz.toString(), "");

        when(quiz.getUserscore()).thenReturn(2f); // 2 out of 8 - 25%
        String gradeBoundaryMessage = quiz.getFeedbackMessageBasedOnQuizGrade(quiz.getQuizPercentageScore());
        assertThat(gradeBoundaryMessage, equalTo(FEEDBACK_MESSAGE_25));

        when(quiz.getUserscore()).thenReturn(6f); // 6 out of 8 - 75%
        gradeBoundaryMessage = quiz.getFeedbackMessageBasedOnQuizGrade(quiz.getQuizPercentageScore());
        assertThat(gradeBoundaryMessage, equalTo(FEEDBACK_MESSAGE_75));

        when(quiz.getUserscore()).thenReturn(8f); // 8 out of 8 - 100%
        gradeBoundaryMessage = quiz.getFeedbackMessageBasedOnQuizGrade(quiz.getQuizPercentageScore());
        assertThat(gradeBoundaryMessage, equalTo(FEEDBACK_MESSAGE_100));
    }
}
