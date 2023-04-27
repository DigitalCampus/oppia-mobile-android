package androidTestFiles.utils;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static androidTestFiles.utils.parent.BaseTest.PATH_COURSES_MEDIA_TESTS;
import static androidTestFiles.utils.parent.BaseTest.PATH_COURSES_TESTS;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import android.content.pm.ResolveInfo;
import org.digitalcampus.oppia.utils.resources.ExternalResourceOpener;
import org.digitalcampus.oppia.utils.storage.Storage;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import androidTestFiles.features.courseMedia.CourseMediaBaseTest;
import androidTestFiles.utils.parent.BaseTest;
import androidTestFiles.utils.parent.DaggerInjectMockUITest;

@RunWith(AndroidJUnit4.class)
public class ExternalResourceOpenerTests extends CourseMediaBaseTest {

    @Test
    public void getIntentFor_nonExistingResource() throws Exception {

        File mediaPath = new File(Storage.getMediaPath(context));
        File media = new File(mediaPath, "nonexisting_media.mp4");

        Intent intent = ExternalResourceOpener.getIntentToOpenResource(context, media);
        assertNull(intent);
    }

    @Test
    public void getIntentForShare_nonExistingResource() throws Exception {
        File mediaPath = new File(Storage.getMediaPath(context));
        File media = new File(mediaPath, "nonexisting_media.mp4");

        Intent intent = ExternalResourceOpener.constructShareFileIntent(context, media, "image/png");
        assertNull(intent);
    }

    @Test
    public void getIntentForShare_existingResource() throws Exception {
        File mediaPath = new File(Storage.getMediaPath(context));
        copyMediaFromAssets(BaseTest.MEDIA_FILE_VIDEO_TEST_1);
        File media = new File(mediaPath, BaseTest.MEDIA_FILE_VIDEO_TEST_1);

        Intent intent = ExternalResourceOpener.constructShareFileIntent(context, media, "video/mp4");
        assertNotNull(intent);
    }
}
