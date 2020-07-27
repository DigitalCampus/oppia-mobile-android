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

import com.splunk.mint.Mint;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.exception.GamificationEventNotFound;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.GamificationEvent;
import org.digitalcampus.oppia.model.Media;

import java.util.Locale;


/* This holds all the rules for allocating points */
public class GamificationEngine {

    public static final String TAG = GamificationEngine.class.getSimpleName();
    private static final String LOG_EVENT_NOT_FOUND ="Cannot find gamification event: ";

    private Context ctx;

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

        if (activity != null){
            // check if the activity has custom points for this event
            try{
                return activity.findGamificationEvent(event);
            } catch (GamificationEventNotFound genf){
                // do nothing
            }
        }
        if (course != null){
            // check if the course has custom points for this event
            try{
                return course.findGamificationEvent(event);
            } catch (GamificationEventNotFound genf){
                // do nothing
            }
        }
        // else use the the app level defaults
        Gamification defaultGamification = new Gamification();
        return defaultGamification.getEvent(event);
    }

    private String getMediaCompletionCriteriaFromHierarchy(){
        return Gamification.DEFAULT_MEDIA_CRITERIA;
    }

    private int getMediaCompletionThresholdFromHierarchy() {
        return Gamification.DEFAULT_MEDIA_THRESHOLD;
    }

    private GamificationEvent getEventFromCourseHierarchy(Course course, String event) throws GamificationEventNotFound{

        if (course != null){
            // check if the course has custom points for this event
            try{
                return course.findGamificationEvent(event);
            } catch (GamificationEventNotFound genf){
                // do nothing
            }
        }

        // else use the the app level defaults
        Gamification defaultGamification = new Gamification();
        Log.d(TAG, event + ": using global event definition");
        return defaultGamification.getEvent(event);

    }

    /*
    App level points/event only - shouldn't be added as custom points at activity or course level
     */
    public GamificationEvent processEventRegister(){
        return Gamification.GAMIFICATION_REGISTER;
    }

    public GamificationEvent processEventQuizAttempt(Course course, Activity activity, float scorePercent){
        int totalPoints = 0;
        DbHelper db = DbHelper.getInstance(this.ctx);

        // is it the first attempt at this quiz?
        if(db.isQuizFirstAttempt(activity.getDigest())){
            try {
                totalPoints += this.getEventFromHierarchy(course, activity, Gamification.EVENT_NAME_QUIZ_FIRST_ATTEMPT).getPoints();

                if (scorePercent > 0) {
                    totalPoints += Math.round(scorePercent);
                }
                // on first attempt add the percent points
                if (Math.round(scorePercent) >= this.getEventFromHierarchy(course,activity,Gamification.EVENT_NAME_QUIZ_FIRST_THRESHOLD).getPoints()){
                    totalPoints += this.getEventFromHierarchy(course,activity,Gamification.EVENT_NAME_QUIZ_FIRST_BONUS).getPoints();
                }

            } catch (GamificationEventNotFound genf){
                Log.d(TAG,LOG_EVENT_NOT_FOUND + genf.getEventName(), genf);
                Mint.logException(genf);
            }

        } else if (db.isQuizFirstAttemptToday(activity.getDigest())){
            try {
                totalPoints += this.getEventFromHierarchy(course, activity, Gamification.EVENT_NAME_QUIZ_ATTEMPT).getPoints();
            } catch (GamificationEventNotFound genf){
                Log.d(TAG,LOG_EVENT_NOT_FOUND + genf.getEventName(), genf);
                Mint.logException(genf);
            }
        } else {
            Log.d(TAG,"Not first attempt, nor first attempt today");
        }

        return new GamificationEvent(Gamification.EVENT_NAME_QUIZ_ATTEMPT, totalPoints);
    }

    public GamificationEvent processEventActivityCompleted(Course course, Activity activity){
        int totalPoints = 0;
        DbHelper db = DbHelper.getInstance(this.ctx);

        if(db.isActivityFirstAttemptToday(activity.getDigest())){
            try{
                totalPoints += this.getEventFromHierarchy(course, activity, Gamification.EVENT_NAME_ACTIVITY_COMPLETED).getPoints();
            } catch (GamificationEventNotFound genf) {
                Log.d(TAG,LOG_EVENT_NOT_FOUND + Gamification.EVENT_NAME_ACTIVITY_COMPLETED, genf);
                Mint.logException(genf);
            }
        }
        return new GamificationEvent(Gamification.EVENT_NAME_ACTIVITY_COMPLETED, totalPoints);
    }

    public GamificationEvent processEventMediaPlayed(Course course, Activity activity, String mediaFileName, long timeTaken){
        int totalPoints = 0;
        boolean completed = false;
        DbHelper db = DbHelper.getInstance(this.ctx);

        Media m = activity.getMedia(mediaFileName);
        if (m != null) {
            try {
                String criteria = getMediaCompletionCriteriaFromHierarchy();
                int threshold = getMediaCompletionThresholdFromHierarchy();
                Log.d(TAG, "Video criteria: " + criteria);
                if (criteria.equals(Gamification.MEDIA_CRITERIA_THRESHOLD) && ((timeTaken * 100 / m.getLength()) > threshold)){

                    Log.d(TAG, "Threshold: " + threshold);
                    completed = true;
                    Log.d(TAG, "Threshold passed!");
                    if (!db.isMediaPlayed(activity.getDigest())){
                        Log.d(TAG, "First view --> giving points");
                        totalPoints += this.getEventFromHierarchy(course, activity, Gamification.EVENT_NAME_MEDIA_THRESHOLD_PASSED).getPoints();
                    }
                }
                else{
                    //Using intervals media criteria
                    // add points if first attempt today
                    if (db.isActivityFirstAttemptToday(activity.getDigest())){
                        totalPoints += this.getEventFromHierarchy(course, activity, Gamification.EVENT_NAME_MEDIA_STARTED).getPoints();
                    }
                    // add points for length of time the media has been playing
                    totalPoints += (int)(this.getEventFromHierarchy(course,activity, Gamification.EVENT_NAME_MEDIA_PLAYING_POINTS_PER_INTERVAL).getPoints() * Math.floor((double)timeTaken / (double)this.getEventFromHierarchy(course,activity, Gamification.EVENT_NAME_MEDIA_PLAYING_INTERVAL).getPoints()));

                    // make sure can't exceed max points
                    totalPoints = Math.min(totalPoints, getEventFromHierarchy(course,activity,
                            Gamification.EVENT_NAME_MEDIA_MAX_POINTS).getPoints());
                }


            } catch (GamificationEventNotFound genf) {
                Log.d(TAG,LOG_EVENT_NOT_FOUND + genf.getEventName(), genf);
                Mint.logException(genf);
            }
        }
        return new GamificationEvent(Gamification.EVENT_NAME_MEDIA_PLAYED, totalPoints, completed);
    }

    // needs to be able to access the course object at the point the tracker is triggered
    public GamificationEvent processEventCourseDownloaded(){
        return Gamification.GAMIFICATION_COURSE_DOWNLOADED;
    }

    public GamificationEvent processEventCourseDownloaded(Course course){
        int totalPoints = 0;
        try{
            totalPoints = this.getEventFromCourseHierarchy(course, Gamification.EVENT_NAME_COURSE_DOWNLOADED).getPoints();
        } catch (GamificationEventNotFound genf) {
            //do nothing
        }

        return new GamificationEvent(Gamification.EVENT_NAME_COURSE_DOWNLOADED, totalPoints);
    }

    public GamificationEvent processEventResourceActivity(Course course, Activity activity){
        try{
            return this.getEventFromHierarchy(course, activity, Gamification.EVENT_NAME_ACTIVITY_COMPLETED);
        } catch (GamificationEventNotFound genf) {
            return Gamification.GAMIFICATION_UNDEFINED;
        }
    }

    public GamificationEvent processEventResourceStoppedActivity(){
        return Gamification.GAMIFICATION_UNDEFINED;
    }

    public GamificationEvent processEventFeedbackActivity(Course course, Activity activity){
        DbHelper db = DbHelper.getInstance(this.ctx);
        int totalPoints = 0;
        if(db.isQuizFirstAttempt(activity.getDigest())) {
            try {
                totalPoints += this.getEventFromHierarchy(course, activity, Gamification.EVENT_NAME_FEEDBACK_COMPLETED).getPoints();
            } catch (GamificationEventNotFound genf) {
                Log.d(TAG, LOG_EVENT_NOT_FOUND + genf.getEventName(), genf);
                Mint.logException(genf);
            }
        }
        else {
            Log.d(TAG,"Not first attempt, nor first attempt today");
        }

        return new GamificationEvent(Gamification.EVENT_NAME_FEEDBACK_COMPLETED, totalPoints);
    }

    public GamificationEvent processEventURLActivity(Course course, Activity activity){
        try{
            return this.getEventFromHierarchy(course, activity, Gamification.EVENT_NAME_ACTIVITY_COMPLETED);
        } catch (GamificationEventNotFound genf) {
            return Gamification.GAMIFICATION_UNDEFINED;
        }
    }

    public String getEventMessage(GamificationEvent event, Course c, Activity a){
        String prefLang = Locale.getDefault().getLanguage();
        int resId = ctx.getResources().getIdentifier("points_event_" + event.getEvent(), "string", ctx.getPackageName());

        if (resId <= 0){
            //The string resource for that event was not found
            return (c == null) ? ctx.getString(R.string.points_event_default) : c.getTitle(prefLang);
        }
        if (a != null){
            return ctx.getString(resId,
                (c == null) ? "" : c.getTitle(prefLang),
                a.getTitle(prefLang) );
        }
        else{
            return ctx.getString(resId,
                (c == null) ? "" : c.getTitle(prefLang));
        }
    }
}
