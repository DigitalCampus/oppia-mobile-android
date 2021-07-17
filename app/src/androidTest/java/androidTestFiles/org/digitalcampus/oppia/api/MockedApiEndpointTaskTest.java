package androidTestFiles.org.digitalcampus.oppia.api;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.test.platform.app.InstrumentationRegistry;

import org.mockito.Mockito;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

public class MockedApiEndpointTaskTest {

    protected MockWebServer mockServer;

    protected void startServer(int responseCode, String responseBody, int timeoutDelay){
        try {
            mockServer = new MockWebServer();
            MockResponse response = new MockResponse();
            response.setResponseCode(responseCode);
            if (responseBody!=null) { response.setBody(responseBody); }
            if (timeoutDelay > 0){
                response.setBodyDelay(timeoutDelay, TimeUnit.MILLISECONDS);

            }
            mockServer.enqueue(response);
            mockServer.start();

        }catch(IOException ioe) {
            ioe.printStackTrace();
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    protected void startServer(int responseCode, String responseBody){
        enableConnectivity(true);
        startServer(responseCode, responseBody, 0);
    }

    protected void enableConnectivity(boolean enable) {
        String command = enable ? "enable" : "disable";
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand("svc wifi " + command);
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand("svc data " + command);
    }

    protected ConnectivityManager getAvailableConnectivityManager(){
        final ConnectivityManager connectivityManager = Mockito.mock( ConnectivityManager.class );
        final NetworkInfo networkInfo = Mockito.mock(NetworkInfo.class);

        Mockito.when( networkInfo.isAvailable()).thenReturn(true);
        Mockito.when( networkInfo.isConnected()).thenReturn(true);

        Mockito.when( connectivityManager.getActiveNetworkInfo()).thenReturn( networkInfo );

        return connectivityManager;
    }

}
