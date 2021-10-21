package androidTestFiles.UI.prefs;

import static androidx.fragment.app.testing.FragmentScenario.launchInContainer;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.DrawerMatchers.isClosed;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.isA;

import android.view.Gravity;
import android.view.View;
import android.widget.Checkable;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.RecyclerViewActions;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.fragments.prefs.NotificationsPrefsFragment;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Test;

public class ReminderPrefsTest {


    @Test
    public void showWarningIfZeroDaysSelected() throws Exception {

        launchInContainer(NotificationsPrefsFragment.class, null, R.style.Oppia_ToolbarTheme, null);

        onView(withId(androidx.preference.R.id.recycler_view))
                .perform(RecyclerViewActions.actionOnItem(
                        hasDescendant(withText(R.string.prefCoursesReminderDaysTitle)), click()));

        onView(withText(R.string.week_day_1)).perform(setChecked(false));
        onView(withText(R.string.week_day_2)).perform(setChecked(false));
        onView(withText(R.string.week_day_3)).perform(setChecked(false));
        onView(withText(R.string.week_day_4)).perform(setChecked(false));
        onView(withText(R.string.week_day_5)).perform(setChecked(false));
        onView(withText(R.string.week_day_6)).perform(setChecked(false));
        onView(withText(R.string.week_day_7)).perform(setChecked(false));

        onView(withText(android.R.string.ok))
                .inRoot(isDialog())
                .check(matches(isDisplayed()))
                .perform(click());

        onView(withText(R.string.warning_reminder_at_least_one_day)).check(matches(isDisplayed()));


    }

    @Test
    public void showWarningIfMoreThanOneDayInWeeklyInterval() throws Exception {
        // TODO
    }

    @Test
    public void checkDaysReducedToOneIfWeeklyIntervalIsSelected() throws Exception {
        // TODO
    }

    public static ViewAction setChecked(final boolean checked) {
        return new ViewAction() {
            @Override
            public BaseMatcher<View> getConstraints() {
                return new BaseMatcher<View>() {
                    @Override
                    public boolean matches(Object item) {
                        return isA(Checkable.class).matches(item);
                    }

                    @Override
                    public void describeMismatch(Object item, Description mismatchDescription) {
                    }

                    @Override
                    public void describeTo(Description description) {
                    }
                };
            }

            @Override
            public String getDescription() {
                return null;
            }

            @Override
            public void perform(UiController uiController, View view) {
                Checkable checkableView = (Checkable) view;
                checkableView.setChecked(checked);
            }
        };
    }

}
