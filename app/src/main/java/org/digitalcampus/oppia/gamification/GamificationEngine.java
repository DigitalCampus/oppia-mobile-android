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

import org.digitalcampus.mobile.quiz.Quiz;
import org.digitalcampus.oppia.exception.GamificationEventNotFound;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.GamificationEvent;


/* This holds all the rules for allocating points */
public class GamificationEngine {

    /*
    This finds the level of points for the event that should be used, basic logic:
    does the activity have custom points for this event? - if yes, use these
    if not, does the course have custom points for this event? - if yes, use these
    if not, use the app level defaults
     */
    private GamificationEvent getEventFromHierarchy(Course course, Activity activity, String event) throws GamificationEventNotFound{
        // check if the activity has custom points for this event
        try{
            GamificationEvent ge = activity.findGamificationEvent(event);
            return ge;
        } catch (GamificationEventNotFound genf){
            // do nothing
        }

        // check if the course has custom points for this event
        try{
            GamificationEvent ge = course.findGamificationEvent(event);
            return ge;
        } catch (GamificationEventNotFound genf){
            // do nothing
        }

        // else use the the app level defaults
        DefaultGamification defaultGamification = new DefaultGamification();
        return defaultGamification.getEvent(event);

    }

    /*
    App level points/event only - shouldn't be added as custom points at activity or course level
     */
    public GamificationEvent processEventRegister(){
        return DefaultGamification.GAMIFICATION_REGISTER;
    }

    public GamificationEvent processEventQuizAttempt(Course course, Activity activity, Quiz quiz, float scorePercent){
        // TODO_GAMIFICATION
        return DefaultGamification.GAMIFICATION_UNDEFINED;
    }

    public GamificationEvent processEventActivityCompleted(Course course, Activity activity){
        try{
            return this.getEventFromHierarchy(course, activity, DefaultGamification.EVENT_NAME_ACTIVITY_COMPLETED);
        } catch (GamificationEventNotFound genf) {
            return DefaultGamification.GAMIFICATION_UNDEFINED;
        }
    }

    public GamificationEvent processEventMediaPlayed(Course course, Activity activity, long timeTaken){
        // TODO_GAMIFICATION
        return DefaultGamification.GAMIFICATION_UNDEFINED;
    }

    public GamificationEvent processEventCourseDownloaded(){
        // TODO_GAMIFICATION
        return DefaultGamification.GAMIFICATION_UNDEFINED;
    }

    public GamificationEvent processEventResourceActivity(Course course, Activity activity){
        // TODO_GAMIFICATION - add specific event for this - now just using default activity completed
        try{
            return this.getEventFromHierarchy(course, activity, DefaultGamification.EVENT_NAME_ACTIVITY_COMPLETED);
        } catch (GamificationEventNotFound genf) {
            return DefaultGamification.GAMIFICATION_UNDEFINED;
        }
    }

    public GamificationEvent processEventResourceStoppedActivity(Course course, Activity activity){
        // TODO_GAMIFICATION - add specific event for this - eg using time taken
        return DefaultGamification.GAMIFICATION_UNDEFINED;
    }

    public GamificationEvent processEventFeedbackActivity(Course course, Activity activity){
        // TODO_GAMIFICATION - add specific event for this - now just using default activity completed
        try{
            return this.getEventFromHierarchy(course, activity, DefaultGamification.EVENT_NAME_ACTIVITY_COMPLETED);
        } catch (GamificationEventNotFound genf) {
            return DefaultGamification.GAMIFICATION_UNDEFINED;
        }
    }

    public GamificationEvent processEventURLActivity(Course course, Activity activity){
        // TODO_GAMIFICATION - add specific event for this - now just using default activity completed
        try{
            return this.getEventFromHierarchy(course, activity, DefaultGamification.EVENT_NAME_ACTIVITY_COMPLETED);
        } catch (GamificationEventNotFound genf) {
            return DefaultGamification.GAMIFICATION_UNDEFINED;
        }
    }


}
