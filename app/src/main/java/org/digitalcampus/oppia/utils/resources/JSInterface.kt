package org.digitalcampus.oppia.utils.resources

import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

abstract class JSInterface(val context: Context) {
    companion object {
        const val TAG = "JSInterface"
        const val JS_FILE_REGEX = "{{INTERFACE_EXPOSED_NAME}}"
        const val JS_EXECUTION_PREFIX = "javascript: "
    }

    private var javascriptInjection: String? = null

    abstract fun getInterfaceExposedName(): String

    fun getJavascriptInjection(): String {
        return "$JS_EXECUTION_PREFIX$javascriptInjection"
    }

    protected fun loadJSInjectionSourceFile(filename: String) {
        val sb = StringBuilder()
        var reader: BufferedReader? = null
        try {
            val isStream: InputStream = context.assets.open("js_injects/$filename")
            reader = BufferedReader(InputStreamReader(isStream, StandardCharsets.UTF_8))
            var str: String?
            while (reader.readLine().also { str = it } != null) {
                sb.append(str)
            }

            javascriptInjection = sb.toString()
            javascriptInjection = javascriptInjection?.replace(JS_FILE_REGEX, getInterfaceExposedName())

        } catch (e: IOException) {
            Log.e(TAG, "Error loading JS injection source file", e)
        } finally {
            reader?.close()
        }
    }
}