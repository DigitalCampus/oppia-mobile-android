package org.digitalcampus.oppia.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;

import org.apache.http.message.BasicHeader;
import org.apache.http.params.CoreProtocolPNames;
import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.MobileLearning;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HTTPClientUtils{

    public static final String HEADER_AUTH = "Authorization";
    public static final String HEADER_USER_AGENT = "User-Agent";

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

    public static String getFullURL(Context ctx, String apiPath){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getString(PrefsActivity.PREF_SERVER, ctx.getString(R.string.prefServerDefault)) + apiPath;
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
