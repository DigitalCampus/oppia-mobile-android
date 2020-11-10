

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.mobile.learning.BuildConfig;
import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.application.App;
import org.junit.Before;
import org.junit.Ignore;
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
        //We load the default prefs in case they were not loaded
        App.loadDefaultPreferenceValues(context, false);
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Test
    public void checkDefaultSettingsParameters() {

        String oppiaServerDefault = prefs.getString("prefServer", context.getString(R.string.prefServerDefault));
        String oppiaServerHost = context.getString(R.string.oppiaServerHost);

        assertEquals("https://demo.oppia-mobile.org/", oppiaServerDefault);
        assertEquals("demo.oppia-mobile.org", oppiaServerHost);

        assertEquals(false, BuildConfig.ADMIN_PROTECT_SETTINGS);
        assertEquals(false, BuildConfig.ADMIN_PROTECT_ACTIVITY_SYNC);
        assertEquals(false, BuildConfig.ADMIN_PROTECT_ACTIVITY_EXPORT);
        assertEquals(false, BuildConfig.ADMIN_PROTECT_COURSE_DELETE);
        assertEquals(false, BuildConfig.ADMIN_PROTECT_COURSE_RESET);
        assertEquals(false, BuildConfig.ADMIN_PROTECT_COURSE_INSTALL);
        assertEquals(false, BuildConfig.ADMIN_PROTECT_COURSE_UPDATE);
        assertEquals("", BuildConfig.ADMIN_PROTECT_INITIAL_PASSWORD);
        assertEquals(68, BuildConfig.ADMIN_PASSWORD_OVERRIDE_VERSION);


        assertEquals(true, BuildConfig.MENU_ALLOW_MONITOR);
        assertEquals(true, BuildConfig.MENU_ALLOW_SETTINGS);
        assertEquals(true, BuildConfig.MENU_ALLOW_COURSE_DOWNLOAD);
        assertEquals(true, BuildConfig.MENU_ALLOW_SYNC);
        assertEquals(true, BuildConfig.MENU_ALLOW_LOGOUT);
        assertEquals(true, BuildConfig.MENU_ALLOW_LANGUAGE);
        assertEquals(1, BuildConfig.DOWNLOAD_COURSES_DISPLAY);

        assertEquals(false, BuildConfig.START_COURSEINDEX_COLLAPSED);

        assertEquals(true, BuildConfig.METADATA_INCLUDE_NETWORK);
        assertEquals(true, BuildConfig.METADATA_INCLUDE_DEVICE_ID);
        assertEquals(true, BuildConfig.METADATA_INCLUDE_SIM_SERIAL);
        assertEquals(true, BuildConfig.METADATA_INCLUDE_WIFI_ON);
        assertEquals(true, BuildConfig.METADATA_INCLUDE_NETWORK_CONNECTED);
        assertEquals(true, BuildConfig.METADATA_INCLUDE_BATTERY_LEVEL);
        assertEquals(true, BuildConfig.METADATA_INCLUDE_GPS);

        assertEquals(true, BuildConfig.OFFLINE_REGISTER_ENABLED);
        assertEquals(false, BuildConfig.SESSION_EXPIRATION_ENABLED);
        assertEquals(600, BuildConfig.SESSION_EXPIRATION_TIMEOUT);

        assertEquals(false, BuildConfig.SHOW_COURSE_DESCRIPTION);


        assertEquals("threshold", BuildConfig.GAMIFICATION_MEDIA_CRITERIA);

        assertEquals(80, BuildConfig.GAMIFICATION_DEFAULT_MEDIA_THRESHOLD);


        assertEquals("3", BuildConfig.GAMIFICATION_POINTS_ANIMATION);
        assertEquals(2, BuildConfig.DURATION_GAMIFICATION_POINTS_VIEW);


    }
}
