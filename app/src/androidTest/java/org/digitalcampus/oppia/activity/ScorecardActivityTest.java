package org.digitalcampus.oppia.activity;

import android.os.Bundle;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.digitalcampus.oppia.model.Activity;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class ScorecardActivityTest {

    private Activity act;
    private Bundle args;

    @Rule
    public ActivityTestRule<ScorecardActivity> scorecardActivityTestRule =
            new ActivityTestRule<>(ScorecardActivity.class, false, false);

    @Before
    public void setup() throws Exception {
        // Setting up before every test
        act = new Activity();
    }

    @Test
    public void testActivityOpen() {
        scorecardActivityTestRule.launchActivity(null);
    }
}
