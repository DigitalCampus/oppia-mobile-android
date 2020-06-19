package org.digitalcampus.oppia.gamification;

import android.content.Context;
import android.content.Intent;

import org.digitalcampus.mobile.quiz.Quiz;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;

public class GamificationServiceDelegate {

    public static final String TAG = GamificationServiceDelegate.class.getSimpleName();
    private Intent serviceIntent;
    private Context ctx;

    public GamificationServiceDelegate(Context ctx){
        this.ctx = ctx;
    }


    public GamificationServiceDelegate createActivityIntent(
                                     Course c,
                                     Activity act,
                                     boolean isCompleted,
                                     boolean isBaseline){

        serviceIntent = new Intent(ctx, GamificationService.class);
        serviceIntent.putExtra(GamificationService.SERVICE_COURSE, c);
        serviceIntent.putExtra(GamificationService.SERVICE_ACTIVITY, act);
        serviceIntent.putExtra(GamificationService.EVENTDATA_IS_COMPLETED, isCompleted);
        serviceIntent.putExtra(GamificationService.EVENTDATA_IS_BASELINE, isBaseline);

        return this;
    }

    public void registerPageActivityEvent(long timetaken, boolean isReadAloud){

        if (serviceIntent == null) return;

        serviceIntent.putExtra(GamificationService.SERVICE_EVENT, GamificationService.SERVICE_EVENT_ACTIVITY);
        serviceIntent.putExtra(GamificationService.EVENTDATA_TIMETAKEN, timetaken);
        serviceIntent.putExtra(GamificationService.EVENTDATA_READALOUD, isReadAloud);
        ctx.startService(serviceIntent);

        serviceIntent = null;
    }

    public void registerCourseDownloadEvent(Course c){

        serviceIntent = new Intent(ctx, GamificationService.class);
        serviceIntent.putExtra(GamificationService.SERVICE_EVENT, GamificationService.SERVICE_EVENT_DOWNLOAD);
        serviceIntent.putExtra(GamificationService.SERVICE_COURSE, c);
        ctx.startService(serviceIntent);

    }

    public void registerQuizAttemptEvent(long timetaken, Quiz quiz, float score){

        if (serviceIntent == null) return;

        serviceIntent.putExtra(GamificationService.SERVICE_QUIZ, quiz);
        serviceIntent.putExtra(GamificationService.SERVICE_EVENT, GamificationService.SERVICE_EVENT_QUIZ);
        serviceIntent.putExtra(GamificationService.EVENTDATA_TIMETAKEN, timetaken);
        serviceIntent.putExtra(GamificationService.SERVICE_QUIZ_SCORE, score);
        ctx.startService(serviceIntent);

        serviceIntent = null;
    }

    public void registerResourceEvent(long timetaken) {

        if (serviceIntent == null) return;

        serviceIntent.putExtra(GamificationService.SERVICE_EVENT, GamificationService.SERVICE_EVENT_RESOURCE);
        serviceIntent.putExtra(GamificationService.EVENTDATA_TIMETAKEN, timetaken);

        ctx.startService(serviceIntent);

        serviceIntent = null;
    }

    public void registerFeedbackEvent(long timetaken, Quiz feedback, int quizId, String instanceId) {

        if (serviceIntent == null) return;

        serviceIntent.putExtra(GamificationService.SERVICE_QUIZ, feedback);
        serviceIntent.putExtra(GamificationService.SERVICE_EVENT, GamificationService.SERVICE_EVENT_FEEDBACK);
        serviceIntent.putExtra(GamificationService.EVENTDATA_TIMETAKEN, timetaken);
        serviceIntent.putExtra(GamificationService.EVENTDATA_QUIZID, quizId);
        serviceIntent.putExtra(GamificationService.EVENTDATA_INSTANCEID, instanceId);
        ctx.startService(serviceIntent);

        serviceIntent = null;

    }

    public void registerMediaPlaybackEvent(long timetaken, String mediaFileName){

        if (serviceIntent == null) return;

        serviceIntent.putExtra(GamificationService.SERVICE_EVENT, GamificationService.SERVICE_EVENT_MEDIAPLAYBACK);
        serviceIntent.putExtra(GamificationService.EVENTDATA_TIMETAKEN, timetaken);
        serviceIntent.putExtra(GamificationService.EVENTDATA_MEDIA_FILENAME, mediaFileName);
        ctx.startService(serviceIntent);

        serviceIntent = null;
    }
}
