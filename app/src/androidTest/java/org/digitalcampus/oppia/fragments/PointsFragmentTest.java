package org.digitalcampus.oppia.fragments;

import android.os.Bundle;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.model.Course;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.fragment.app.testing.FragmentScenario.launchInContainer;

@RunWith(AndroidJUnit4.class)
public class PointsFragmentTest {

    private Bundle args;
    @Before
    public void setup() throws Exception {
        args = new Bundle();
        args.putSerializable(Course.TAG, new Course(""));
    }

    @Test
    public void openPointsFragment(){
        launchInContainer(PointsFragment.class, args, R.style.Oppia_ToolbarTheme, null);
        // onView(withId(R.id.tv_total_points)).check(matches(withText("0")));
    }
}
