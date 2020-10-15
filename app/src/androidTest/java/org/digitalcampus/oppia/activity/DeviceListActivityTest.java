package org.digitalcampus.oppia.activity;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class DeviceListActivityTest {

    @Rule
    public ActivityTestRule<DeviceListActivity> deviceListActivityTestRule =
            new ActivityTestRule<>(DeviceListActivity.class, false, false);

    @Test
    public void testActivityOpen() {
        // TODO - stub...
        //deviceListActivityTestRule.launchActivity(null);
    }
}
