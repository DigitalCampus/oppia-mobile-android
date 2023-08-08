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
package org.digitalcampus.oppia.utils

import android.content.Context
import androidx.preference.PreferenceManager
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.digitalcampus.mobile.learning.BuildConfig
import org.digitalcampus.mobile.learning.R
import org.digitalcampus.oppia.activity.PrefsActivity
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLSession

object HTTPClientUtils {

    val TAG = HTTPClientUtils::class.simpleName
    const val HEADER_AUTH = "Authorization"
    const val HEADER_USER_AGENT = "User-Agent"
    @JvmField
    val MEDIA_TYPE_JSON: MediaType = "application/json; charset=utf-8".toMediaType()
    const val USER_AGENT = "OppiaMobile Android: "
    private var client: OkHttpClient? = null

    @JvmStatic
    fun getClient(ctx: Context): OkHttpClient? {
        if (client == null) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(ctx)
            val timeoutConn = prefs.getString(PrefsActivity.PREF_SERVER_TIMEOUT_CONN,
                    ctx.getString(R.string.prefServerTimeoutConnectionDefault))?.toIntOrNull() ?: 0
            client = OkHttpClient.Builder()
                    .addInterceptor(UserAgentInterceptor())
                    .addInterceptor(getLoggingInterceptor())
                    .connectTimeout(timeoutConn.toLong(), TimeUnit.MILLISECONDS)
                    .readTimeout(timeoutConn.toLong(), TimeUnit.MILLISECONDS)
                    .hostnameVerifier { hostname: String, session: SSLSession -> hostname.equals(session.peerHost, ignoreCase = true) }
                    .build()
        }
        return client
    }

    private fun getLoggingInterceptor(): Interceptor {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        return logging
    }

    @JvmStatic
    fun getUrlWithCredentials(url: String, username: String?, apiKey: String?): HttpUrl {
        return url.toHttpUrl().newBuilder()
                .addQueryParameter("username", username)
                .addQueryParameter("api_key", apiKey)
                .addQueryParameter("format", "json")
                .build()
    }

    class UserAgentInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val v = BuildConfig.VERSION_NAME
            val originalRequest: Request = chain.request()
            val requestWithUserAgent = originalRequest.newBuilder()
                    .header(HEADER_USER_AGENT, USER_AGENT + v)
                    .build()
            return chain.proceed(requestWithUserAgent)
        }
    }

}