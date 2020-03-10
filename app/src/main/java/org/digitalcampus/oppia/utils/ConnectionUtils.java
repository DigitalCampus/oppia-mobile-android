package org.digitalcampus.oppia.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectionUtils {

	public static final String TAG = ConnectionUtils.class.getSimpleName();

	private ConnectionUtils() {
		throw new IllegalStateException("Utility class");
	}

	public static boolean isOnWifi(Context ctx) {
		ConnectivityManager conMan = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = conMan.getActiveNetworkInfo();
		return (netInfo == null || netInfo.getType() != ConnectivityManager.TYPE_WIFI);
	}

	public static boolean isNetworkConnected(Context context) {
		return isNetworkConnected(getConnectivityManager(context));
	}

	public static boolean isNetworkConnected(ConnectivityManager manager) {
		return (manager.getActiveNetworkInfo() != null && manager.getActiveNetworkInfo().isAvailable() && manager
				.getActiveNetworkInfo().isConnected());
	}

	public static ConnectivityManager getConnectivityManager(Context ctx){
		return (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
	}

}
