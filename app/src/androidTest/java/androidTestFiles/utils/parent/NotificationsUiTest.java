package androidTestFiles.utils.parent;

import android.content.Intent;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;

public class NotificationsUiTest extends DaggerInjectMockUITest {



    @After
    public void tearDown() throws Exception {
        Intent closeIntent = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        InstrumentationRegistry.getInstrumentation().getTargetContext().sendBroadcast(closeIntent);
    }
}
