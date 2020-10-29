package UI;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.DownloadMediaActivity;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
public class DownloadMediaUITest {

    @Rule
    public ActivityTestRule<DownloadMediaActivity> downloadMediaActivityTestRule =
            new ActivityTestRule<>(DownloadMediaActivity.class, false, false);

    @Test
    public void testActivityOpen(){
        downloadMediaActivityTestRule.launchActivity(null);

        onView(withId(R.id.home_messages))
                .check(matches(not(isDisplayed())));
    }
}
