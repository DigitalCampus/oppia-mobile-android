package UI;


import android.os.Bundle;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.fragments.BadgesFragment;
import org.digitalcampus.oppia.model.Activity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


import Utils.Assertions.RecyclerViewItemCountAssertion;
import Utils.MockedApiEndpointTest;

import static androidx.fragment.app.testing.FragmentScenario.launchInContainer;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
public class BadgesUITest extends MockedApiEndpointTest {

    private static final String VALID_BADGES_RESPONSE_NOT_EMPTY = "responses/response_200_badges_not_empty.json";
    private static final String VALID_BADGES_RESPONSE_EMPTY = "responses/response_200_badges_empty.json";

    private Bundle args;

    @Before
    public void setup() throws Exception {

    }

    @Test
    public void showErrorStateOnWrongAPIRequest() throws Exception {

        startServer(400, null, 200);

        launchInContainer(BadgesFragment.class, args, R.style.Oppia_ToolbarTheme, null);

        onView(withId(R.id.error_state)).check(matches(isDisplayed()));
        onView(withId(R.id.empty_state)).check(matches(not(isDisplayed())));
        onView(withId(R.id.loading_badges)).check(matches(not(isDisplayed())));
    }

    @Test
    public void showErrorOnInvalidResponseJson() throws Exception {

        startServer(200, ERROR_MESSAGE_BODY, 200);

        launchInContainer(BadgesFragment.class, args, R.style.Oppia_ToolbarTheme, null);

        onView(withText(R.string.error_connection)).check(matches(isDisplayed()));

        onView(withText(R.string.close)).perform(click());

        onView(withId(R.id.error_state)).check(matches(isDisplayed()));
        onView(withId(R.id.empty_state)).check(matches(not(isDisplayed())));
        onView(withId(R.id.loading_badges)).check(matches(not(isDisplayed())));
    }


    @Test
    public void showEmptyStateWhenNoBadges() throws Exception {

        startServer(200, VALID_BADGES_RESPONSE_EMPTY, 200);

        launchInContainer(BadgesFragment.class, args, R.style.Oppia_ToolbarTheme, null);

        onView(withId(R.id.error_state)).check(matches(not(isDisplayed())));
        onView(withId(R.id.empty_state)).check(matches(isDisplayed()));
        onView(withId(R.id.loading_badges)).check(matches(not(isDisplayed())));

    }

    @Test
    public void showBadgesWhenNotEmpty() throws Exception {

        startServer(200, VALID_BADGES_RESPONSE_NOT_EMPTY, 200);

        launchInContainer(BadgesFragment.class, args, R.style.Oppia_ToolbarTheme, null);

        onView(withId(R.id.error_state)).check(matches(not(isDisplayed())));
        onView(withId(R.id.empty_state)).check(matches(not(isDisplayed())));
        onView(withId(R.id.loading_badges)).check(matches(not(isDisplayed())));

        onView(withId(R.id.recycler_badges)).check(new RecyclerViewItemCountAssertion(2));

    }
}
