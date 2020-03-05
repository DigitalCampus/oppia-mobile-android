import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

import org.digitalcampus.mobile.learning.BuildConfig;

import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.utils.MetaDataUtils;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;

@RunWith(AndroidJUnit4.class)
public class MetadataUtilsTest {

    private Context context;
    private SharedPreferences prefs;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().clear().commit();
    }


    private void assertCorrectValues(JSONObject eventData, Map<String, Boolean> expectedValues){
        for (String key : expectedValues.keySet()){
            assertEquals(eventData.has(key), (boolean) expectedValues.get(key));
        }
    }

    @Test
    public void metadataUtils_defaultValues() throws Exception{

        JSONObject eventData = new MetaDataUtils(context).getMetaData();

        Map<String, Boolean> expectedValues = new HashMap<String, Boolean>() {{
            put("network", BuildConfig.METADATA_INCLUDE_NETWORK);
            put("battery", BuildConfig.METADATA_INCLUDE_BATTERY_LEVEL);
            put("deviceid", BuildConfig.METADATA_INCLUDE_DEVICE_ID);
            put("gps", BuildConfig.METADATA_INCLUDE_GPS);
            put("netconnected", BuildConfig.METADATA_INCLUDE_NETWORK_CONNECTED);
            put("simserial", BuildConfig.METADATA_INCLUDE_SIM_SERIAL);
            put("wifion", BuildConfig.METADATA_INCLUDE_WIFI_ON);
        }};

        assertCorrectValues(eventData, expectedValues);
    }

    @Test
    public void metadataUtils_nullJSON() throws Exception{
        JSONObject eventData = new MetaDataUtils(context).getMetaData(null);

        Map<String, Boolean> expectedValues = new HashMap<String, Boolean>() {{
            put("network", BuildConfig.METADATA_INCLUDE_NETWORK);
            put("battery", BuildConfig.METADATA_INCLUDE_BATTERY_LEVEL);
            put("deviceid", BuildConfig.METADATA_INCLUDE_DEVICE_ID);
            put("gps", BuildConfig.METADATA_INCLUDE_GPS);
            put("netconnected", BuildConfig.METADATA_INCLUDE_NETWORK_CONNECTED);
            put("simserial", BuildConfig.METADATA_INCLUDE_SIM_SERIAL);
            put("wifion", BuildConfig.METADATA_INCLUDE_WIFI_ON);
        }};

        assertCorrectValues(eventData, expectedValues);

    }

    @Test
    public void metadataUtils_overridesJSON() throws Exception{
        JSONObject eventData = new JSONObject();
        eventData.put("network", "initial_value");

        MetaDataUtils mdu = new MetaDataUtils(context);
        prefs.edit().putBoolean(mdu.getMetadataPref(PrefsActivity.PREF_METADATA_NETWORK), true).commit();
        eventData = mdu.getMetaData(eventData);

        Map<String, Boolean> expectedValues = new HashMap<String, Boolean>() {{
            put("network", true);
        }};

        assertCorrectValues(eventData, expectedValues);
        assertFalse("initial_value".equals(eventData.get("network")));
    }

    @Test
    public void metadataUtils_fromPreferences() throws Exception{

        MetaDataUtils mdu = new MetaDataUtils(context);
        prefs.edit()
                .putBoolean(mdu.getMetadataPref(PrefsActivity.PREF_METADATA_NETWORK), true)
                .putBoolean(mdu.getMetadataPref(PrefsActivity.PREF_METADATA_DEVICE_ID), true)
                .putBoolean(mdu.getMetadataPref(PrefsActivity.PREF_METADATA_WIFI_ON), false)
                .putBoolean(mdu.getMetadataPref(PrefsActivity.PREF_METADATA_GPS), false)
                .commit();

        JSONObject eventData = mdu.getMetaData();

        Map<String, Boolean> expectedValues = new HashMap<String, Boolean>() {{
            put("network", true);
            put("deviceid", true);
            put("gps", false);
            put("wifion", false);
        }};

        assertCorrectValues(eventData, expectedValues);
    }
}
