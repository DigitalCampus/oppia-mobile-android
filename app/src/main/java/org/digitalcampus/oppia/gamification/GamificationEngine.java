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
    private GamificationEvent getEventFromHierarchy(String event, Course course, Activity activity){
        // check if the activity has custom points for this event

        // check if the course has custom points for this event

        // else use the the app level defaults



    }

    /*
    App level points/event only - shouldn't be added as custom points at activity or course level
     */
    public GamificationEvent processEventRegister(){
        return DefaultGamification.GAMIFICATION_REGISTER;
    }

    public GamificationEvent processEventQuizAttempt(){
        GamificationEvent ge = new GamificationEvent();
        // TODO_GAMIFICATION
        return ge;
    }

    public GamificationEvent processEventActivityCompleted(){
        GamificationEvent ge = new GamificationEvent();
        // TODO_GAMIFICATION
        return ge;
    }

    public GamificationEvent processEventMediaPlayed(){
        GamificationEvent ge = new GamificationEvent();
        // TODO_GAMIFICATION
        return ge;
    }

    public GamificationEvent processEventCourseDownloaded(){
        GamificationEvent ge = new GamificationEvent();
        // TODO_GAMIFICATION
        return ge;
    }


}
