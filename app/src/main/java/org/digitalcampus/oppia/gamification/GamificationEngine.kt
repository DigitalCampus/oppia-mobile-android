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
package org.digitalcampus.oppia.gamification

import android.content.Context
import android.util.Log
import org.digitalcampus.mobile.learning.BuildConfig
import org.digitalcampus.mobile.learning.R
import org.digitalcampus.oppia.analytics.Analytics.logException
import org.digitalcampus.oppia.database.DbHelper
import org.digitalcampus.oppia.exception.GamificationEventNotFound
import org.digitalcampus.oppia.gamification.Gamification
import org.digitalcampus.oppia.gamification.Gamification.getEvent
import org.digitalcampus.oppia.model.Activity
import org.digitalcampus.oppia.model.Course
import org.digitalcampus.oppia.model.GamificationEvent
import java.util.Locale
import kotlin.math.floor
import kotlin.math.roundToInt

/* This holds all the rules for allocating points */
class GamificationEngine(private val ctx: Context) {

    val TAG = GamificationEngine::class.simpleName
    private val LOG_EVENT_NOT_FOUND = "Cannot find gamification event: "


    /*
    This finds the level of points for the event that should be used, basic logic:
    does the activity have custom points for this event? - if yes, use these
    if not, does the course have custom points for this event? - if yes, use these
    if not, use the app level defaults
     */
    @Throws(GamificationEventNotFound::class)
    private fun getEventFromHierarchy(course: Course?, activity: Activity?, event: String): GamificationEvent {
        if (activity != null) {
            // check if the activity has custom points for this event
            try {
                return activity.findGamificationEvent(event)
            } catch (genf: GamificationEventNotFound) {
                // do nothing
            }
        }
        if (course != null) {
            // check if the course has custom points for this event
            try {
                return course.findGamificationEvent(event)
            } catch (genf: GamificationEventNotFound) {
                // do nothing
            }
        }
        // else use the the app level defaults
        return Gamification.getEvent(event)
    }

    private fun getMediaCompletionCriteriaFromHierarchy(): String {
        return Gamification.DEFAULT_MEDIA_CRITERIA
    }

    private fun getMediaCompletionThresholdFromHierarchy(): Int {
        return Gamification.DEFAULT_MEDIA_THRESHOLD
    }

    @Throws(GamificationEventNotFound::class)
    private fun getEventFromCourseHierarchy(course: Course?, event: String): GamificationEvent {
        if (course != null) {
            // check if the course has custom points for this event
            try {
                return course.findGamificationEvent(event)
            } catch (genf: GamificationEventNotFound) {
                // do nothing
            }
        }

        // else use the the app level defaults
        Log.d(TAG, "$event: using global event definition")
        return Gamification.getEvent(event)
    }

    /*
    App level points/event only - shouldn't be added as custom points at activity or course level
     */
    fun processEventRegister(): GamificationEvent {
        return Gamification.GAMIFICATION_REGISTER
    }

    fun processEventQuizAttempt(course: Course?, activity: Activity, scorePercent: Float): GamificationEvent {
        var totalPoints = 0
        val db = DbHelper.getInstance(ctx)

        // is it the first attempt at this quiz?
        if (db.isQuizFirstAttempt(activity.digest)) {
            try {
                totalPoints += getEventFromHierarchy(course, activity, Gamification.EVENT_NAME_QUIZ_FIRST_ATTEMPT).points
                if (scorePercent > 0) {
                    totalPoints += scorePercent.roundToInt()
                }
                // on first attempt add the percent points
                if (scorePercent.roundToInt() >= getEventFromHierarchy(course, activity, Gamification.EVENT_NAME_QUIZ_FIRST_THRESHOLD).points) {
                    totalPoints += getEventFromHierarchy(course, activity, Gamification.EVENT_NAME_QUIZ_FIRST_BONUS).points
                }
            } catch (genf: GamificationEventNotFound) {
                Log.d(TAG, LOG_EVENT_NOT_FOUND + genf.eventName, genf)
                logException(genf)
            }
        } else if (db.isQuizFirstAttemptToday(activity.digest)) {
            try {
                totalPoints += getEventFromHierarchy(course, activity, Gamification.EVENT_NAME_QUIZ_ATTEMPT).points
            } catch (genf: GamificationEventNotFound) {
                Log.d(TAG, LOG_EVENT_NOT_FOUND + genf.eventName, genf)
                logException(genf)
            }
        } else {
            Log.d(TAG, "Not first attempt, nor first attempt today")
        }
        return GamificationEvent(Gamification.EVENT_NAME_QUIZ_ATTEMPT, totalPoints)
    }

    fun processEventActivityCompleted(course: Course?, activity: Activity): GamificationEvent {
        var totalPoints = 0
        val db = DbHelper.getInstance(ctx)
        if (db.isActivityFirstAttemptToday(activity.digest)) {
            try {
                totalPoints += getEventFromHierarchy(course, activity, Gamification.EVENT_NAME_ACTIVITY_COMPLETED).points
            } catch (genf: GamificationEventNotFound) {
                Log.d(TAG, LOG_EVENT_NOT_FOUND + Gamification.EVENT_NAME_ACTIVITY_COMPLETED, genf)
                logException(genf)
            }
        }
        return GamificationEvent(Gamification.EVENT_NAME_ACTIVITY_COMPLETED, totalPoints)
    }

    fun processEventMediaPlayed(course: Course?, activity: Activity, mediaFileName: String?, timeTaken: Long, mediaEndReached: Boolean): GamificationEvent {
        var totalPoints = 0
        var completed = false
        val db = DbHelper.getInstance(ctx)
        val m = activity.getMedia(mediaFileName!!)
        if (m != null) {
            try {
                val criteria: String = this.getMediaCompletionCriteriaFromHierarchy()
                Log.d(TAG, "Video criteria: $criteria")
                if (criteria == Gamification.MEDIA_CRITERIA_THRESHOLD) {
                    val threshold: Int = this.getMediaCompletionThresholdFromHierarchy()
                    val percentViewed = timeTaken * 100 / m.length
                    Log.d(TAG, "$percentViewed% viewed, $threshold threshold")
                    if (percentViewed > threshold) {
                        completed = true
                        Log.d(TAG, "Threshold passed!")
                        if (!db.isMediaPlayed(activity.digest)) {
                            Log.d(TAG, "First view --> giving points")
                            totalPoints += getEventFromHierarchy(course, activity, Gamification.EVENT_NAME_MEDIA_THRESHOLD_PASSED).points
                        }
                    }
                } else {
                    //Using intervals media criteria
                    // add points if first attempt today
                    if (db.isActivityFirstAttemptToday(activity.digest)) {
                        totalPoints += getEventFromHierarchy(course, activity, Gamification.EVENT_NAME_MEDIA_STARTED).points
                    }
                    // add points for length of time the media has been playing
                    totalPoints += (getEventFromHierarchy(course, activity, Gamification.EVENT_NAME_MEDIA_PLAYING_POINTS_PER_INTERVAL).points * floor(
                        timeTaken.toDouble() / getEventFromHierarchy(course, activity, Gamification.EVENT_NAME_MEDIA_PLAYING_INTERVAL).points.toDouble()
                    )).toInt()

                    // make sure can't exceed max points
                    totalPoints = totalPoints.coerceAtMost(getEventFromHierarchy(course, activity, Gamification.EVENT_NAME_MEDIA_MAX_POINTS).points
                    )
                }
            } catch (genf: GamificationEventNotFound) {
                Log.d(TAG, LOG_EVENT_NOT_FOUND + genf.eventName, genf)
                logException(genf)
            }
        }
        if (BuildConfig.GAMIFICATION_MEDIA_SHOULD_REACH_END) {
            completed = completed && mediaEndReached
        }
        return GamificationEvent(Gamification.EVENT_NAME_MEDIA_PLAYED, totalPoints, completed)
    }

    // needs to be able to access the course object at the point the tracker is triggered
    fun processEventCourseDownloaded(): GamificationEvent {
        return Gamification.GAMIFICATION_COURSE_DOWNLOADED
    }

    fun processEventCourseDownloaded(course: Course?): GamificationEvent {
        var totalPoints = 0
        try {
            totalPoints = getEventFromCourseHierarchy(course, Gamification.EVENT_NAME_COURSE_DOWNLOADED).points
        } catch (genf: GamificationEventNotFound) {
            //do nothing
        }
        return GamificationEvent(Gamification.EVENT_NAME_COURSE_DOWNLOADED, totalPoints)
    }

    fun processEventResourceActivity(course: Course?, activity: Activity?): GamificationEvent {
        return try {
            getEventFromHierarchy(course, activity, Gamification.EVENT_NAME_ACTIVITY_COMPLETED)
        } catch (genf: GamificationEventNotFound) {
            Gamification.GAMIFICATION_UNDEFINED
        }
    }

    fun processEventResourceStoppedActivity(): GamificationEvent {
        return Gamification.GAMIFICATION_UNDEFINED
    }

    fun processEventFeedbackActivity(course: Course?, activity: Activity): GamificationEvent {
        val db = DbHelper.getInstance(ctx)
        var totalPoints = 0
        if (db.isQuizFirstAttempt(activity.digest)) {
            try {
                totalPoints += getEventFromHierarchy(course, activity, Gamification.EVENT_NAME_FEEDBACK_COMPLETED).points
            } catch (genf: GamificationEventNotFound) {
                Log.d(TAG, LOG_EVENT_NOT_FOUND + genf.eventName, genf)
                logException(genf)
            }
        } else {
            Log.d(TAG, "Not first attempt, nor first attempt today")
        }
        return GamificationEvent(Gamification.EVENT_NAME_FEEDBACK_COMPLETED, totalPoints)
    }

    fun processEventURLActivity(course: Course?, activity: Activity?): GamificationEvent {
        return try {
            getEventFromHierarchy(course, activity, Gamification.EVENT_NAME_ACTIVITY_COMPLETED)
        } catch (genf: GamificationEventNotFound) {
            Gamification.GAMIFICATION_UNDEFINED
        }
    }

    fun getEventMessage(event: GamificationEvent, c: Course?, a: Activity?): String {
        val prefLang = Locale.getDefault().language
        val resId = ctx.resources.getIdentifier("points_event_" + event.event, "string", ctx.packageName)
        if (resId <= 0) {
            //The string resource for that event was not found
            return c?.getTitle(prefLang) ?: ctx.getString(R.string.points_event_default)
        }
        return if (a != null) {
            ctx.getString(resId, c?.getTitle(prefLang) ?: "", a.getTitle(prefLang))
        } else {
            ctx.getString(resId, c?.getTitle(prefLang) ?: "")
        }
    }
}