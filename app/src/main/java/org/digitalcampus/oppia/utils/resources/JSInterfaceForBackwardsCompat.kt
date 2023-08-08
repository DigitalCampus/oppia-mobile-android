package org.digitalcampus.oppia.utils.resources

import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import org.digitalcampus.mobile.learning.BuildConfig

class JSInterfaceForBackwardsCompat(ctx: Context) : JSInterface(ctx) {

    companion object {
        private val TAG = JSInterfaceForBackwardsCompat::class.simpleName
        //Name of the JS interface to add to the webView
        const val INTERFACE_EXPOSED_NAME = "OppiaAndroid_BackwardsCompat"
        private const val JS_RESOURCE_FILE = "backwards_compat.js"
    }

    init {
        loadJSInjectionSourceFile(JS_RESOURCE_FILE)
    }

    override fun getInterfaceExposedName(): String {
        return INTERFACE_EXPOSED_NAME
    }

    @JavascriptInterface // must be added for API 17 or higher
    fun checkCompatibility(versionCode: Int): Boolean {
        Log.d(TAG, "Compatibility check:$versionCode")
        return versionCode <= BuildConfig.VERSION_CODE
    }
}