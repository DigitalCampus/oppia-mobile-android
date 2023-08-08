package org.digitalcampus.oppia.utils.resources

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import org.digitalcampus.mobile.learning.BuildConfig
import org.digitalcampus.mobile.learning.R
import org.digitalcampus.oppia.utils.storage.FileUtils
import org.digitalcampus.oppia.utils.storage.Storage
import java.io.File
import java.util.regex.Pattern

class ExternalResourceOpener private constructor() {
    init {
        throw IllegalStateException("Utility class")
    }

    companion object {
        private const val FILEPROVIDER_AUTHORITY = BuildConfig.APPLICATION_ID + ".provider"
        private const val GOOGLE_PLAY_INTENT_URI = "market://details?id="
        private const val GOOGLE_PLAY_INTENT_URL = "https://play.google.com/store/apps/details?id="
        private const val RESOURCE_SUBPATH = "resources/"
        private const val RESOURCE_HREF_REGEX = "href=\"([0-9A-Za-z./\\-']+)\""
        private val MIMETYPE_OPENER_PACKAGES: Map<String, String> = object : HashMap<String, String>() {
            init {
                put("application/pdf", "com.artifex.mupdf.viewer.app")
            }
        }

        @JvmStatic
        fun getResourcesFromContent(content: String?): List<String> {
            val resources: MutableList<String> = ArrayList()
            val m = Pattern.compile(RESOURCE_HREF_REGEX).matcher(content)
            while (m.find()) {
                val url = m.group(1)
                if (url.contains(RESOURCE_SUBPATH)) {
                    var filename = m.group(1)
                    filename = filename.substring(filename.lastIndexOf(RESOURCE_SUBPATH) + RESOURCE_SUBPATH.length)
                    resources.add(filename)
                }
            }
            return resources
        }

        @JvmStatic
        fun getIntentToOpenResource(ctx: Context, resourceFile: File): Intent? {
            var resourceFile = resourceFile
            val resourceMimeType = FileUtils.getMimeType(resourceFile.path)
            val storageLocationRoot = Storage.getStorageLocationRoot(ctx)
            if (storageLocationRoot != null && resourceFile.absolutePath.contains(storageLocationRoot)) {
                val relativePath = resourceFile.absolutePath.substring(storageLocationRoot.length + 1)
                //Create the file descriptor again to avoid possible file:// prefixes in the URI
                resourceFile = File(Storage.getStorageLocationRoot(ctx), relativePath)
            }
            if (!resourceFile.exists()) {
                return null
            }
            val resourceUri = FileProvider.getUriForFile(ctx, FILEPROVIDER_AUTHORITY, resourceFile)

            // check there is actually an app installed to open this filetype
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            intent.setDataAndType(resourceUri, resourceMimeType)
            val targetAppPackage = getAppToResolveIntent(ctx, intent)
            if (targetAppPackage != null) {
                //In case there is a valid filter, we grant permission and return the intent, otherwise null
                ctx.grantUriPermission(targetAppPackage, resourceUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                return intent
            }
            return null
        }

        @JvmStatic
        fun getIntentToInstallAppForResource(ctx: Context, resourceFile: File): Intent? {
            val resourceMimeType = FileUtils.getMimeType(resourceFile.path)
            if (!MIMETYPE_OPENER_PACKAGES.containsKey(resourceMimeType)) {
                return null
            }
            val appPackage = MIMETYPE_OPENER_PACKAGES[resourceMimeType]
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            intent.data = Uri.parse(GOOGLE_PLAY_INTENT_URI + appPackage)

            // If Google Play is not installed, we open the Google Play link in the browser
            if (getAppToResolveIntent(ctx, intent) == null) {
                intent.data = Uri.parse(GOOGLE_PLAY_INTENT_URL + appPackage)
            }
            return intent
        }

        private fun getAppToResolveIntent(ctx: Context, i: Intent): String? {
            val pm = ctx.packageManager
            val activityInfo = i.resolveActivityInfo(pm, 0)
            val appFound = activityInfo != null && activityInfo.exported
            return if (appFound) activityInfo!!.packageName else null
        }

        @JvmStatic
        fun constructShareFileIntent(ctx: Context?, fileToShare: File, type: String?): Intent? {
            if (!fileToShare.exists()) {
                return null
            }
            val share = Intent(Intent.ACTION_SEND)
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            share.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            share.type = type
            val targetUri = FileProvider.getUriForFile(ctx!!, FILEPROVIDER_AUTHORITY, fileToShare)
            share.putExtra(Intent.EXTRA_STREAM, targetUri)
            return share
        }

        @JvmStatic
        fun shareFile(context: Context, fileToShare: File, type: String?) {
            val intentShare = constructShareFileIntent(context, fileToShare, type)
            if (intentShare == null) {
                Toast.makeText(context, context.getString(R.string.error_resource_not_found, fileToShare.name), Toast.LENGTH_SHORT).show()
            }
            try {
                context.startActivity(intentShare)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, R.string.no_app_to_share, Toast.LENGTH_SHORT).show()
            }
        }
    }
}