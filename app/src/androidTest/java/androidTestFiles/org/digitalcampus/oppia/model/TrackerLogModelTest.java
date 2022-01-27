package androidTestFiles.org.digitalcampus.oppia.model;

import android.Manifest;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.digitalcampus.oppia.model.TrackerLog;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class TrackerLogModelTest {
    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    @Test
    public void getAndSetTest(){
        TrackerLog tl = new TrackerLog();
        tl.setId(123456);
        assertEquals(123456, tl.getId());
    }
}
