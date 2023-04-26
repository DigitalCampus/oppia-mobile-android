package androidTestFiles.features.quiz;

import androidTestFiles.utils.parent.BaseTest;
import androidTestFiles.widgets.quiz.BaseQuizTest;

public class ChangeAnswersUiTest extends BaseQuizTest {

    private static final String DESCRIPTION_QUESTION_JSON =
            BaseTest.PATH_QUIZZES + "/description_question.json";

    @Override
    protected String getQuizContentFile() {
        return null;
    }
}
