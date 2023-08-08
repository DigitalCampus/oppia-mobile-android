package org.digitalcampus.oppia.utils.resources

import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import org.digitalcampus.oppia.listener.OnInputEnteredListener

class JSInterfaceForInlineInput(ctx: Context) : JSInterface(ctx) {

    companion object {
        private val TAG = JSInterfaceForInlineInput::class.simpleName

        //Name of the JS interface to add to the webView
        const val INTERFACE_EXPOSED_NAME = "OppiaAndroid_InlineInput"
        private const val JS_RESOURCE_FILE = "register_inline_input.js"
    }

    private var listener: OnInputEnteredListener? = null

    init {
        loadJSInjectionSourceFile(JS_RESOURCE_FILE)
    }

    override fun getInterfaceExposedName(): String {
        return INTERFACE_EXPOSED_NAME
    }

    fun setOnInputEnteredListener(listener: OnInputEnteredListener) {
        this.listener = listener
    }

    @JavascriptInterface // must be added for API 17 or higher
    fun registerInlineInput(userInput: String) {
        Log.d(TAG, "User input! $userInput")
        listener?.inlineInputReceived(userInput)
    }
}