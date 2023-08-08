package androidTestFiles.utils.parent;

import android.content.Context;
import android.text.TextUtils;

import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.application.App;
import org.mockito.Mock;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import androidTestFiles.utils.FileUtils;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public abstract class MockedApiEndpointTest extends DaggerInjectMockUITest {

    public static final String ERROR_MESSAGE_BODY = BaseTest.PATH_RESPONSES + "/response_body_error_message.txt";
    protected static final String EMPTY_JSON = "{}";

    @Mock
    protected ApiEndpoint apiEndpoint;

    private MockWebServer mockServer;

    protected void startServer(int responseCode, String responseAsset) {
        startServer(responseCode, responseAsset, 0);
    }

    protected void startServer(int responseCode, String responseAsset, int timeoutDelay) {
        startServer(responseCode, responseAsset, timeoutDelay, 1);
    }

    protected void startServer(int responseCode, String responseAsset, int timeoutDelay, int orderWhenManyRequests) {

        try {
            mockServer = new MockWebServer();
            MockResponse response = createResponse(responseCode, responseAsset, timeoutDelay);
            if (orderWhenManyRequests > 1) {
                for (int i = 1; i < orderWhenManyRequests; i++) {
                    MockResponse responseUnused = new MockResponse();
                    responseUnused.setResponseCode(200);
                    mockServer.enqueue(responseUnused);
                }
            }
            mockServer.enqueue(response);
            mockServer.start();

        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        when(apiEndpoint.getFullURL((Context) any(), anyString())).thenReturn(mockServer.url("").toString());

    }

    protected MockResponse createResponse(int responseCode, String responseAsset, int timeoutDelay) throws IOException {
        MockResponse response = new MockResponse();
        response.setResponseCode(responseCode);

        if (!TextUtils.isEmpty(responseAsset)) {
            String responseBody = FileUtils.getStringFromFile(
                    InstrumentationRegistry.getInstrumentation().getContext(), /*BaseTest.PATH_TESTS + File.separator + */responseAsset);
            response.setBody(responseBody);

        } else {
            response.setBody(EMPTY_JSON);
        }

        if (timeoutDelay > 0) {
            response.setBodyDelay(timeoutDelay, TimeUnit.MILLISECONDS);
        }
        return response;
    }

    protected void enqueueResponse(int responseCode, String responseAsset, int timeoutDelay){
        try {
            MockResponse response = createResponse(responseCode, responseAsset, timeoutDelay);
            mockServer.enqueue(response);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public App getApp() {
        return (App) InstrumentationRegistry.getInstrumentation()
                .getTargetContext().getApplicationContext();
    }
}
