package org.digitalcampus.oppia.api

import android.content.Context
import androidx.preference.PreferenceManager
import org.digitalcampus.mobile.learning.R
import org.digitalcampus.oppia.activity.PrefsActivity

object RemoteApiEndpoint : ApiEndpoint {

    private const val MIN_MAJOR_VERSION = 0
    private const val MIN_MIN_VERSION = 12
    private const val MIN_BUILD_VERSION = 6

    override fun getFullURL(ctx: Context?, apiPath: String?): String {
        val prefs = PreferenceManager.getDefaultSharedPreferences(ctx!!)
        var url = prefs.getString(PrefsActivity.PREF_SERVER, ctx.getString(R.string.prefServerDefault)) ?: ""
        if (!url.endsWith("/") && !apiPath!!.startsWith("/")) url += "/"
        return url + apiPath
    }

    // TODO KOTLIN: Remove JvmStatic annotation when all reference files are converted to Kotlin
    @JvmStatic
    fun isServerVersionCompatible(version: String?): Boolean {
        var modifiedVersion = version ?: return false
        if (modifiedVersion.startsWith("v")) {
            modifiedVersion = modifiedVersion.substring(1)
        }
        if (modifiedVersion.contains("-")) {
            try {
                 modifiedVersion = modifiedVersion.split("-")[0]
            } catch (e: ArrayIndexOutOfBoundsException) {
                return false
            }
        }
        val versioning = modifiedVersion.split(".")
        return if (versioning.size < 3) {
            // No correct format (expected x.x.x)
            false
        } else try {
            val majorVersion = versioning[0].toInt()
            val minorVersion = versioning[1].toInt()
            val buildVersion = versioning[2].toInt()
            majorVersion > MIN_MAJOR_VERSION ||
                    (majorVersion == MIN_MAJOR_VERSION && minorVersion > MIN_MIN_VERSION) ||
                    (majorVersion == MIN_MAJOR_VERSION && minorVersion == MIN_MIN_VERSION && buildVersion >= MIN_BUILD_VERSION)
        } catch (e: NumberFormatException) {
            false
        }
    }

}