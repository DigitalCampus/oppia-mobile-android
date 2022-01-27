package androidTestFiles.org.digitalcampus.oppia.activity;

import android.Manifest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

@RunWith(AndroidJUnit4.class)
public class DeviceListActivityTest {
    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    @Test
    public void testActivityOpen() {

        // tests adapted to emulators without bluetooth
    }
}
