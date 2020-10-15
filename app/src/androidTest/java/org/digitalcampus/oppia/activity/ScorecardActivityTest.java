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

    @Rule
    public ActivityTestRule<ScorecardActivity> scorecardActivityTestRule =
            new ActivityTestRule<>(ScorecardActivity.class, false, false);

    @Test
    public void testActivityOpen() {
        scorecardActivityTestRule.launchActivity(null);
    }
}
