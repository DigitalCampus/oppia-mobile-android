package androidTestFiles.org.digitalcampus.oppia.model;

import android.Manifest;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.digitalcampus.oppia.model.DownloadProgress;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class DownloadProgressModelTest {
    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    @Test
    public void getAndSetTest(){
        DownloadProgress dp = new DownloadProgress();
        dp.setMessage("my msg");
        assertEquals("my msg", dp.getMessage());
    }
}
