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

import android.util.Log
import org.digitalcampus.oppia.exception.InvalidXMLException
import org.digitalcampus.oppia.gamification.Gamification
import org.digitalcampus.oppia.model.QuizAttempt
import org.digitalcampus.oppia.model.TrackerLog
import org.digitalcampus.oppia.utils.DateUtils
import org.w3c.dom.Document
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import java.io.File
import java.io.IOException
import java.io.StringReader
import java.lang.Boolean.parseBoolean
import javax.xml.parsers.ParserConfigurationException
import kotlin.jvm.Throws

class CourseTrackerXMLReader {

    companion object {
        val TAG = CourseTrackerXMLReader::class.simpleName
        private const val NODE_TYPE = "type"
        private const val NODE_QUIZ = "quiz"
        private const val NODE_DIGEST = "digest"
        private const val NODE_SUBMITTEDDATE = "submitteddate"
        private const val NODE_COMPLETED = "completed"
        private const val NODE_SCORE = "score"
        private const val NODE_MAXSCORE = "maxscore"
        private const val NODE_PASSED = "passed"
        private const val NODE_EVENT = "event"
        private const val NODE_POINTS = "points"
    }

    private var document: Document? = null

    @Throws(InvalidXMLException::class)
    constructor(courseXML: File) {
        if (courseXML.exists()) {
            document = try {
                val builder = XMLSecurityHelper.getNewSecureDocumentBuilder()
                builder.parse(courseXML)
            } catch (e: ParserConfigurationException) {
                throw InvalidXMLException(e)
            } catch (e: SAXException) {
                throw InvalidXMLException(e)
            } catch (e: IOException) {
                throw InvalidXMLException(e)
            }
        }
    }

    @Throws(InvalidXMLException::class)
    constructor(xmlContent: String?) {
        document = try {
            val builder = XMLSecurityHelper.getNewSecureDocumentBuilder()
            val isSource = InputSource(StringReader(xmlContent))
            builder.parse(isSource)
        } catch (e: ParserConfigurationException) {
            throw InvalidXMLException(e)
        } catch (e: SAXException) {
            throw InvalidXMLException(e)
        } catch (e: IOException) {
            throw InvalidXMLException(e)
        }
    }

    fun getTrackers(courseId: Long, userId: Long): List<TrackerLog> {
        val trackers: MutableList<TrackerLog> = ArrayList()

        document?.let {
            val actTrackers : NodeList = it.firstChild.childNodes
            for (i in 0 until actTrackers.length) {
                val attrs = actTrackers.item(i).attributes
                val digest = attrs.getNamedItem(NODE_DIGEST).textContent
                val submittedDateString = attrs.getNamedItem(NODE_SUBMITTEDDATE).textContent
                val sdt = DateUtils.DATETIME_FORMAT.parseDateTime(submittedDateString)
                val completed: Boolean = parseBoolean(attrs.getNamedItem(NODE_COMPLETED).textContent)
                val type: String? = attrs.getNamedItem(NODE_TYPE).textContent ?: null
                val event: String = attrs.getNamedItem(NODE_EVENT).textContent ?: Gamification.EVENT_NAME_UNDEFINED
                val points: Int = attrs.getNamedItem(NODE_POINTS).textContent.toIntOrNull() ?: 0
                val t = TrackerLog()
                t.digest = digest
                t.isSubmitted = true
                t.datetime = sdt
                t.isCompleted = completed
                t.type = type
                t.courseId = courseId
                t.userId = userId
                t.event = event
                t.points = points
                trackers.add(t)
            }
        }

        return trackers
    }

    fun getQuizAttempts(courseId: Long, userId: Long): List<QuizAttempt> {
        val quizAttempts = ArrayList<QuizAttempt>()
        document?.let {
            val actTrackers = it.firstChild.childNodes
            for (i in 0 until actTrackers.length) {
                val attrs = actTrackers.item(i).attributes
                val digest = attrs.getNamedItem(NODE_DIGEST).textContent
                val type: String? = try {
                    attrs.getNamedItem(NODE_TYPE).textContent
                } catch (npe: NullPointerException) {
                    null
                }

                // if quiz activity then get the results etc
                if (type != null && type.equals(NODE_QUIZ, ignoreCase = true)) {
                    val quizNodes = actTrackers.item(i).childNodes
                    for (j in 0 until quizNodes.length) {
                        val quizAttrs = quizNodes.item(j).attributes
                        val maxScore = quizAttrs.getNamedItem(NODE_MAXSCORE).textContent.toFloat()
                        val score = quizAttrs.getNamedItem(NODE_SCORE).textContent.toFloat()
                        var event: String? = ""
                        try {
                            event = quizAttrs.getNamedItem(NODE_EVENT).textContent
                        } catch (npe: NullPointerException) {
                            Log.d(TAG, "Event node not found", npe)
                        }
                        var points = 0
                        try {
                            points = quizAttrs.getNamedItem(NODE_POINTS).textContent.toInt()
                        } catch (nfe: NumberFormatException) {
                            Log.d(TAG, "Points node not an integer", nfe)
                        } catch (npe: NullPointerException) {
                            Log.d(TAG, "Points node not found", npe)
                        }
                        val submittedDateString = quizAttrs.getNamedItem(NODE_SUBMITTEDDATE).textContent
                        val sdt = DateUtils.DATETIME_FORMAT.parseDateTime(submittedDateString)
                        val passed: Boolean = try {
                            java.lang.Boolean.parseBoolean(quizAttrs.getNamedItem(NODE_PASSED).textContent)
                        } catch (npe: NullPointerException) {
                            true
                        }
                        val qa = QuizAttempt()
                        qa.courseId = courseId
                        qa.userId = userId
                        qa.activityDigest = digest
                        qa.score = score
                        qa.maxscore = maxScore
                        qa.isPassed = passed
                        qa.isSent = true
                        qa.datetime = sdt
                        qa.event = event
                        qa.points = points
                        quizAttempts.add(qa)
                    }
                }
            }
        }
        return quizAttempts
    }
}