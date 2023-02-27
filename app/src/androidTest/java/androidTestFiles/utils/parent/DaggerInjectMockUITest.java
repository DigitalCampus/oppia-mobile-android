package androidTestFiles.utils.parent;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.DrawerMatchers.isClosed;
import static androidx.test.espresso.contrib.DrawerMatchers.isOpen;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertTrue;
import static androidTestFiles.utils.CourseUtils.runInstallCourseTask;

import android.Manifest;
import android.content.Context;
import android.os.Build;

import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.application.App;
import org.digitalcampus.oppia.di.AppComponent;
import org.digitalcampus.oppia.di.AppModule;
import org.digitalcampus.oppia.task.result.BasicResult;
import org.junit.Rule;

import java.util.concurrent.TimeUnit;

import androidTestFiles.utils.FileUtils;
import it.cosenonjaviste.daggermock.DaggerMockRule;

public class DaggerInjectMockUITest {

    @Rule
    public GrantPermissionRule grantWriteStoragePermissionsRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    @Rule
    public GrantPermissionRule grantNotificationsPermissionsRule =
            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) ?
                    GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS) :
                    GrantPermissionRule.grant();

    @Rule
    public DaggerMockRule<AppComponent> daggerRule =
            new DaggerMockRule<>(AppComponent.class, new AppModule((App) InstrumentationRegistry.getInstrumentation()
                    .getTargetContext()
                    .getApplicationContext())).set(
                    component -> {
                        App app =
                                (App) InstrumentationRegistry.getInstrumentation()
                                        .getTargetContext()
                                        .getApplicationContext();
                        app.setComponent(component);
                    });

    public void openDrawer() {
        onView(withId(R.id.drawer))
                .perform(DrawerActions.open());

        await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> onView(ViewMatchers.withId(R.id.drawer))
                        .check(matches(isCompletelyDisplayed())));

        onView(withId(R.id.drawer)).check(matches(isOpen()));
    }

    public void clickPrefWithText(int prefTitleId) {
        onView(withId(androidx.preference.R.id.recycler_view))
                .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(prefTitleId)),
                        click()));
    }

    public void performClickDrawerItem(int itemId) {
        onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(itemId));
    }


    protected void copyCourseFromAssets(Context context, String path, String filename) {
        FileUtils.copyZipFromAssetsPath(context, path, filename);  //Copy course zip from assets to download path
    }


    protected void installCourse(String path, String filename) {

        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        copyCourseFromAssets(context, path, filename);

        BasicResult response = runInstallCourseTask(context);
        assertTrue(response.isSuccess());
    }
}
