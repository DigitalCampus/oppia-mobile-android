package org.digitalcampus.oppia.gamification;

import android.content.Context;
import android.content.Intent;

import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;

public class GamificationServiceDelegate {

    public void registerPageActivityEvent(Context context, Intent intent,
                                          Course c,
                                          Activity act,
                                          long timetaken,
                                          boolean isReadAloud
                                          ){
        intent.putExtra(GamificationService.SERVICE_EVENT, GamificationService.SERVICE_EVENT_ACTIVITY);
        intent.putExtra(GamificationService.SERVICE_COURSE, c);
        intent.putExtra(GamificationService.SERVICE_ACTIVITY, act);
        intent.putExtra(GamificationService.EVENTDATA_TIMETAKEN, timetaken);
        intent.putExtra(GamificationService.EVENTDATA_READALOUD, isReadAloud);
        context.startService(intent);
    }

}
