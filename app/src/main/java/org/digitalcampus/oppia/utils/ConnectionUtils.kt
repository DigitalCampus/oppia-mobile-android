package org.digitalcampus.oppia.utils

import android.content.Context
import android.net.ConnectivityManager
import org.digitalcampus.oppia.annotations.Mockable

@Mockable
class ConnectionUtils {
    fun isConnected(context: Context): Boolean {
        return isNetworkConnected(getConnectivityManager(context))
    }

    companion object {
        val TAG = ConnectionUtils::class.java.simpleName
        @JvmStatic
        fun isOnWifi(ctx: Context): Boolean {
            val conMan = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val netInfo = conMan.activeNetworkInfo
            return netInfo == null || netInfo.type != ConnectivityManager.TYPE_WIFI
        }

        @JvmStatic
        fun isNetworkConnected(context: Context): Boolean {
            return isNetworkConnected(getConnectivityManager(context))
        }

        @JvmStatic
        fun isNetworkConnected(manager: ConnectivityManager): Boolean {
            return manager.activeNetworkInfo != null && manager.activeNetworkInfo!!.isAvailable && manager
                    .activeNetworkInfo!!.isConnected
        }

        @JvmStatic
        fun getConnectivityManager(ctx: Context): ConnectivityManager {
            return ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        }
    }
}