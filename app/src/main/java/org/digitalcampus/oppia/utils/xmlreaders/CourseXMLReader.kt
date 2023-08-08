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
package org.digitalcampus.oppia.utils.xmlreaders

import android.content.Context
import android.util.Log
import org.digitalcampus.mobile.learning.R
import org.digitalcampus.oppia.analytics.Analytics
import org.digitalcampus.oppia.application.SessionManager
import org.digitalcampus.oppia.database.DbHelper
import org.digitalcampus.oppia.exception.InvalidXMLException
import org.digitalcampus.oppia.model.CompleteCourse
import org.digitalcampus.oppia.model.Media
import org.digitalcampus.oppia.utils.storage.Storage.getStorageLocationRoot
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import org.xml.sax.XMLReader
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import kotlin.jvm.Throws

class CourseXMLReader(filename: String, private val courseId: Long, private val ctx: Context) {

    companion object {
        val TAG = CourseXMLReader::class.simpleName
    }

    private val reader: XMLReader? = XMLSecurityHelper.getSecureXMLReader()
    private var completeParseHandler: CourseXMLHandler? = null
    private var mediaParseHandler: CourseMediaXMLHandler? = null
    private val courseXML: File = File(filename)

    enum class ParseMode {
        COMPLETE, ONLY_META, ONLY_MEDIA
    }

    init {
        if (!courseXML.exists()) {
            Log.d(TAG, "course XML not found at: $filename")
            throw InvalidXMLException("Course XML not found at: $filename")
        }
    }

    @Throws(InvalidXMLException::class)
    fun parse(parseMode: ParseMode): Boolean {
        if (courseXML.exists()) {
            try {
                if (parseMode == ParseMode.ONLY_MEDIA) {
                    parseMedia()
                } else {
                    parseComplete()
                }
            } catch (e: Exception) {
                Analytics.logException(e)
                Log.d(TAG, "Error loading course", e)
                throw InvalidXMLException(e, ctx.resources.getString(R.string.error_reading_xml))
            }
            return true
        } else {
            Log.d(TAG, "course XML not found at: " + courseXML.path)
            return false
        }
    }

    private fun parseComplete() {
        val db = DbHelper.getInstance(ctx)
        val userId = db.getUserId(SessionManager.getUsername(ctx))
        completeParseHandler = CourseXMLHandler(courseId, userId, db)
        reader?.contentHandler = completeParseHandler
        reader?.setProperty("http://xml.org/sax/properties/lexical-handler", completeParseHandler)
        val inputStream: InputStream = BufferedInputStream(FileInputStream(courseXML))
        reader?.parse(InputSource(inputStream))
    }

    private fun parseMedia() {
        mediaParseHandler = CourseMediaXMLHandler()
        reader?.contentHandler = mediaParseHandler
        reader?.setProperty("http://xml.org/sax/properties/lexical-handler", mediaParseHandler)
        val inputStream: InputStream = BufferedInputStream(FileInputStream(courseXML))
        reader?.parse(InputSource(inputStream))
    }

    @Throws(InvalidXMLException::class)
    fun getParsedCourse(): CompleteCourse {
        if (completeParseHandler == null) {
            parse(ParseMode.COMPLETE)
        }
        val location = getStorageLocationRoot(ctx)
        return completeParseHandler!!.getCourse(location)
    }

    @Throws(InvalidXMLException::class)
    fun getMediaResponses(): IMediaXMLHandler? {
        return if (mediaParseHandler != null) {
            mediaParseHandler
        } else if (completeParseHandler != null) {
            completeParseHandler
        } else {
            parse(ParseMode.ONLY_MEDIA)
            mediaParseHandler
        }
    }

    fun getMedia(): List<Media>{
        return getMediaResponses()!!.courseMedia
    }
}