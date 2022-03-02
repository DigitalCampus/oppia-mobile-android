package androidTestFiles.org.digitalcampus.oppia.activity;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static androidTestFiles.Matchers.RecyclerViewMatcher.withRecyclerView;
import static androidTestFiles.Utils.CourseUtils.runInstallCourseTask;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.test.core.app.ActivityScenario;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.DownloadMediaActivity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.Media;
import org.digitalcampus.oppia.service.DownloadService;
import org.digitalcampus.oppia.service.DownloadServiceDelegate;
import org.digitalcampus.oppia.task.result.BasicResult;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;

import androidTestFiles.UI.CourseMediaBaseTest;
import androidTestFiles.Utils.Assertions.RecyclerViewItemCountAssertion;

public class DownloadMediaActivityUITest extends CourseMediaBaseTest {
    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    @Mock DownloadServiceDelegate downloadServiceDelegate;

    private void sendBroadcast(Context ctx, String action, String url){
        Intent intent = new Intent(DownloadService.BROADCAST_ACTION);
        intent.putExtra(DownloadService.SERVICE_ACTION, action);
        intent.putExtra(DownloadService.SERVICE_URL, url);
        intent.putExtra(DownloadService.SERVICE_MESSAGE, "1");
        ctx.sendOrderedBroadcast(intent, null);
    }

    @Test
    public void showEmptyStateWhenNoMediaMissing() throws Exception {

        copyCourseFromAssets(COURSE_WITH_MEDIA_1);
        copyCourseFromAssets(COURSE_WITH_MEDIA_2);

        BasicResult response = runInstallCourseTask(context);
        assertTrue(response.isSuccess());

        copyMediaFromAssets(MEDIA_FILE_VIDEO_TEST_1);
        copyMediaFromAssets(MEDIA_FILE_VIDEO_TEST_2);

        try (ActivityScenario<DownloadMediaActivity> scenario = ActivityScenario.launch(DownloadMediaActivity.class)) {
            onView(withId(R.id.empty_state)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void showMissingMediaForAllCourses() throws Exception {

        copyCourseFromAssets(COURSE_WITH_MEDIA_1);
        copyCourseFromAssets(COURSE_WITH_MEDIA_2);

        BasicResult response = runInstallCourseTask(context);
        assertTrue(response.isSuccess());

        try (ActivityScenario<DownloadMediaActivity> scenario = ActivityScenario.launch(DownloadMediaActivity.class)) {
            onView(withId(R.id.missing_media_list)).check(new RecyclerViewItemCountAssertion(2));
        }
    }

    @Test
    public void showMissingMediaForConcreteCourse() throws Exception {

        copyCourseFromAssets(COURSE_WITH_MEDIA_1);
        copyCourseFromAssets(COURSE_WITH_MEDIA_2);

        BasicResult response = runInstallCourseTask(context);
        assertTrue(response.isSuccess());

        Course course = new Course();
        course.setCourseId(1);
        Intent intent = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), DownloadMediaActivity.class);
        intent.putExtra(DownloadMediaActivity.MISSING_MEDIA_COURSE_FILTER, course);

        try (ActivityScenario<DownloadMediaActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.missing_media_list)).check(new RecyclerViewItemCountAssertion(1));
        }
    }

    @Test
    public void showErrorBadgeWhenDownloadFails() throws Exception {

        copyCourseFromAssets(COURSE_WITH_MEDIA_1);

        BasicResult response = runInstallCourseTask(context);
        assertTrue(response.isSuccess());

        doAnswer(invocationOnMock -> {
            Context ctx = (Context) invocationOnMock.getArguments()[0];
            Media m = (Media) invocationOnMock.getArguments()[1];
            sendBroadcast(ctx, DownloadService.ACTION_FAILED, m.getDownloadUrl());
            return null;
        }).when(downloadServiceDelegate).startDownload(any(), any());

        try (ActivityScenario<DownloadMediaActivity> scenario = ActivityScenario.launch(DownloadMediaActivity.class)) {

            onView(withRecyclerView(R.id.missing_media_list)
                    .atPositionOnView(0, R.id.action_btn))
                    .perform(click());

            onView(withRecyclerView(R.id.missing_media_list)
                    .atPositionOnView(0, R.id.download_error))
                    .check(matches(isDisplayed()));
        }
    }

}
