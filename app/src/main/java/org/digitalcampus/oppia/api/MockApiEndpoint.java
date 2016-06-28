package org.digitalcampus.oppia.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;

import okhttp3.mockwebserver.MockWebServer;

/**
 * Created by Alberto on 28/06/2016.
 */
public class MockApiEndpoint implements ApiEndpoint {
    private MockWebServer mockServer;

    public MockApiEndpoint(MockWebServer mockServer){
        this.mockServer = mockServer;
    }

    @Override
    public String getFullURL(Context ctx, String apiPath) {
        return mockServer.url(apiPath).toString();
    }
}
