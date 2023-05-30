package androidTestFiles.features.storage;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static androidTestFiles.utils.UITestActionsUtils.waitForView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.MainActivity;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.utils.storage.StorageAccessStrategy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import androidTestFiles.utils.parent.DaggerInjectMockUITest;

@RunWith(AndroidJUnit4.class)
public class SdCardAvailableTests extends DaggerInjectMockUITest {

    @Mock
    StorageAccessStrategy storageStrategy;

    @Test
    public void showBlockingMessageIfExternalSelectedAndNotSdCardAvailable() throws Exception {

        when(storageStrategy.getStorageType()).thenReturn(PrefsActivity.STORAGE_OPTION_EXTERNAL);
        when(storageStrategy.isStorageAvailable(any())).thenReturn(false);

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {

            waitForView(withText(R.string.sdcard_not_available_message))
                    .inRoot(isDialog())
                    .check(matches(isDisplayed()));

        }
    }

    @Test
    public void dontShowBlockingMessageIfExternalSelectedAndSdCardAvailable() throws Exception {

        when(storageStrategy.getStorageType()).thenReturn(PrefsActivity.STORAGE_OPTION_EXTERNAL);
        when(storageStrategy.isStorageAvailable(any())).thenReturn(true);

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {

            onView(withText(R.string.sdcard_not_available_message))
                    .check(doesNotExist());
        }
    }

    @Test
    public void dontShowBlockingMessageIfInternalStorageAndNotSdCardAvailable() throws Exception {

        when(storageStrategy.getStorageType()).thenReturn(PrefsActivity.STORAGE_OPTION_INTERNAL);
        when(storageStrategy.isStorageAvailable(any())).thenReturn(false);

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {

            onView(withText(R.string.sdcard_not_available_message))
                    .check(doesNotExist());
        }
    }
}
