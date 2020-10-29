package UI;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.digitalcampus.oppia.activity.DeviceListActivity;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class DeviceListUITest {

    @Rule
    public ActivityTestRule<DeviceListActivity> deviceListActivityTestRule =
            new ActivityTestRule<>(DeviceListActivity.class, false, false);

    @Test
    public void openActivityTest(){
        // TODO this is a stub
        // deviceListActivityTestRule.launchActivity(null);

    }
}
