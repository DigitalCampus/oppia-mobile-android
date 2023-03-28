package androidTestFiles.features.exportActivity;

import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.containsString;
import static androidTestFiles.utils.UITestActionsUtils.waitForView;

import android.Manifest;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.MainActivity;
import org.digitalcampus.oppia.utils.storage.Storage;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import androidTestFiles.database.BaseTestDB;

@RunWith(AndroidJUnit4.class)
public class ExportFullActivityUITest extends BaseTestDB {
    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    private static final int NUM_TRACKER_TEST = 3;

    @After
    public void cleanActivityFiles() throws Exception {

        cleanDir(Storage.getActivityPath(getContext()));
        cleanDir(Storage.getActivityFullExportPath(getContext()));

    }

    private void cleanDir(String path) {
        File activityPath = new File(path);
        if (activityPath.exists() && activityPath.isDirectory()) {
            for (File file : activityPath.listFiles()) {
                if (file.isFile()) {
                    file.delete();
                }
            }
        }
    }

    @Test
    public void checkExportActivityCompleteUIFlow() throws Exception {

        getTestDataManager().addTrackers(NUM_TRACKER_TEST);

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            openDrawer();
            performClickDrawerItem(R.id.menu_settings);
            clickPrefWithText(R.string.prefAdvanced_title);
            clickPrefWithText(R.string.pref_export_full_activity_title);
            waitForView(withText(containsString(getContext().getString(R.string.full_activity_exported_success))))
                    .inRoot(isDialog())
                    .check(matches(isDisplayed()));

        }
    }

    @Test
    public void showNoFilesToExportMessageWhenThereIsNoActivity() throws Exception {

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            openDrawer();
            performClickDrawerItem(R.id.menu_settings);
            clickPrefWithText(R.string.prefAdvanced_title);
            clickPrefWithText(R.string.pref_export_full_activity_title);
            waitForView(withText(R.string.export_task_no_activities))
                    .inRoot(isDialog())
                    .check(matches(isDisplayed()));

        }
    }

}
