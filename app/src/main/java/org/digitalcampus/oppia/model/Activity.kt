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
package org.digitalcampus.oppia.model

import android.util.Log
import org.digitalcampus.mobile.learning.R
import org.digitalcampus.oppia.analytics.Analytics
import org.digitalcampus.oppia.exception.GamificationEventNotFound
import org.digitalcampus.oppia.utils.storage.FileUtils
import java.io.File
import java.io.IOException
import java.io.Serializable

class Activity : MultiLangInfoModel(), Serializable {
    companion object {
        private const val serialVersionUID = -1548943805902073988L
        @JvmField
        val TAG = Activity::class.simpleName
    }

    var courseId: Long = 0
    var sectionId = 0
    var actId = 0
    var dbId = 0
    var actType: String? = null
    private var locations: List<Lang> = ArrayList()
    private var contents: List<Lang> = ArrayList()
    var digest: String? = null
    private var imageFile: String? = null
    var media: List<Media> = ArrayList()
    var completed = false
    var isAttempted = false
    private var customImage = false
    var mimeType: String? = null
    private var gamificationEvents: List<GamificationEvent> = ArrayList()
    var wordCount = 0

    fun hasCustomImage(): Boolean {
        return customImage
    }

    fun getImageFilePath(prefix: String): String {
        var prefixedPath = prefix
        if (!prefixedPath.endsWith(File.separator)) {
            prefixedPath += File.separator
        }
        return prefixedPath + imageFile
    }

    fun getDefaultResourceImage(): Int {
        return when(actType) {
            "quiz" -> R.drawable.default_icon_quiz
            "activity" -> if (hasMedia()) R.drawable.default_icon_video else R.drawable.default_icon_activity
            else -> R.drawable.default_icon_activity
        }
    }

    fun setImageFile(imageFile: String?) {
        this.imageFile = imageFile
        customImage = true
    }

    fun hasMedia(): Boolean {
        return media.isNotEmpty()
    }

    fun getMedia(filename: String): Media? {
        return media.find { it.filename == filename }
    }

    fun getLocation(lang: String?): String? {
        val matchingLang = locations.find { it.language.equals(lang, ignoreCase = true) }
        return matchingLang?.content ?: if (locations.isNotEmpty()) locations[0].content else null
    }

    fun setLocations(locations: List<Lang>) {
        this.locations = locations
    }

    fun getContents(lang: String?): String {
        val matchingLang = contents.find { it.language.equals(lang, ignoreCase = true) }
        return matchingLang?.content ?: if (contents.isNotEmpty()) contents[0].content else "No content"
    }

    fun getFileContents(courseLocation: String, lang: String?): String? {
        val fileContent = StringBuilder()
        if (getLocation(lang) != null && actType != "url") {
            val url = courseLocation + getLocation(lang)
            try {
                fileContent.append(" ")
                fileContent.append(FileUtils.readFile(url))
                return fileContent.toString().trim()
            } catch (e: IOException) {
                Analytics.logException(e)
                Log.d(TAG, "IOException:", e)
            }
        }
        return null
    }

    fun setContents(contents: List<Lang>) {
        this.contents = contents
    }

    fun setGamificationEvents(events: List<GamificationEvent>) {
        gamificationEvents = events
    }

    @Throws(GamificationEventNotFound::class)
    fun findGamificationEvent(event: String): GamificationEvent {
        return gamificationEvents.find { it.event == event }
            ?: throw GamificationEventNotFound(event)
    }
}