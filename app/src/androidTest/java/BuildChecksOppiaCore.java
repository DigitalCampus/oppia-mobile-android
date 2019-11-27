

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.mobile.learning.BuildConfig;
import org.digitalcampus.mobile.learning.R;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class BuildChecksOppiaCore {

    private Context context;
    private SharedPreferences prefs;

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Test
    public void checkDefaultSettingsParameters() {


        String oppiaServerDefault = prefs.getString("prefServer", null);
        String oppiaServerHost = context.getString(R.string.oppiaServerHost);

        assertEquals(oppiaServerDefault, "https://demo.oppia-mobile.org/");
        assertEquals(oppiaServerHost, "demo.oppia-mobile.org");

        assertEquals(BuildConfig.ADMIN_PROTECT_SETTINGS, false);
        assertEquals(BuildConfig.ADMIN_PROTECT_ACTIVITY_SYNC, false);
        assertEquals(BuildConfig.ADMIN_PROTECT_ACTIVITY_EXPORT, false);
        assertEquals(BuildConfig.ADMIN_PROTECT_COURSE_DELETE, false);
        assertEquals(BuildConfig.ADMIN_PROTECT_COURSE_RESET, false);
        assertEquals(BuildConfig.ADMIN_PROTECT_COURSE_INSTALL, false);
        assertEquals(BuildConfig.ADMIN_PROTECT_COURSE_UPDATE, false);


        assertEquals(BuildConfig.MENU_ALLOW_MONITOR, true);
        assertEquals(BuildConfig.MENU_ALLOW_SETTINGS, true);
        assertEquals(BuildConfig.MENU_ALLOW_COURSE_DOWNLOAD, true);
        assertEquals(BuildConfig.MENU_ALLOW_SYNC, true);
        assertEquals(BuildConfig.MENU_ALLOW_LOGOUT, true);
        assertEquals(BuildConfig.MENU_ALLOW_LANGUAGE, true);
        assertEquals(BuildConfig.DOWNLOAD_COURSES_DISPLAY, 1);

        assertEquals(BuildConfig.START_COURSEINDEX_COLLAPSED, false);

        assertEquals(BuildConfig.METADATA_INCLUDE_NETWORK, true);
        assertEquals(BuildConfig.METADATA_INCLUDE_DEVICE_ID, true);
        assertEquals(BuildConfig.METADATA_INCLUDE_SIM_SERIAL, true);
        assertEquals(BuildConfig.METADATA_INCLUDE_WIFI_ON, true);
        assertEquals(BuildConfig.METADATA_INCLUDE_NETWORK_CONNECTED, true);
        assertEquals(BuildConfig.METADATA_INCLUDE_BATTERY_LEVEL, true);
        assertEquals(BuildConfig.METADATA_INCLUDE_GPS, true);

        assertEquals(BuildConfig.OFFLINE_REGISTER_ENABLED, true);
        assertEquals(BuildConfig.SESSION_EXPIRATION_ENABLED, false);
        assertEquals(BuildConfig.SESSION_EXPIRATION_TIMEOUT, 600);

        assertEquals(BuildConfig.SHOW_COURSE_DESCRIPTION, false);


        assertEquals(BuildConfig.GAMIFICATION_MEDIA_CRITERIA, "threshold");

        assertEquals(BuildConfig.GAMIFICATION_DEFAULT_MEDIA_THRESHOLD, 80);


        assertEquals(BuildConfig.GAMIFICATION_POINTS_ANIMATION, "3");
        assertEquals(BuildConfig.DURATION_GAMIFICATION_POINTS_VIEW, 2);


    }
}
