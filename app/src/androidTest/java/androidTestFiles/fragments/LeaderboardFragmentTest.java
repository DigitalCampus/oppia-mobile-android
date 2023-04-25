package androidTestFiles.fragments;

import android.Manifest;
import android.os.Bundle;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.fragments.LeaderboardFragment;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


import androidTestFiles.utils.parent.BaseTest;
import androidTestFiles.utils.parent.MockedApiEndpointTest;
import androidx.test.rule.GrantPermissionRule;

import static androidx.fragment.app.testing.FragmentScenario.launchInContainer;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidTestFiles.utils.UITestActionsUtils.waitForView;

@RunWith(AndroidJUnit4.class)
public class LeaderboardFragmentTest extends MockedApiEndpointTest {
    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    private static final String VALID_LEADERBOARD_RESPONSE = BaseTest.PATH_RESPONSES + "/response_201_login.json";

    private Bundle args;
    @Before
    public void setup() throws Exception {
        args = new Bundle();
    }

    @Test
    public void openLeaderboardFragment(){
        startServer(200, VALID_LEADERBOARD_RESPONSE, 0);

        launchInContainer(LeaderboardFragment.class, args, R.style.Oppia_ToolbarTheme);
    }

    @Test
    public void openleaderboard_ServerError(){
        startServer(400, null, 0);

        launchInContainer(LeaderboardFragment.class, args, R.style.Oppia_ToolbarTheme);
        waitForView(withId(R.id.error_state)).check(matches(isDisplayed()));
    }
}
