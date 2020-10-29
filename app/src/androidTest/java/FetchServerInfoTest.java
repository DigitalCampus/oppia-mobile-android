import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import androidx.preference.PreferenceManager;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.api.MockApiEndpoint;
import org.digitalcampus.oppia.api.MockedApiEndpointTaskTest;
import org.digitalcampus.oppia.task.FetchServerInfoTask;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;

@RunWith(AndroidJUnit4.class)
public class FetchServerInfoTest extends MockedApiEndpointTaskTest {

    private static final String VALID_SERVERINFO_RESPONSE = "responses/response_200_serverinfo.json";

    private Context context;
    private SharedPreferences prefs;

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Before
    public void before() throws Exception {
        prefs.edit().putBoolean(PrefsActivity.PREF_SERVER_CHECKED, false).apply();
    }

    @After
    public void tearDown() throws Exception {
        if (mockServer!=null)
        mockServer.shutdown();
    }

    private void fetchServerInfoSync(Context context, ConnectivityManager manager){
        final CountDownLatch signal = new CountDownLatch(1);  //Control AsyncTask sincronization for testing

        FetchServerInfoTask task = new FetchServerInfoTask(context, new MockApiEndpoint(mockServer), manager);
        task.setListener(new FetchServerInfoTask.FetchServerInfoListener() {
            @Override
            public void onError(String message) {
                signal.countDown();
            }

            @Override
            public void onValidServer(String version, String name) {
                signal.countDown();
            }

            @Override
            public void onUnchecked() {
                signal.countDown();
            }
        });

        task.execute();

        try {
            signal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void fetchServerInfo_validServer() throws Exception{

        startServer(200, Utils.FileUtils.getStringFromFile(
                InstrumentationRegistry.getInstrumentation().getContext(), VALID_SERVERINFO_RESPONSE));
        fetchServerInfoSync(context, getAvailableConnectivityManager());

        assertTrue(prefs.getBoolean(PrefsActivity.PREF_SERVER_CHECKED, false));
        assertTrue(prefs.getBoolean(PrefsActivity.PREF_SERVER_VALID, false));
        assertEquals(prefs.getString(PrefsActivity.PREF_SERVER_NAME, ""), "test_name");
        assertEquals(prefs.getString(PrefsActivity.PREF_SERVER_VERSION, ""), "test_version");
    }

    @Test
    public void fetchServerInfo_malformedJSON() {
        startServer(200, "{\"name\":wrongjson }");
        fetchServerInfoSync(context, getAvailableConnectivityManager());

        assertTrue(prefs.getBoolean(PrefsActivity.PREF_SERVER_CHECKED, false));
        assertFalse(prefs.getBoolean(PrefsActivity.PREF_SERVER_VALID, false));
    }

    @Test
    public void fetchServerInfo_emptyResponse() {
        startServer(200, "");
        fetchServerInfoSync(context, getAvailableConnectivityManager());

        assertTrue(prefs.getBoolean(PrefsActivity.PREF_SERVER_CHECKED, false));
        assertFalse(prefs.getBoolean(PrefsActivity.PREF_SERVER_VALID, false));
    }

    @Test
    public void fetchServerInfo_noConnection(){
        final ConnectivityManager connectivityManager = Mockito.mock( ConnectivityManager.class );
        Mockito.when( connectivityManager.getActiveNetworkInfo()).thenReturn( null );

        Context ctx = Mockito.mock(Context.class);
        Mockito.when (ctx.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(connectivityManager);

        fetchServerInfoSync(ctx, null);

        assertFalse(prefs.getBoolean(PrefsActivity.PREF_SERVER_CHECKED, false));
    }

    @Test
    public void fetchServerInfo_notfound(){
        startServer(404, null);
        fetchServerInfoSync(context, getAvailableConnectivityManager());

        assertTrue(prefs.getBoolean(PrefsActivity.PREF_SERVER_CHECKED, false));
        assertFalse(prefs.getBoolean(PrefsActivity.PREF_SERVER_VALID, false));
    }

    @Test
    public void fetchServerInfo_connectionTimeout() throws Exception {

        int timeoutConn = Integer.parseInt(prefs.getString(PrefsActivity.PREF_SERVER_TIMEOUT_CONN,
                context.getString(R.string.prefServerTimeoutConnectionDefault)));
        startServer(200, Utils.FileUtils.getStringFromFile(InstrumentationRegistry.getInstrumentation().getContext(), VALID_SERVERINFO_RESPONSE), timeoutConn+100);
        fetchServerInfoSync(context, getAvailableConnectivityManager());

        assertTrue(prefs.getBoolean(PrefsActivity.PREF_SERVER_CHECKED, false));
        assertFalse(prefs.getBoolean(PrefsActivity.PREF_SERVER_VALID, false));
    }
}
