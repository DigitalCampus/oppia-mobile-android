package androidTestFiles.org.digitalcampus.oppia.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.DownloadMediaActivity;
import org.digitalcampus.oppia.activity.ScorecardActivity;
import org.digitalcampus.oppia.model.Media;
import org.digitalcampus.oppia.service.DownloadService;
import org.digitalcampus.oppia.service.DownloadServiceDelegate;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;

import androidTestFiles.TestRules.DaggerInjectMockUITest;
import androidx.test.core.app.ActivityScenario;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import static androidTestFiles.Matchers.RecyclerViewMatcher.withRecyclerView;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;

public class DownloadMediaActivityUITest extends DaggerInjectMockUITest {
    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    @Mock DownloadServiceDelegate downloadServiceDelegate;

    private Intent getIntentParams(int numFiles){

        ArrayList<Media> mediaFiles = new ArrayList<>();
        for (int i=0; i<numFiles; i++){
            Media m = new Media();
            m.setFilename("Media_test_" + i + ".mp4");
            m.setFileSize(2000);
            m.setDownloadUrl("http://fakeurl.test/media_" + i + ".mp4");
            mediaFiles.add(m);
        }

        Intent i = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), DownloadMediaActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(DownloadMediaActivity.MISSING_MEDIA, mediaFiles);
        i.putExtras(bundle);
        return i;
    }

    private void sendBroadcast(Context ctx, String action, String url){
        Intent intent = new Intent(DownloadService.BROADCAST_ACTION);
        intent.putExtra(DownloadService.SERVICE_ACTION, action);
        intent.putExtra(DownloadService.SERVICE_URL, url);
        intent.putExtra(DownloadService.SERVICE_MESSAGE, "1");
        ctx.sendOrderedBroadcast(intent, null);
    }

    @Test
    public void showEmptyStateWhenEmptyList() throws Exception {

        try (ActivityScenario<ScorecardActivity> scenario = ActivityScenario.launch(getIntentParams(0))) {
            onView(withId(R.id.empty_state)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void showErrorBadgeWhenDownloadFails() throws Exception {

        doAnswer(invocationOnMock -> {
            Context ctx = (Context) invocationOnMock.getArguments()[0];
            Media m = (Media) invocationOnMock.getArguments()[1];
            sendBroadcast(ctx, DownloadService.ACTION_FAILED, m.getDownloadUrl());
            return null;
        }).when(downloadServiceDelegate).startDownload(any(), any());

        try (ActivityScenario<ScorecardActivity> scenario = ActivityScenario.launch(getIntentParams(4))) {

            onView(withRecyclerView(R.id.missing_media_list)
                    .atPositionOnView(0, R.id.action_btn))
                    .perform(click());

            onView(withRecyclerView(R.id.missing_media_list)
                    .atPositionOnView(0, R.id.download_error))
                    .check(matches(isDisplayed()));
        }
    }
}
