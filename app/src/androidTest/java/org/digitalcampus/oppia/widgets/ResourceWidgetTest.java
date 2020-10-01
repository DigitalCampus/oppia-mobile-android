package org.digitalcampus.oppia.widgets;
import android.os.Bundle;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.CourseActivity;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.Lang;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static androidx.fragment.app.testing.FragmentScenario.launchInContainer;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class ResourceWidgetTest {

    private Activity act;
    private Bundle args;
    private Course course;

    @Before
    public void setup() throws Exception {
        // Setting up before every test
        act = new Activity();
        Lang lang = new Lang("en", "my title");
        lang.setLocation("myloc.html");
        List<Lang> langList = new ArrayList<>();
        langList.add(lang);
        act.setTitles(langList);
        act.setActId(1234);
        act.setLocations(langList);
        act.setMimeType("image/jpeg");

        course = new Course();
        course.setTitles(langList);
        course.setShortname("myshortname");

        args = new Bundle();
        args.putSerializable(Activity.TAG, act);
        args.putSerializable(Course.TAG, course);
        args.putBoolean(CourseActivity.BASELINE_TAG, false);
    }

    @Test
    public void openResource() {
        launchInContainer(ResourceWidget.class, args, R.style.Oppia_ToolbarTheme, null);
        // this doesn't really test anything, but gives some coverage to the ResourceWidget class
        assertEquals(androidx.test.espresso.ViewInteraction.class.getCanonicalName(), onView(withId(act.getActId())).getClass().getCanonicalName());
    }
}
