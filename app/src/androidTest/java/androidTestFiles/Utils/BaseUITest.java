package androidTestFiles.Utils;

import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.mobile.learning.R;
import org.junit.Before;

import static org.mockito.Mockito.when;

public class BaseUITest extends BaseTest {

    protected boolean isTabletLand;

    @Before
    public void setDeviceType() throws Exception {
        isTabletLand = InstrumentationRegistry.getInstrumentation().getTargetContext().getResources().getBoolean(R.bool.is_tablet_land);
    }
}
