package androidTestFiles.Utils;

import android.content.Context;
import android.text.TextUtils;

import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.application.App;
import org.mockito.Mock;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import androidTestFiles.TestRules.DaggerInjectMockUITest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public abstract class MockedApiEndpointTest extends DaggerInjectMockUITest {

    public static final String ERROR_MESSAGE_BODY = "responses/response_body_error_message.txt";

    @Mock
    protected ApiEndpoint apiEndpoint;

    private MockWebServer mockServer;

    protected void startServer(int responseCode, String responseAsset, int timeoutDelay) {
        try {
            mockServer = new MockWebServer();
            MockResponse response = new MockResponse();
            response.setResponseCode(responseCode);

            if (!TextUtils.isEmpty(responseAsset)) {

                String responseBody = FileUtils.getStringFromFile(
                        InstrumentationRegistry.getInstrumentation().getContext(), responseAsset);

                if (responseBody != null) {
                    response.setBody(responseBody);
                }
            }

            if (timeoutDelay > 0) {
                response.setBodyDelay(timeoutDelay, TimeUnit.MILLISECONDS);

            }
            mockServer.enqueue(response);
            mockServer.start();

            when(apiEndpoint.getFullURL((Context) any(), anyString())).thenReturn(mockServer.url("").toString());

        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public App getApp() {
        return (App) InstrumentationRegistry.getInstrumentation()
                .getTargetContext().getApplicationContext();
    }
}
