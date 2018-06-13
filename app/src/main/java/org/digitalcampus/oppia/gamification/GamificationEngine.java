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

import android.content.Context;
import android.util.Log;

import org.digitalcampus.mobile.quiz.Quiz;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.exception.GamificationEventNotFound;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.GamificationEvent;
import org.digitalcampus.oppia.model.Media;


/* This holds all the rules for allocating points */
public class GamificationEngine {

    public static final String TAG = GamificationEngine.class.getSimpleName();
    private final Context ctx;

    public GamificationEngine(Context context){
        this.ctx = context;
    }
    /*
    This finds the level of points for the event that should be used, basic logic:
    does the activity have custom points for this event? - if yes, use these
    if not, does the course have custom points for this event? - if yes, use these
    if not, use the app level defaults
     */
    private GamificationEvent getEventFromHierarchy(Course course, Activity activity, String event) throws GamificationEventNotFound{
        // check if the activity has custom points for this event
        try{
            return activity.findGamificationEvent(event);
        } catch (GamificationEventNotFound genf){
            // do nothing
        }

        // check if the course has custom points for this event
        try{
            return course.findGamificationEvent(event);
        } catch (GamificationEventNotFound genf){
            // do nothing
        }

        // else use the the app level defaults
        Gamification defaultGamification = new Gamification();
        return defaultGamification.getEvent(event);

    }

    private GamificationEvent getEventFromCourseHierarchy(Course course, String event) throws GamificationEventNotFound{

        // check if the course has custom points for this event
        try{
            GamificationEvent ge = course.findGamificationEvent(event);
            return ge;
        } catch (GamificationEventNotFound genf){
            // do nothing
        }

        // else use the the app level defaults
        Gamification defaultGamification = new Gamification();
        return defaultGamification.getEvent(event);

    }

    /*
    App level points/event only - shouldn't be added as custom points at activity or course level
     */
    public GamificationEvent processEventRegister(){
        return Gamification.GAMIFICATION_REGISTER;
    }

    public GamificationEvent processEventQuizAttempt(Course course, Activity activity, Quiz quiz, float scorePercent){
        int totalPoints = 0;
        DbHelper db = DbHelper.getInstance(this.ctx);

        // is it the first attempt at this quiz?
        if(db.isQuizFirstAttempt(activity.getDigest())){
            try {
                totalPoints += this.getEventFromHierarchy(course, activity, Gamification.EVENT_NAME_QUIZ_FIRST_ATTEMPT).getPoints();
            } catch (GamificationEventNotFound gnef){
                Log.d(this.TAG,"EVENT_NAME_QUIZ_FIRST_ATTEMPT - event not found");
            }

            // on first attempt add the percent points
            if (scorePercent > 0) {
                totalPoints += Math.round(scorePercent);
            }

            // add bonus
            try {
                if (Math.round(scorePercent) >= this.getEventFromHierarchy(course,activity,Gamification.EVENT_NAME_QUIZ_FIRST_THRESHOLD).getPoints()){
                    totalPoints += this.getEventFromHierarchy(course,activity,Gamification.EVENT_NAME_QUIZ_FIRST_BONUS).getPoints();
                }
            } catch (GamificationEventNotFound gnef){
                Log.d(this.TAG,"EVENT_NAME_QUIZ_FIRST_THRESHOLD or EVENT_NAME_QUIZ_FIRST_THRESHOLD - event not found");
            }
        } else if (db.isQuizFirstAttemptToday(activity.getDigest())){
            try {
                totalPoints += this.getEventFromHierarchy(course, activity, Gamification.EVENT_NAME_QUIZ_ATTEMPT).getPoints();
            } catch (GamificationEventNotFound gnef){
                Log.d(this.TAG,"EVENT_NAME_QUIZ_ATTEMPT - event not found");
            }
        } else {
            Log.d(this.TAG,"Not first attempt, nor first attempt today");
        }

        GamificationEvent gamificationEvent = new GamificationEvent(Gamification.EVENT_NAME_QUIZ_ATTEMPT, totalPoints);

        return gamificationEvent;
    }

    public GamificationEvent processEventActivityCompleted(Course course, Activity activity){
        int totalPoints = 0;
        DbHelper db = DbHelper.getInstance(this.ctx);

        if(db.isActivityFirstAttemptToday(activity.getDigest())){
            try{
                totalPoints += this.getEventFromHierarchy(course, activity, Gamification.EVENT_NAME_ACTIVITY_COMPLETED).getPoints();
            } catch (GamificationEventNotFound genf) {
                //do nothing
            }
        }

        GamificationEvent gamificationEvent = new GamificationEvent(Gamification.EVENT_NAME_ACTIVITY_COMPLETED, totalPoints);
        return gamificationEvent;
    }

    public GamificationEvent processEventMediaPlayed(Course course, Activity activity, String mediaFileName, long timeTaken){
        // TODO_GAMIFICATION
        int totalPoints = 0;
        DbHelper db = DbHelper.getInstance(this.ctx);

        for (Media m : activity.getMedia()) {
            if (m.getFilename().equals(mediaFileName)) {
                // add points if first attempt today
                if (db.isActivityFirstAttemptToday(m.getDigest())){
                    try {
                        totalPoints += this.getEventFromHierarchy(course, activity, Gamification.EVENT_NAME_MEDIA_STARTED).getPoints();
                    } catch (GamificationEventNotFound genf) {
                        //do nothing
                    }
                }
                // add points for length of time the media has been playing
                try {
                    totalPoints += this.getEventFromHierarchy(course,activity, Gamification.EVENT_NAME_MEDIA_PLAYING_POINTS_PER_INTERVAL).getPoints() * Math.floor(timeTaken/this.getEventFromHierarchy(course,activity, Gamification.EVENT_NAME_MEDIA_PLAYING_INTERVAL).getPoints());
                } catch (GamificationEventNotFound genf) {
                    //do nothing
                }

                // make sure can't exceed max points
                try {
                    if (totalPoints >= this.getEventFromHierarchy(course,activity, Gamification.EVENT_NAME_MEDIA_MAX_POINTS).getPoints()){
                        totalPoints = this.getEventFromHierarchy(course,activity, Gamification.EVENT_NAME_MEDIA_MAX_POINTS).getPoints();
                    }
                } catch (GamificationEventNotFound genf) {
                    //do nothing
                }
            }
        }
        GamificationEvent gamificationEvent = new GamificationEvent(Gamification.EVENT_NAME_MEDIA_PLAYED, totalPoints);
        return gamificationEvent;
    }

    // TODO_GAMIFICATION - allow adding specific points for particular course
    // needs to be able to access the course object at the point the tracker is triggered
    public GamificationEvent processEventCourseDownloaded(){
        return Gamification.GAMIFICATION_COURSE_DOWNLOADED;
        /*
        try{
            return this.getEventFromCourseHierarchy(course, Gamification.EVENT_NAME_COURSE_DOWNLOADED);
        } catch (GamificationEventNotFound genf) {
            return Gamification.GAMIFICATION_UNDEFINED;
        }
        */
    }

    public GamificationEvent processEventResourceActivity(Course course, Activity activity){
        // TODO_GAMIFICATION - add specific event for this - now just using default activity completed
        try{
            return this.getEventFromHierarchy(course, activity, Gamification.EVENT_NAME_ACTIVITY_COMPLETED);
        } catch (GamificationEventNotFound genf) {
            return Gamification.GAMIFICATION_UNDEFINED;
        }
    }

    public GamificationEvent processEventResourceStoppedActivity(Course course, Activity activity){
        // TODO_GAMIFICATION - add specific event for this - eg using time taken
        return Gamification.GAMIFICATION_UNDEFINED;
    }

    public GamificationEvent processEventFeedbackActivity(Course course, Activity activity){
        // TODO_GAMIFICATION - add specific event for this - now just using default activity completed
        try{
            return this.getEventFromHierarchy(course, activity, Gamification.EVENT_NAME_ACTIVITY_COMPLETED);
        } catch (GamificationEventNotFound genf) {
            return Gamification.GAMIFICATION_UNDEFINED;
        }
    }

    public GamificationEvent processEventURLActivity(Course course, Activity activity){
        // TODO_GAMIFICATION - add specific event for this - now just using default activity completed
        try{
            return this.getEventFromHierarchy(course, activity, Gamification.EVENT_NAME_ACTIVITY_COMPLETED);
        } catch (GamificationEventNotFound genf) {
            return Gamification.GAMIFICATION_UNDEFINED;
        }
    }


}
