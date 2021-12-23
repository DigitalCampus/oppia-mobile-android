package androidTestFiles.UI.prefs;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.DrawerMatchers.isOpen;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.MainActivity;
import org.digitalcampus.oppia.utils.storage.Storage;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import androidTestFiles.Utils.Assertions.RecyclerViewItemCountAssertion;
import androidTestFiles.database.BaseTestDB;

@RunWith(AndroidJUnit4.class)
public class ExportFullActivityUITest extends BaseTestDB {

    private static final int NUM_QUIZ_ATTEMPTS_TEST = 2;
    private static final int NUM_TRACKER_TEST = 3;

    @After
    public void cleanActivityFiles() throws Exception {

        File activityPath = new File(Storage.getActivityPath(getContext()));
        for (String filename : activityPath.list()) {
            File file = new File(activityPath, filename);
            file.delete();
        }
    }

    @Test
    public void checkExportActivityCompleteUIFlow() throws Exception {

        getTestDataManager().addQuizAttempts(NUM_QUIZ_ATTEMPTS_TEST);
        getTestDataManager().addTrackers(NUM_TRACKER_TEST);

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            openDrawer();
            performClickDrawerItem(R.id.menu_settings);
            clickPrefWithText(R.string.prefAdvanced_title);
            clickPrefWithText(R.string.pref_export_full_activity_title);
            pressBack();
            pressBack();
            pressBack();
            openDrawer();
            performClickDrawerItem(R.id.menu_activitylog);
            onView(withId(R.id.exported_files_list)).check(new RecyclerViewItemCountAssertion(1));
            onView(withId(R.id.highlight_to_export)).check(matches(withText(String.valueOf(NUM_TRACKER_TEST))));

            onView(withId(R.id.export_btn)).perform(click());
            pressBack();

            onView(withId(R.id.exported_files_list)).check(new RecyclerViewItemCountAssertion(2));
            onView(withId(R.id.highlight_to_export)).check(matches(withText(String.valueOf(0))));

        }
    }

    private void clickPrefWithText(int prefTitleId) {
        onView(withId(androidx.preference.R.id.recycler_view))
                .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(prefTitleId)),
                        click()));
    }

    private void openDrawer() {
        onView(withId(R.id.drawer))
                .perform(DrawerActions.open());

        onView(withId(R.id.drawer)).check(matches(isOpen()));
    }

    private void performClickDrawerItem(int itemId) {
        onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(itemId));
    }
}
