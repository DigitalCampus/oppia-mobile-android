package org.digitalcampus.oppia.utils.resources;

import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;

import org.digitalcampus.mobile.learning.BuildConfig;

public class JSInterfaceForBackwardsCompat extends JSInterface{

    private static final String TAG = JSInterfaceForBackwardsCompat.class.getSimpleName();

    //Name of the JS interface to add to the webView
    public static final String INTERFACE_EXPOSED_NAME = "OppiaAndroid_BackwardsCompat";
    private static final String JS_RESOURCE_FILE = "backwards_compat.js";


    public JSInterfaceForBackwardsCompat(Context ctx) {
        super(ctx);
        loadJSInjectionSourceFile(JS_RESOURCE_FILE);
    }

    @Override
    public String getInterfaceExposedName() {
        return INTERFACE_EXPOSED_NAME;
    }

    @JavascriptInterface   // must be added for API 17 or higher
    public boolean checkCompatibility(int versionCode) {
        Log.d(TAG, "Compatibility check:" + versionCode);
        return versionCode <= BuildConfig.VERSION_CODE;
    }
}