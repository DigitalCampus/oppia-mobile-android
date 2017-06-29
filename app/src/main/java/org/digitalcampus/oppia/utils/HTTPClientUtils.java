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
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.MobileLearning;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HTTPClientUtils{

    public static final String HEADER_AUTH = "Authorization";
    public static final String HEADER_USER_AGENT = "User-Agent";
    public static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

    private static OkHttpClient client;

    public static OkHttpClient getClient(Context ctx) {
        if (client == null){
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
            int timeoutConn = Integer.parseInt(prefs.getString(PrefsActivity.PREF_SERVER_TIMEOUT_CONN,
                            ctx.getString(R.string.prefServerTimeoutConnectionDefault)));

            client = new OkHttpClient.Builder()
                    .addInterceptor(new UserAgentInterceptor(ctx))
                    .connectTimeout(timeoutConn, TimeUnit.MILLISECONDS)
                    .readTimeout(timeoutConn, TimeUnit.MILLISECONDS)
                    .build();
        }

        return client;
    }

    public static String getAuthHeaderValue(String username, String apiKey){
        return "ApiKey " + username + ":" + apiKey;
    }
    
    public static HttpUrl getUrlWithCredentials(String url, String username, String apiKey){
        return HttpUrl.parse(url).newBuilder()
                .addQueryParameter("username", username)
                .addQueryParameter("api_key", apiKey)
                .addQueryParameter("format", "json")
                .build();
    }

    public static class UserAgentInterceptor implements Interceptor {

        private Context ctx;

        public UserAgentInterceptor(Context ctx){
            this.ctx = ctx;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {

            String v = "0";
            try {
                v = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            Request originalRequest = chain.request();
            Request requestWithUserAgent = originalRequest.newBuilder()
                    .header(HEADER_USER_AGENT, MobileLearning.USER_AGENT + v)
                    .build();
            return chain.proceed(requestWithUserAgent);
        }
    }

}
