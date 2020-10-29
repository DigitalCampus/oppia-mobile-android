package org.digitalcampus.oppia.fragments;

import android.os.Bundle;

import Utils.MockedApiEndpointTest;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.digitalcampus.mobile.learning.R;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.fragment.app.testing.FragmentScenario.launchInContainer;

@RunWith(AndroidJUnit4.class)
public class LeaderboardFragmentTest extends MockedApiEndpointTest {

    private static final String VALID_LEADERBOARD_RESPONSE = "responses/response_201_login.json";

    private Bundle args;
    @Before
    public void setup() throws Exception {
        args = new Bundle();
    }

    @Test
    public void openLeaderboardFragment(){
        startServer(200, VALID_LEADERBOARD_RESPONSE, 0);

        launchInContainer(LeaderboardFragment.class, args, R.style.Oppia_ToolbarTheme, null);
        // onView(withId(R.id.tv_total_points)).check(matches(withText("0")));
    }
}
