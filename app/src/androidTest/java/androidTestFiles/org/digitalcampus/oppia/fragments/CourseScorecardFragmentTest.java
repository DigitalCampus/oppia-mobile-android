package androidTestFiles.org.digitalcampus.oppia.fragments;

import android.Manifest;
import android.os.Bundle;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.Lang;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;


@RunWith(AndroidJUnit4.class)
public class CourseScorecardFragmentTest {
    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    private Bundle args;
    @Before
    public void setup() throws Exception {
        Lang lang = new Lang("en", "my title");
        List<Lang> langList = new ArrayList<>();
        langList.add(lang);

        Course course = new Course();
        course.setTitles(langList);
        course.setShortname("myshortname");
        course.setNoActivities(10);

        args = new Bundle();
        args.putSerializable(Course.TAG, course);
    }

    @Test
    public void openCourseScorecardFragment(){
        // TODO stub - needs an actual course xml file to load
        // launchInContainer(CourseScorecardFragment.class, args, R.style.Oppia_ToolbarTheme, null);
        // onView(withId(R.id.tv_total_points)).check(matches(withText("0")));
    }
}
