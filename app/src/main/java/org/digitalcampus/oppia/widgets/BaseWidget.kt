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
package org.digitalcampus.oppia.widgets

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import org.digitalcampus.mobile.learning.BuildConfig
import org.digitalcampus.mobile.learning.R
import org.digitalcampus.oppia.activity.PrefsActivity
import org.digitalcampus.oppia.activity.VideoPlayerActivity
import org.digitalcampus.oppia.application.App
import org.digitalcampus.oppia.application.Tracker
import org.digitalcampus.oppia.di.AppComponent
import org.digitalcampus.oppia.model.Activity
import org.digitalcampus.oppia.model.Course
import org.digitalcampus.oppia.utils.TextUtilsJava
import org.digitalcampus.oppia.utils.storage.FileUtils.getMimeType
import org.digitalcampus.oppia.utils.storage.FileUtils.isSupportedMediafileType
import org.digitalcampus.oppia.utils.storage.Storage.getMediaPath
import org.digitalcampus.oppia.utils.storage.Storage.mediaFileExists
import java.util.Locale


abstract class BaseWidget : Fragment() {

    companion object {
        val TAG = BaseWidget::class.simpleName
        const val QUIZ_EXCEPTION_MESSAGE = "Invalid Quiz Error: "
        const val ACTION_TEXT_SIZE_CHANGED = BuildConfig.APPLICATION_ID + ".ACTION_TEXT_SIZE_CHANGED"
        const val WIDGET_CONFIG = "widget_config"
        const val PROPERTY_ACTIVITY_STARTTIME = "Activity_StartTime"
        const val PROPERTY_COURSE = "Course"
        const val PROPERTY_ACTIVITY = "Activity"
    }

    @JvmField protected var activity: Activity? = null
    @JvmField protected var course: Course? = null
    @JvmField protected var prefs: SharedPreferences? = null
    @JvmField protected var isBaseline = false
    @JvmField protected var readAloud = false
    protected var prefLang: String? = null
    private var startTime: Long = 0
    private var spentTime: Long = 0
    private var currentTimeAccounted = false

    abstract fun getActivityCompleted(): Boolean
    abstract fun saveTracker()
    abstract fun getContentToRead(): String?
    abstract fun getWidgetConfig(): HashMap<String, Any>?
    abstract fun setWidgetConfig(config: HashMap<String, Any>)

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (TextUtilsJava.equals(intent.action, ACTION_TEXT_SIZE_CHANGED)) {
                val fontSize = prefs?.getString(PrefsActivity.PREF_TEXT_SIZE, "16")?.toInt() ?: 16
                onTextSizeChanged(fontSize)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate WidgetFactory: " + this.javaClass.simpleName)
        prefs = PreferenceManager.getDefaultSharedPreferences(requireActivity())
        prefLang = prefs?.getString(PrefsActivity.PREF_CONTENT_LANGUAGE, Locale.getDefault().language)
        requireActivity().registerReceiver(receiver, IntentFilter(ACTION_TEXT_SIZE_CHANGED))
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().unregisterReceiver(receiver)
    }

    fun setReadAloud(readAloud: Boolean) {
        this.readAloud = readAloud
    }

    protected fun getDigest(): String? {
        return activity?.digest
    }

    open fun setIsBaseline(isBaseline: Boolean) {
        this.isBaseline = isBaseline
    }

    protected fun setStartTime(startTime: Long) {
        this.startTime = if (startTime != 0L) startTime else System.currentTimeMillis() / 1000
        currentTimeAccounted = false
    }

    fun getAppComponent(): AppComponent {
        val app = requireActivity().application as App
        return app.component
    }

    fun getStartTime(): Long {
        return if (startTime != 0L) startTime else System.currentTimeMillis() / 1000
    }

    private fun addSpentTime() {
        val start = getStartTime()
        val now = System.currentTimeMillis() / 1000
        val spent = now - start
        spentTime += spent
        currentTimeAccounted = true
    }

    fun resetTimeTracking() {
        spentTime = 0
        startTime = System.currentTimeMillis() / 1000
        currentTimeAccounted = false
    }

    fun resumeTimeTracking() {
        startTime = System.currentTimeMillis() / 1000
        currentTimeAccounted = false
    }

    fun pauseTimeTracking() {
        addSpentTime()
    }

    fun getSpentTime(): Long {
        if (!currentTimeAccounted) {
            addSpentTime()
        }
        return spentTime
    }

    protected fun startMediaPlayerWithFile(mediaFileName: String) {
        var filename = mediaFileName
        val ctx: Context = requireActivity()
        // check media file exists
        if (!mediaFileExists(ctx, filename)) {
            //Try if the filename is URL encoded
            filename = Uri.decode(mediaFileName)
            if (!mediaFileExists(ctx, filename)) {
                Toast.makeText(
                    ctx,
                    getString(R.string.error_media_not_found, mediaFileName),
                    Toast.LENGTH_LONG
                ).show()
                val m = activity?.getMedia(mediaFileName)
                val digest = m?.digest ?: ""
                Tracker(ctx).saveMissingMediaTracker(course!!.courseId, digest, mediaFileName)
                return
            }
        }
        val mimeType = getMimeType(getMediaPath(ctx) + filename)
        if (!isSupportedMediafileType(mimeType)) {
            Toast.makeText(
                ctx,
                getString(R.string.error_media_unsupported, mediaFileName),
                Toast.LENGTH_LONG
            ).show()
            return
        }
        val intent = Intent(ctx, VideoPlayerActivity::class.java)
        val tb = Bundle()
        tb.putSerializable(VideoPlayerActivity.MEDIA_TAG, filename)
        tb.putSerializable(Activity.TAG, activity)
        tb.putSerializable(Course.TAG, course)
        intent.putExtras(tb)
        startActivity(intent)
    }

    protected open fun onTextSizeChanged(fontSize: Int) {
        // pages that need this can override
    }
}