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

import org.digitalcampus.mobile.learning.BuildConfig
import org.digitalcampus.oppia.exception.GamificationEventNotFound
import org.digitalcampus.oppia.model.GamificationEvent

object Gamification {

    // event names
    const val EVENT_NAME_REGISTER = "register"
    const val EVENT_NAME_QUIZ_FIRST_ATTEMPT = "quiz_first_attempt"
    const val EVENT_NAME_QUIZ_ATTEMPT = "quiz_attempt"
    const val EVENT_NAME_QUIZ_FIRST_THRESHOLD = "quiz_first_attempt_threshold"
    const val EVENT_NAME_QUIZ_FIRST_BONUS = "quiz_first_attempt_bonus"
    const val EVENT_NAME_ACTIVITY_COMPLETED = "activity_completed"
    const val EVENT_NAME_MEDIA_STARTED = "media_started"
    const val EVENT_NAME_MEDIA_PLAYED = "media_played"
    const val EVENT_NAME_MEDIA_PLAYING_INTERVAL = "media_playing_interval"
    const val EVENT_NAME_MEDIA_PLAYING_POINTS_PER_INTERVAL = "media_playing_points_per_interval"
    const val EVENT_NAME_MEDIA_MAX_POINTS = "media_max_points"
    const val EVENT_NAME_COURSE_DOWNLOADED = "course_downloaded"
    const val EVENT_NAME_SEARCH_PERFORMED = "search_performed"
    const val EVENT_NAME_MEDIA_MISSING = "media_missing"
    const val EVENT_NAME_MEDIA_THRESHOLD_PASSED = "media_threshold_passed"
    const val EVENT_NAME_FEEDBACK_COMPLETED = "feedback_completed"
    const val EVENT_NAME_UNDEFINED = "undefined" //fallback option

    // default points for gamification
    @JvmField
    val GAMIFICATION_REGISTER = GamificationEvent(EVENT_NAME_REGISTER, 100)
    val GAMIFICATION_QUIZ_FIRST_ATTEMPT = GamificationEvent(EVENT_NAME_QUIZ_FIRST_ATTEMPT, 20)
    val GAMIFICATION_QUIZ_FIRST_ATTEMPT_TODAY = GamificationEvent(EVENT_NAME_QUIZ_ATTEMPT, 10)
    val GAMIFICATION_QUIZ_ATTEMPT = GamificationEvent(EVENT_NAME_QUIZ_ATTEMPT, 0)
    val GAMIFICATION_QUIZ_FIRST_ATTEMPT_THRESHOLD = GamificationEvent(EVENT_NAME_QUIZ_FIRST_THRESHOLD, 100)
    val GAMIFICATION_QUIZ_FIRST_ATTEMPT_BONUS = GamificationEvent(EVENT_NAME_QUIZ_FIRST_BONUS, 50)
    val GAMIFICATION_ACTIVITY_COMPLETED = GamificationEvent(EVENT_NAME_ACTIVITY_COMPLETED, 10)
    val GAMIFICATION_MEDIA_STARTED = GamificationEvent(EVENT_NAME_MEDIA_STARTED, 20)
    val GAMIFICATION_MEDIA_PLAYING_INTERVAL = GamificationEvent(EVENT_NAME_MEDIA_PLAYING_INTERVAL, 30)
    val GAMIFICATION_MEDIA_PLAYING_POINTS_PER_INTERVAL = GamificationEvent(EVENT_NAME_MEDIA_PLAYING_POINTS_PER_INTERVAL, 10)
    val GAMIFICATION_MEDIA_MAX_POINTS = GamificationEvent(EVENT_NAME_MEDIA_MAX_POINTS, 200)
    @JvmField
    val GAMIFICATION_COURSE_DOWNLOADED = GamificationEvent(EVENT_NAME_COURSE_DOWNLOADED, 50)
    val GAMIFICATION_SEARCH_PERFORMED = GamificationEvent(EVENT_NAME_SEARCH_PERFORMED, 0)
    val GAMIFICATION_MEDIA_MISSING = GamificationEvent(EVENT_NAME_MEDIA_MISSING, 0)
    val GAMIFICATION_MEDIA_THRESHOLD_PASSED = GamificationEvent(EVENT_NAME_MEDIA_THRESHOLD_PASSED, 150)
    val GAMIFICATION_FEEDBACK_COMPLETED = GamificationEvent(EVENT_NAME_FEEDBACK_COMPLETED, 50)
    @JvmField
    val GAMIFICATION_UNDEFINED = GamificationEvent(EVENT_NAME_UNDEFINED, 0)

    // Gamification points configuration
    const val GAMIFICATION_POINTS_ANIMATION = BuildConfig.GAMIFICATION_POINTS_ANIMATION.toString()
    const val DURATION_GAMIFICATION_POINTS_VIEW = BuildConfig.DURATION_GAMIFICATION_POINTS_VIEW
    const val MEDIA_CRITERIA_INTERVALS = "intervals"
    const val MEDIA_CRITERIA_THRESHOLD = "threshold"
    const val DEFAULT_MEDIA_CRITERIA = BuildConfig.GAMIFICATION_MEDIA_CRITERIA
    const val DEFAULT_MEDIA_THRESHOLD = BuildConfig.GAMIFICATION_DEFAULT_MEDIA_THRESHOLD

    private val events = ArrayList<GamificationEvent>()

    init {
        events.add(GAMIFICATION_REGISTER)
        events.add(GAMIFICATION_QUIZ_FIRST_ATTEMPT)
        events.add(GAMIFICATION_QUIZ_FIRST_ATTEMPT_TODAY)
        events.add(GAMIFICATION_QUIZ_ATTEMPT)
        events.add(GAMIFICATION_QUIZ_FIRST_ATTEMPT_THRESHOLD)
        events.add(GAMIFICATION_QUIZ_FIRST_ATTEMPT_BONUS)
        events.add(GAMIFICATION_ACTIVITY_COMPLETED)
        events.add(GAMIFICATION_MEDIA_STARTED)
        events.add(GAMIFICATION_MEDIA_PLAYING_INTERVAL)
        events.add(GAMIFICATION_MEDIA_PLAYING_POINTS_PER_INTERVAL)
        events.add(GAMIFICATION_MEDIA_MAX_POINTS)
        events.add(GAMIFICATION_COURSE_DOWNLOADED)
        events.add(GAMIFICATION_SEARCH_PERFORMED)
        events.add(GAMIFICATION_MEDIA_MISSING)
        events.add(GAMIFICATION_MEDIA_THRESHOLD_PASSED)
        events.add(GAMIFICATION_FEEDBACK_COMPLETED)
    }

    @Throws(GamificationEventNotFound::class)
    fun getEvent(event: String): GamificationEvent {
        return events.find { it.event == event } ?: throw GamificationEventNotFound(event)
    }
}