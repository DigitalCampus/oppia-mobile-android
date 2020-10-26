package UI;

import android.content.Context;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.ActivityLogActivity;
import org.digitalcampus.oppia.application.App;
import org.digitalcampus.oppia.di.AppComponent;
import org.digitalcampus.oppia.di.AppModule;
import org.digitalcampus.oppia.model.ActivityLogRepository;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.io.File;
import java.util.ArrayList;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import it.cosenonjaviste.daggermock.DaggerMockRule;

import static Utils.RecyclerViewMatcher.withRecyclerView;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;

@RunWith(AndroidJUnit4.class)
public class ActivityLogUITest {

    @Rule public DaggerMockRule<AppComponent> daggerRule =
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

    @Rule
    public ActivityTestRule<ActivityLogActivity> activityLogActivityTestRule =
            new ActivityTestRule<>(ActivityLogActivity.class, false, false);


    @Mock ActivityLogRepository logsRepository;


    @Test
    public void showActivityLogListIfAny() throws Exception {

        doAnswer(invocationOnMock -> {
            ArrayList<File> files = new ArrayList<>();
            files.add(new File("username_202001251800.json"));
            return files;
        }).when(logsRepository).getExportedActivityLogs((Context) any());

        activityLogActivityTestRule.launchActivity(null);

        onView(withRecyclerView(R.id.exported_files_list)
                .atPositionOnView(0, R.id.file_name))
                .check(matches(withText(startsWith("username"))));

    }

}
