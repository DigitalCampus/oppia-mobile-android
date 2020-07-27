/*
 * This file is part of OppiaMobile - https://digital-campus.org/
 *
 * OppiaMobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OppiaMobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OppiaMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package org.digitalcampus.oppia.utils;

import android.content.Context;
import android.content.SharedPreferences;

import org.digitalcampus.mobile.learning.BuildConfig;
import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import androidx.preference.PreferenceManager;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class HTTPClientUtils {

    public static final String TAG = HTTPClientUtils.class.getSimpleName();
    public static final String HEADER_AUTH = "Authorization";
    public static final String HEADER_USER_AGENT = "User-Agent";
    public static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
    public static final String USER_AGENT = "OppiaMobile Android: ";

    private static OkHttpClient client;

    private HTTPClientUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static OkHttpClient getClient(Context ctx) {
        if (client == null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
            int timeoutConn = Integer.parseInt(prefs.getString(PrefsActivity.PREF_SERVER_TIMEOUT_CONN,
                    ctx.getString(R.string.prefServerTimeoutConnectionDefault)));

            client = new OkHttpClient.Builder()
                    .addInterceptor(new UserAgentInterceptor())
                    .addInterceptor(getLoggingInterceptor())
                    .connectTimeout(timeoutConn, TimeUnit.MILLISECONDS)
                    .readTimeout(timeoutConn, TimeUnit.MILLISECONDS)
                    .hostnameVerifier((hostname, session) -> hostname.equalsIgnoreCase(session.getPeerHost()))
                    .build();

        }

        return client;
    }

    private static Interceptor getLoggingInterceptor() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        return logging;
    }

    public static String getAuthHeaderValue(String username, String apiKey) {
        return "ApiKey " + username + ":" + apiKey;
    }

    public static HttpUrl getUrlWithCredentials(String url, String username, String apiKey) {
        return HttpUrl.parse(url).newBuilder()
                .addQueryParameter("username", username)
                .addQueryParameter("api_key", apiKey)
                .addQueryParameter("format", "json")
                .build();
    }

    public static class UserAgentInterceptor implements Interceptor {

        public UserAgentInterceptor() {
            // required constructor
        }

        @Override
        public Response intercept(Chain chain) throws IOException {

            String v = BuildConfig.VERSION_NAME;
            Request originalRequest = chain.request();
            Request requestWithUserAgent = originalRequest.newBuilder()
                    .header(HEADER_USER_AGENT, USER_AGENT + v)
                    .build();
            return chain.proceed(requestWithUserAgent);
        }
    }

}
