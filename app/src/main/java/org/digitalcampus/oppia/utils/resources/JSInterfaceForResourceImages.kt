package org.digitalcampus.oppia.utils.resources

import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import android.widget.Toast
import org.digitalcampus.mobile.learning.R
import java.io.File

class JSInterfaceForResourceImages(context: Context, private val resourcesLocation: String) :
        JSInterface(context) {

    companion object {
        const val INTERFACE_EXPOSED_NAME = "OppiaAndroid_ResourceImages"
        const val JS_RESOURCE_FILE = "open_file.js"
    }

    init {
        loadJSInjectionSourceFile(JS_RESOURCE_FILE)
    }

    override fun getInterfaceExposedName(): String {
        return INTERFACE_EXPOSED_NAME
    }

    @JavascriptInterface
    fun openFile(relativeFilePath: String) {
        val fileToOpen = File(resourcesLocation + relativeFilePath)
        Log.d(TAG, "File to open externally: ${fileToOpen.path}")
        val intent = ExternalResourceOpener.getIntentToOpenResource(context, fileToOpen)
        if (intent != null) {
            context.startActivity(intent)
        } else {
            Toast.makeText(
                    context,
                    context.getString(R.string.error_resource_app_not_found, relativeFilePath),
                    Toast.LENGTH_LONG
            ).show()
        }
    }
}