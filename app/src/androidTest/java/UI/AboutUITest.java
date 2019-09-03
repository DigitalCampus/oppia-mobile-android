package UI;

import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.rule.ActivityTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.digitalcampus.oppia.activity.AboutActivity;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.AllOf.allOf;

@RunWith(AndroidJUnit4.class)
public class AboutUITest {

    @Rule
    public ActivityTestRule<AboutActivity> aboutActivityTestRule =
            new ActivityTestRule<>(AboutActivity.class);



    @Test
    public void clickPrivacyTab() throws Exception {

        /*onView(allOf(withText(R.string.tab_title_privacy), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE),
                isDescendantOfA(withId(R.id.tabs_toolbar))))
                .perform(click());*/
    }

    /*
    @Test
    public void clickHelpTab() throws Exception {

        onView(allOf(withText(R.string.tab_title_help), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE),
                isDescendantOfA(withId(R.id.tabs_toolbar))))
                .perform(click());
    }

    @Test
    public void clickAboutTab() throws Exception {

        onView(allOf(withText(R.string.tab_title_about), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE),
                isDescendantOfA(withId(R.id.tabs_toolbar))))
                .perform(click());
    }

    */
}
