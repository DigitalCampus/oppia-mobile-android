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

package org.digitalcampus.oppia.gamification;

import org.digitalcampus.oppia.model.GamificationEvent;

public class DefaultGamification {

    // event names
    public static final String EVENT_NAME_REGISTER = "register";
    public static final String EVENT_NAME_QUIZ_FIRST_ATTEMPT = "quiz_first_attempt";
    public static final String EVENT_NAME_QUIZ_ATTEMPT = "quiz_attempt";
    public static final String EVENT_NAME_QUIZ_FIRST_THRESHOLD = "quiz_first_attempt_threshold";
    public static final String EVENT_NAME_QUIZ_FIRST_BONUS = "quiz_first_attempt_bonus";
    public static final String EVENT_NAME_ACTIVITY_COMPLETED = "activity_completed";
    public static final String EVENT_NAME_MEDIA_STARTED = "media_started";
    public static final String EVENT_NAME_MEDIA_PLAYING_INTERVAL = "media_playing_interval";
    public static final String EVENT_NAME_MEDIA_PLAYING_POINTS_PER_INTERVAL = "media_playing_points_per_interval";
    public static final String EVENT_NAME_MEDIA_MAX_POINTS = "media_max_points";
    public static final String EVENT_NAME_COURSE_DOWNLOADED = "course_downloaded";


    // default points for gamification
    public static final GamificationEvent GAMIFICATION_REGISTER = new GamificationEvent(EVENT_NAME_REGISTER,100);
    public static final GamificationEvent GAMIFICATION_QUIZ_FIRST_ATTEMPT =  new GamificationEvent(EVENT_NAME_QUIZ_FIRST_ATTEMPT,20);
    public static final GamificationEvent GAMIFICATION_QUIZ_ATTEMPT =  new GamificationEvent(EVENT_NAME_QUIZ_ATTEMPT,10);
    public static final GamificationEvent GAMIFICATION_QUIZ_FIRST_ATTEMPT_THRESHOLD =  new GamificationEvent(EVENT_NAME_QUIZ_FIRST_THRESHOLD,100);
    public static final GamificationEvent GAMIFICATION_QUIZ_FIRST_ATTEMPT_BONUS =  new GamificationEvent(EVENT_NAME_QUIZ_FIRST_BONUS,50);
    public static final GamificationEvent GAMIFICATION_ACTIVITY_COMPLETED =  new GamificationEvent(EVENT_NAME_ACTIVITY_COMPLETED,10);
    public static final GamificationEvent GAMIFICATION_MEDIA_STARTED =  new GamificationEvent(EVENT_NAME_MEDIA_STARTED,20);
    public static final GamificationEvent GAMIFICATION_MEDIA_PLAYING_INTERVAL =  new GamificationEvent(EVENT_NAME_MEDIA_PLAYING_INTERVAL,30);
    public static final GamificationEvent GAMIFICATION_MEDIA_PLAYING_POINTS_PER_INTERVAL =  new GamificationEvent(EVENT_NAME_MEDIA_PLAYING_POINTS_PER_INTERVAL,10);
    public static final GamificationEvent GAMIFICATION_MEDIA_MAX_POINTS =  new GamificationEvent(EVENT_NAME_MEDIA_MAX_POINTS,200);
    public static final GamificationEvent GAMIFICATION_COURSE_DOWNLOADED =  new GamificationEvent(EVENT_NAME_COURSE_DOWNLOADED,50);
}
