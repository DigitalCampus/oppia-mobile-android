package androidTestFiles.org.digitalcampus.oppia.activity;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.digitalcampus.oppia.activity.CourseActivity;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.Section;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;



@RunWith(AndroidJUnit4.class)
public class CourseActivityTest {


    @Test
    public void testActivityOpen() {

        Section s = new Section();
        s.setTitlesFromJSONString("{'en': 'my section'}");

        Course c = new Course();
        c.setTitlesFromJSONString("{'en': 'my course'}");
        Intent intent = new Intent();
        intent.putExtra(Section.TAG, s);
        intent.putExtra(Course.TAG, c);

        // TODO stub...
//        try (ActivityScenario<CourseActivity> scenario = ActivityScenario.launch(CourseActivity.class)) {
//
//        }

    }
}
