package org.digitalcampus.oppia.gamification;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.digitalcampus.mobile.quiz.Quiz;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.application.Tracker;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.GamificationEvent;
import org.digitalcampus.oppia.model.QuizAttempt;
import org.digitalcampus.oppia.utils.MetaDataUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class GamificationService  extends IntentService {

    public static final String TAG = GamificationService.class.getSimpleName();
    public static final String BROADCAST_ACTION = "com.digitalcampus.oppia.GAMIFICATIONSERVICE";

    public static final String SERVICE_COURSE = "course";
    public static final String SERVICE_QUIZ = "quiz";
    public static final String SERVICE_ACTIVITY = "activity";
    public static final String SERVICE_EVENT = "event";
    public static final String SERVICE_MESSAGE = "message";
    public static final String SERVICE_POINTS = "points";
    public static final String SERVICE_QUIZ_SCORE = "quiz_score";

    public static final String SERVICE_EVENT_ACTIVITY = "activity_completed";
    public static final String SERVICE_EVENT_QUIZ = "quiz_attempt";
    public static final String SERVICE_EVENT_DOWNLOAD = "course_downloaded";
    public static final String SERVICE_EVENT_RESOURCE = "resource_completed";
    public static final String SERVICE_EVENT_FEEDBACK = "feedback";
    public static final String SERVICE_EVENT_MEDIAPLAYBACK = "media_playback";

    public static final String EVENTDATA_IS_BASELINE = "data_is_baseline";
    public static final String EVENTDATA_IS_COMPLETED = "data_is_completed";
    public static final String EVENTDATA_TIMETAKEN = "data_timetaken";
    public static final String EVENTDATA_READALOUD = "data_readaloud";
    public static final String EVENTDATA_QUIZID = "data_quiz_id";
    public static final String EVENTDATA_INSTANCEID = "data_instance_id";
    public static final String EVENTDATA_MEDIA_FILENAME = "data_media_filename";

    private SharedPreferences prefs;
    private GamificationEngine gEngine;

    public GamificationService() { super(TAG); }

    @Override
    public void onCreate(){
        super.onCreate();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        gEngine = new GamificationEngine(this);

    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent == null) return;
        try {

            if (intent.hasExtra (SERVICE_EVENT)) {
                String eventName = intent.getStringExtra(SERVICE_EVENT);
                boolean isCompleted = intent.getBooleanExtra(EVENTDATA_IS_COMPLETED, false);
                boolean isBaseline = intent.getBooleanExtra(EVENTDATA_IS_BASELINE, false);

                JSONObject eventData = new JSONObject();

                MetaDataUtils mdu = new MetaDataUtils(this);
                eventData = mdu.getMetaData(eventData);
                String lang = prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage());
                eventData.put("lang", lang);

                GamificationEvent event = null;
                Course c = null;
                Activity act = null;


                if (SERVICE_EVENT_DOWNLOAD.equals(eventName)){
                    c = (Course) intent.getSerializableExtra(SERVICE_COURSE);
                    event = gEngine.processEventCourseDownloaded(c);
                    isCompleted = true;
                }
                else if (SERVICE_EVENT_ACTIVITY.equals(eventName)){
                    if (isCompleted){
                        act = (Activity) intent.getSerializableExtra(SERVICE_ACTIVITY);
                        c = (Course) intent.getSerializableExtra(SERVICE_COURSE);
                        event = gEngine.processEventActivityCompleted(c, act);

                        eventData.put("timetaken", intent.getLongExtra(EVENTDATA_TIMETAKEN, 0));
                        eventData.put("readaloud", intent.getBooleanExtra(EVENTDATA_READALOUD, false));
                    }
                }
                else if (SERVICE_EVENT_QUIZ.equals(eventName)){
                    act = (Activity) intent.getSerializableExtra(SERVICE_ACTIVITY);
                    c = (Course) intent.getSerializableExtra(SERVICE_COURSE);
                    Quiz quiz = (Quiz) intent.getSerializableExtra(SERVICE_QUIZ);
                    float score = intent.getFloatExtra(SERVICE_QUIZ_SCORE, 0f);
                    event = gEngine.processEventQuizAttempt(c, act, quiz, score);

                    DbHelper db = DbHelper.getInstance(this);
                    long userId = db.getUserId(SessionManager.getUsername(this));

                    Log.d(TAG,"quiz points:" + String.valueOf(event.getPoints()));
                    // save results ready to send back to the quiz server
                    String data = quiz.getResultObject(event).toString();
                    Log.d(TAG,data);

                    QuizAttempt qa = new QuizAttempt();
                    qa.setCourseId(c.getCourseId());
                    qa.setUserId(userId);
                    qa.setData(data);
                    qa.setActivityDigest(act.getDigest());
                    qa.setScore(quiz.getUserscore());
                    qa.setMaxscore(quiz.getMaxscore());
                    qa.setPassed(isCompleted);
                    qa.setSent(false);
                    qa.setEvent(event.getEvent());
                    //don't save points twice
                    //qa.setPoints(event.getPoints());
                    db.insertQuizAttempt(qa);

                    long now = System.currentTimeMillis()/1000;
                    prefs.edit().putLong(PrefsActivity.PREF_TRIGGER_POINTS_REFRESH, now).apply();

                    eventData.put("timetaken", intent.getLongExtra(EVENTDATA_TIMETAKEN, 0));
                    eventData.put("quiz_id", quiz.getID());
                    eventData.put("instance_id", quiz.getInstanceID());
                    eventData.put("score", score);

                }
                else if (SERVICE_EVENT_RESOURCE.equals(eventName)){
                    if (isCompleted){
                        act = (Activity) intent.getSerializableExtra(SERVICE_ACTIVITY);
                        c = (Course) intent.getSerializableExtra(SERVICE_COURSE);
                        event = gEngine.processEventResourceActivity(c, act);

                        eventData.put("timetaken", intent.getLongExtra(EVENTDATA_TIMETAKEN, 0));
                    }
                }
                else if (SERVICE_EVENT_FEEDBACK.equals(eventName)){
                    act = (Activity) intent.getSerializableExtra(SERVICE_ACTIVITY);
                    c = (Course) intent.getSerializableExtra(SERVICE_COURSE);
                    event = gEngine.processEventFeedbackActivity(c, act);

                    eventData.put("timetaken", intent.getLongExtra(EVENTDATA_TIMETAKEN, 0));
                    eventData.put("quiz_id", intent.getIntExtra(EVENTDATA_QUIZID, 0));
                    eventData.put("instance_id", intent.getStringExtra(EVENTDATA_INSTANCEID));
                }
                else if (SERVICE_EVENT_MEDIAPLAYBACK.equals(eventName)){
                    act = (Activity) intent.getSerializableExtra(SERVICE_ACTIVITY);
                    c = (Course) intent.getSerializableExtra(SERVICE_COURSE);
                    String filename = intent.getStringExtra(EVENTDATA_MEDIA_FILENAME);
                    long timetaken = intent.getLongExtra(EVENTDATA_TIMETAKEN, 0);

                    event = gEngine.processEventMediaPlayed(c, act, filename, timetaken);
                    eventData.put("timetaken", timetaken);
                    eventData.put("mediafile", filename);
                    eventData.put("media", "played");
                }

                if (event == null)
                    return;

                Tracker t = new Tracker(this);
                t.saveTracker(c.getCourseId(), act!=null ? act.getDigest() : "", eventData, event.isCompleted() || isCompleted || isBaseline, event);

                if (event.getPoints() > 0){
                    broadcastEvent(event, act, c);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void broadcastEvent(GamificationEvent event, Activity act, Course c) {
        String message = gEngine.getEventMessage(event, c, act);

        Intent localIntent = new Intent(BROADCAST_ACTION);
        localIntent.putExtra(SERVICE_MESSAGE, message);
        localIntent.putExtra(SERVICE_POINTS, event.getPoints());

        // Broadcasts the Intent to receivers in this app.
        Log.d(TAG, message);
        sendOrderedBroadcast(localIntent, null);
    }
}
