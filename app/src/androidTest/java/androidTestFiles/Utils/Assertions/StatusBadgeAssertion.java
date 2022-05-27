package androidTestFiles.Utils.Assertions;

import static org.junit.Assert.assertEquals;

import androidx.test.espresso.ViewAssertion;

import org.digitalcampus.oppia.utils.course_status.CourseStatusBadgeView;

public class StatusBadgeAssertion {

    public static ViewAssertion withText(String text) {
        return (view, noViewFoundException) -> {
            if (noViewFoundException != null) {
                throw noViewFoundException;
            }

            String viewText = ((CourseStatusBadgeView) view).getText();
            assertEquals(text, viewText);
        };
    }
}
