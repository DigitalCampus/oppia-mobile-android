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

import org.digitalcampus.mobile.learning.BuildConfig;
import org.digitalcampus.oppia.exception.GamificationEventNotFound;
import org.digitalcampus.oppia.model.GamificationEvent;

import java.util.ArrayList;

public class Gamification {

    // event names
    public static final String EVENT_NAME_REGISTER = "register";
    public static final String EVENT_NAME_QUIZ_FIRST_ATTEMPT = "quiz_first_attempt";
    public static final String EVENT_NAME_QUIZ_ATTEMPT = "quiz_attempt";
    public static final String EVENT_NAME_QUIZ_FIRST_THRESHOLD = "quiz_first_attempt_threshold";
    public static final String EVENT_NAME_QUIZ_FIRST_BONUS = "quiz_first_attempt_bonus";
    public static final String EVENT_NAME_ACTIVITY_COMPLETED = "activity_completed";
    public static final String EVENT_NAME_MEDIA_STARTED = "media_started";
    public static final String EVENT_NAME_MEDIA_PLAYED = "media_played";
    public static final String EVENT_NAME_MEDIA_PLAYING_INTERVAL = "media_playing_interval";
    public static final String EVENT_NAME_MEDIA_PLAYING_POINTS_PER_INTERVAL = "media_playing_points_per_interval";
    public static final String EVENT_NAME_MEDIA_MAX_POINTS = "media_max_points";
    public static final String EVENT_NAME_COURSE_DOWNLOADED = "course_downloaded";
    public static final String EVENT_NAME_SEARCH_PERFORMED = "search_performed";
    public static final String EVENT_NAME_MEDIA_MISSING = "media_missing";
    public static final String EVENT_NAME_MEDIA_THRESHOLD_PASSED = "media_threshold_passed";
    public static final String EVENT_NAME_FEEDBACK_COMPLETED = "feedback_completed";

    // default points for gamification
    public static final GamificationEvent GAMIFICATION_REGISTER = new GamificationEvent(EVENT_NAME_REGISTER,100);
    public static final GamificationEvent GAMIFICATION_QUIZ_FIRST_ATTEMPT =  new GamificationEvent(EVENT_NAME_QUIZ_FIRST_ATTEMPT,20);
    public static final GamificationEvent GAMIFICATION_QUIZ_FIRST_ATTEMPT_TODAY =  new GamificationEvent(EVENT_NAME_QUIZ_ATTEMPT,10);
    public static final GamificationEvent GAMIFICATION_QUIZ_ATTEMPT =  new GamificationEvent(EVENT_NAME_QUIZ_ATTEMPT,0);
    public static final GamificationEvent GAMIFICATION_QUIZ_FIRST_ATTEMPT_THRESHOLD =  new GamificationEvent(EVENT_NAME_QUIZ_FIRST_THRESHOLD,100);
    public static final GamificationEvent GAMIFICATION_QUIZ_FIRST_ATTEMPT_BONUS =  new GamificationEvent(EVENT_NAME_QUIZ_FIRST_BONUS,50);
    public static final GamificationEvent GAMIFICATION_ACTIVITY_COMPLETED =  new GamificationEvent(EVENT_NAME_ACTIVITY_COMPLETED,10);
    public static final GamificationEvent GAMIFICATION_MEDIA_STARTED =  new GamificationEvent(EVENT_NAME_MEDIA_STARTED,20);
    public static final GamificationEvent GAMIFICATION_MEDIA_PLAYING_INTERVAL =  new GamificationEvent(EVENT_NAME_MEDIA_PLAYING_INTERVAL,30);
    public static final GamificationEvent GAMIFICATION_MEDIA_PLAYING_POINTS_PER_INTERVAL =  new GamificationEvent(EVENT_NAME_MEDIA_PLAYING_POINTS_PER_INTERVAL,10);
    public static final GamificationEvent GAMIFICATION_MEDIA_MAX_POINTS =  new GamificationEvent(EVENT_NAME_MEDIA_MAX_POINTS,200);
    public static final GamificationEvent GAMIFICATION_COURSE_DOWNLOADED =  new GamificationEvent(EVENT_NAME_COURSE_DOWNLOADED,50);
    public static final GamificationEvent GAMIFICATION_SEARCH_PERFORMED =  new GamificationEvent(EVENT_NAME_SEARCH_PERFORMED,0);
    public static final GamificationEvent GAMIFICATION_MEDIA_MISSING =  new GamificationEvent(EVENT_NAME_MEDIA_MISSING,0);
    public static final GamificationEvent GAMIFICATION_MEDIA_THRESHOLD_PASSED =  new GamificationEvent(EVENT_NAME_MEDIA_THRESHOLD_PASSED,150);
    public static final GamificationEvent GAMIFICATION_FEEDBACK_COMPLETED =  new GamificationEvent(EVENT_NAME_FEEDBACK_COMPLETED,50);

    // Gamification points configuration
    public static final String GAMIFICATION_POINTS_ANIMATION = BuildConfig.GAMIFICATION_POINTS_ANIMATION;
    public static final int DURATION_GAMIFICATION_POINTS_VIEW = BuildConfig.DURATION_GAMIFICATION_POINTS_VIEW;

    public static final String MEDIA_CRITERIA_INTERVALS = "intervals";
    public static final String MEDIA_CRITERIA_THRESHOLD = "threshold";
    public static final String DEFAULT_MEDIA_CRITERIA = BuildConfig.GAMIFICATION_MEDIA_CRITERIA;
    public static final int DEFAULT_MEDIA_THRESHOLD = BuildConfig.GAMIFICATION_DEFAULT_MEDIA_THRESHOLD;

    //fallback option
    public static final String EVENT_NAME_UNDEFINED = "undefined";
    public static final GamificationEvent GAMIFICATION_UNDEFINED =  new GamificationEvent(EVENT_NAME_UNDEFINED,0);

    private ArrayList<GamificationEvent> events = new ArrayList<>();

    public Gamification(){
        events.add(GAMIFICATION_REGISTER);
        events.add(GAMIFICATION_QUIZ_FIRST_ATTEMPT);
        events.add(GAMIFICATION_QUIZ_FIRST_ATTEMPT_TODAY);
        events.add(GAMIFICATION_QUIZ_ATTEMPT);
        events.add(GAMIFICATION_QUIZ_FIRST_ATTEMPT_THRESHOLD);
        events.add(GAMIFICATION_QUIZ_FIRST_ATTEMPT_BONUS);
        events.add(GAMIFICATION_ACTIVITY_COMPLETED);
        events.add(GAMIFICATION_MEDIA_STARTED);
        events.add(GAMIFICATION_MEDIA_PLAYING_INTERVAL);
        events.add(GAMIFICATION_MEDIA_PLAYING_POINTS_PER_INTERVAL);
        events.add(GAMIFICATION_MEDIA_MAX_POINTS);
        events.add(GAMIFICATION_COURSE_DOWNLOADED);
        events.add(GAMIFICATION_SEARCH_PERFORMED);
        events.add(GAMIFICATION_MEDIA_MISSING);
        events.add(GAMIFICATION_MEDIA_THRESHOLD_PASSED);
        events.add(GAMIFICATION_FEEDBACK_COMPLETED);
    }

    public GamificationEvent getEvent(String event) throws GamificationEventNotFound {
        for(GamificationEvent ge: events){
            if(ge.getEvent().equals(event)){
                return ge;
            }
        }
        throw new GamificationEventNotFound(event);
    }
}
