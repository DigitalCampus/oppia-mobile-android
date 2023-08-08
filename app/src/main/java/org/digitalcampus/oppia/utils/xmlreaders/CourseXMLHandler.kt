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

import org.digitalcampus.oppia.application.App
import org.digitalcampus.oppia.database.DbHelper
import org.digitalcampus.oppia.model.Activity
import org.digitalcampus.oppia.model.CompleteCourse
import org.digitalcampus.oppia.model.Course
import org.digitalcampus.oppia.model.CourseMetaPage
import org.digitalcampus.oppia.model.GamificationEvent
import org.digitalcampus.oppia.model.Lang
import org.digitalcampus.oppia.model.Media
import org.digitalcampus.oppia.model.Section
import org.digitalcampus.oppia.utils.TextUtilsJava
import org.xml.sax.Attributes
import java.util.Stack

internal class CourseXMLHandler(private val courseId: Long, private val userId: Long, private val db: DbHelper) : DefaultLexicalHandler(), IMediaXMLHandler {
    companion object {
        private const val NODE_LANG = "lang"
        private const val NODE_TITLE = "title"
        private const val NODE_PRIORITY = "priority"
        private const val NODE_DESCRIPTION = "description"
        private const val NODE_VERSIONID = "versionid"
        private const val NODE_PAGE = "page"
        private const val NODE_LOCATION = "location"
        private const val NODE_IMAGE = "image"
        private const val NODE_ACTIVITY = "activity"
        private const val NODE_CONTENT = "content"
        private const val NODE_MEDIA = "media"
        private const val NODE_FILE = "file"
        private const val NODE_SECTION = "section"
        private const val NODE_META = "meta"
        private const val NODE_SEQUENCING = "sequencing"
        private const val NODE_GAMIFICATION = "gamification"
        private const val NODE_EVENT = "event"
        private const val ATTR_LANG = "lang"
        private const val ATTR_ID = "id"
        private const val ATTR_LENGTH = "length"
        private const val ATTR_FILESIZE = "filesize"
        private const val ATTR_DOWNLOAD_URL = "download_url"
        private const val ATTR_ORDER = "order"
        private const val ATTR_FILENAME = "filename"
        private const val ATTR_DIGEST = "digest"
        private const val ATTR_TYPE = "type"
        private const val ATTR_NAME = "name"
        private const val ATTR_PASSWORD = "password"
    }

    private var courseVersionId = 0.0
    var courseImage: String? = null
        private set
    private var courseSequencingMode: String? = null
    private var coursePriority = 0
    private val courseDescriptions = ArrayList<Lang>()
    private val courseTitles = ArrayList<Lang>()
    private val courseGamification = ArrayList<GamificationEvent>()
    private val courseLangs = ArrayList<Lang>()
    private val courseBaseline = ArrayList<Activity?>()
    private val sections = ArrayList<Section?>()
    private val courseMetaPages = ArrayList<CourseMetaPage?>()
    override val courseMedia: MutableList<Media> = ArrayList()

    //Vars for traversing the tree
    private val parentElements = Stack<String>()

    //Temporary vars
    private var currentSection: Section? = null
    private var currentLang: String? = null
    private var currentGamifEvent: String? = null
    private var currentActivity: Activity? = null
    private var sectTitles: ArrayList<Lang>? = null
    private var currentPage: CourseMetaPage? = null
    private var pageTitles: ArrayList<Lang>? = null
    private var pageLocations: ArrayList<Lang>? = null
    private var actTitles: ArrayList<Lang>? = null
    private var actLocations: ArrayList<Lang>? = null
    private var actContents: ArrayList<Lang>? = null
    private var actDescriptions: ArrayList<Lang>? = null
    private var actMedia: ArrayList<Media>? = null
    private var actGamification: ArrayList<GamificationEvent>? = null
    private val currentGamification = ArrayList<GamificationEvent>()
    private val currentMedia = ArrayList<Media>()
    override fun startElement(aUri: String, aLocalName: String, aQName: String, aAttributes: Attributes) {
        chars?.setLength(0)
        if (NODE_SECTION == aQName) {
            currentSection = Section()
            sectTitles = ArrayList()
            currentSection?.order = aAttributes.getValue(ATTR_ORDER).toInt()
            val sectionPassword = aAttributes.getValue(ATTR_PASSWORD)
            if (!TextUtilsJava.isEmpty(sectionPassword)) {
                currentSection?.password = sectionPassword
            }
            parentElements.push(NODE_SECTION)
        } else if (NODE_ACTIVITY == aQName) {
            currentActivity = Activity()
            currentActivity?.courseId = courseId
            currentActivity?.digest = aAttributes.getValue(ATTR_DIGEST)
            currentActivity?.actType = aAttributes.getValue(ATTR_TYPE)
            currentActivity?.actId = aAttributes.getValue(ATTR_ORDER).toInt()
            actTitles = ArrayList()
            actLocations = ArrayList()
            actContents = ArrayList()
            actDescriptions = ArrayList()
            actMedia = ArrayList()
            actGamification = ArrayList()
            parentElements.push(NODE_ACTIVITY)
        } else if (NODE_TITLE == aQName) {
            currentLang = aAttributes.getValue(ATTR_LANG)
        } else if (NODE_LOCATION == aQName) {
            currentLang = aAttributes.getValue(NODE_LANG)
            val mimeType = aAttributes.getValue(ATTR_TYPE)
            if (mimeType != null && NODE_ACTIVITY == parentElements.peek()) {
                currentActivity?.mimeType = mimeType
            }
        } else if (NODE_CONTENT == aQName) {
            currentLang = aAttributes.getValue(ATTR_LANG)
        } else if (NODE_DESCRIPTION == aQName) {
            currentLang = aAttributes.getValue(ATTR_LANG)
        } else if (NODE_IMAGE == aQName) {
            if (NODE_ACTIVITY == parentElements.peek()) {
                currentActivity?.setImageFile(aAttributes.getValue(ATTR_FILENAME))
            } else if (NODE_META == parentElements.peek()) {
                courseImage = aAttributes.getValue(ATTR_FILENAME)
            } else if (NODE_SECTION == parentElements.peek()) {
                currentSection?.imageFile = aAttributes.getValue(ATTR_FILENAME)
            }
        } else if (NODE_FILE == aQName) {
            val m = Media()
            m.filename = aAttributes.getValue(ATTR_FILENAME)
            m.downloadUrl = aAttributes.getValue(ATTR_DOWNLOAD_URL)
            m.digest = aAttributes.getValue(ATTR_DIGEST)
            val mediaLength = aAttributes.getValue(ATTR_LENGTH)
            val mediaFilesize = aAttributes.getValue(ATTR_FILESIZE)
            m.length = mediaLength?.toIntOrNull() ?: 0
            m.fileSize = mediaFilesize.toDoubleOrNull() ?: 0.0
            currentMedia.add(m)
        } else if (NODE_META == aQName) {
            parentElements.push(NODE_META)
        } else if (NODE_MEDIA == aQName) {
            parentElements.push(NODE_MEDIA)
        } else if (NODE_PAGE == aQName) {
            currentPage = CourseMetaPage()
            pageTitles = ArrayList()
            pageLocations = ArrayList()
            currentPage?.id = aAttributes.getValue(ATTR_ID).toInt()
            parentElements.push(NODE_PAGE)
        } else if (NODE_GAMIFICATION == aQName) {
            parentElements.push(NODE_GAMIFICATION)
        } else if (NODE_EVENT == aQName) {
            currentGamifEvent = aAttributes.getValue(ATTR_NAME)
        }
    }

    override fun endElement(aUri: String, aLocalName: String, aQName: String) {
        if (NODE_SECTION == aQName) {
            currentSection?.setTitles(sectTitles)
            if (currentSection!!.isProtectedByPassword) {
                currentSection?.isUnlocked = db.sectionUnlocked(courseId, currentSection!!.order, userId)
            }
            sections.add(currentSection)
            parentElements.pop()
        } else if (NODE_TITLE == aQName) {
            if (chars!!.isEmpty()) return
            if (NODE_SECTION == parentElements.peek()) {
                sectTitles?.add(Lang(currentLang, chars.toString()))
            } else if (NODE_ACTIVITY == parentElements.peek()) {
                actTitles?.add(Lang(currentLang, chars.toString()))
            } else if (NODE_META == parentElements.peek()) {
                courseTitles.add(Lang(if (currentLang == null) App.DEFAULT_LANG else currentLang, chars.toString()))
            } else if (NODE_PAGE == parentElements.peek()) {
                pageTitles?.add(Lang(if (currentLang == null) App.DEFAULT_LANG else currentLang, chars.toString()))
            }
        } else if (NODE_LOCATION == aQName) {
            if (chars!!.isEmpty()) return
            if (NODE_ACTIVITY == parentElements.peek()) {
                actLocations?.add(Lang(currentLang, chars.toString()))
            } else if (NODE_PAGE == parentElements.peek()) {
                pageLocations?.add(Lang(currentLang, chars.toString()))
            }
        } else if (NODE_CONTENT == aQName) {
            if (chars!!.isNotEmpty() && NODE_ACTIVITY == parentElements.peek()) {
                actContents?.add(Lang(currentLang, chars.toString()))
            }
        } else if (NODE_DESCRIPTION == aQName) {
            if (chars!!.isEmpty()) return
            if (NODE_ACTIVITY == parentElements.peek()) {
                actDescriptions?.add(Lang(currentLang, chars.toString()))
            } else if (NODE_META == parentElements.peek()) {
                courseDescriptions.add(Lang(if (currentLang == null) App.DEFAULT_LANG else currentLang, chars.toString()))
            }
        } else if (NODE_VERSIONID == aQName) {
            if (chars!!.isEmpty()) return
            courseVersionId = chars.toString().toDouble()
        } else if (NODE_PRIORITY == aQName) {
            if (chars!!.isEmpty()) return
            if (NODE_META == parentElements.peek()) {
                coursePriority = chars.toString().toInt()
            }
        } else if (NODE_SEQUENCING == aQName) {
            if (chars!!.isEmpty()) return
            if (NODE_META == parentElements.peek()) {
                courseSequencingMode = chars.toString()
            }
        } else if (NODE_ACTIVITY == aQName) {
            currentActivity?.setTitles(actTitles)
            currentActivity?.setDescriptions(actDescriptions)
            currentActivity?.setLocations(actLocations)
            currentActivity?.setContents(actContents)
            currentActivity?.media = actMedia
            currentActivity?.setGamificationEvents(actGamification)
            parentElements.pop()
            if (NODE_SECTION == parentElements.peek()) {
                currentActivity?.sectionId = currentSection!!.order
                currentActivity?.completed = db.activityCompleted(courseId.toInt(), currentActivity?.digest, userId)
                currentSection?.addActivity(currentActivity)
            } else if (NODE_META == parentElements.peek()) {
                currentActivity?.sectionId = 0
                currentActivity?.isAttempted = db.activityAttempted(courseId, currentActivity?.digest, userId)
                courseBaseline.add(currentActivity)
            }
        } else if (NODE_MEDIA == aQName) {
            parentElements.pop()
            if (!parentElements.empty() && NODE_ACTIVITY == parentElements.peek()) {
                actMedia = ArrayList()
                actMedia?.addAll(currentMedia)
            } else {
                courseMedia.addAll(currentMedia)
            }
            currentMedia.clear()
        } else if (NODE_META == aQName) {
            parentElements.pop()
        } else if (NODE_LANG == aQName) {
            if (chars!!.isEmpty()) return
            courseLangs.add(Lang(chars.toString(), ""))
        } else if (NODE_PAGE == aQName) {
            for (title in pageTitles!!) {
                for (location in pageLocations!!) {
                    if (title.language == location.language) {
                        title.location = location.content
                        currentPage!!.addLang(title)
                    }
                }
            }
            courseMetaPages.add(currentPage)
            parentElements.pop()
        } else if (NODE_GAMIFICATION == aQName) {
            parentElements.pop()
            if (NODE_ACTIVITY == parentElements.peek()) {
                actGamification = ArrayList()
                actGamification?.addAll(currentGamification)
            } else {
                courseGamification.addAll(currentGamification)
            }
            currentGamification.clear()
        } else if (NODE_EVENT == aQName) {
            if (chars!!.isEmpty()) return
            val points = chars.toString().toInt()
            val event = GamificationEvent()
            event.event = currentGamifEvent
            event.points = points
            currentGamification.add(event)
        }
    }

    fun getCourse(root: String?): CompleteCourse {
        val c = CompleteCourse(root)
        c.versionId = courseVersionId
        c.imageFile = courseImage
        c.priority = coursePriority
        c.setTitles(courseTitles)
        c.langs = courseLangs
        c.setDescriptions(courseDescriptions)
        c.baselineActivities = courseBaseline
        c.media = courseMedia
        c.metaPages = courseMetaPages
        c.sections = sections
        c.gamification = courseGamification
        if ((courseSequencingMode != null)
                && ((courseSequencingMode == Course.SEQUENCING_MODE_COURSE)
                || (courseSequencingMode == Course.SEQUENCING_MODE_SECTION)
                || (courseSequencingMode == Course.SEQUENCING_MODE_NONE))) {
            c.sequencingMode = courseSequencingMode
        }
        return c
    }


}