package org.digitalcampus.oppia.gamification;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.digitalcampus.mobile.quiz.Quiz;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.analytics.Analytics;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.application.Tracker;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.GamificationEvent;
import org.digitalcampus.oppia.model.Media;
import org.digitalcampus.oppia.model.QuizAttempt;
import org.digitalcampus.oppia.utils.MetaDataUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import androidx.preference.PreferenceManager;

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
    public static final String EVENTDATA_MEDIA_END_REACHED = "data_end_reached";

    private static final String LOGDATA_LANG = "lang";
    private static final String LOGDATA_READALOUD = "readaloud";
    private static final String LOGDATA_TIMETAKEN = "timetaken";
    private static final String LOGDATA_QUIZ_ID = "quiz_id";
    private static final String LOGDATA_INSTANCE = "instance_id";
    private static final String LOGDATA_SCORE = "score";
    private static final String LOGDATA_MEDIAFILE = "mediafile";
    private static final String LOGDATA_MEDIA_EVENT = "media";
    private static final String LOGDATA_MEDIA_ENDREACHED = "media_end_reached";

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

                JSONObject eventData =  new MetaDataUtils(this).getMetaData();
                String lang = prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage());
                eventData.put(LOGDATA_LANG, lang);

                GamificationEvent event = null;
                Course course = null;
                Activity act = null;
                String trackerDigest = "";

                if (SERVICE_EVENT_DOWNLOAD.equals(eventName)){
                    course = (Course) intent.getSerializableExtra(SERVICE_COURSE);
                    event = gEngine.processEventCourseDownloaded(course);
                    isCompleted = true;
                }
                else if (SERVICE_EVENT_ACTIVITY.equals(eventName)){
                    if (isCompleted){
                        act = (Activity) intent.getSerializableExtra(SERVICE_ACTIVITY);
                        course = (Course) intent.getSerializableExtra(SERVICE_COURSE);
                        event = gEngine.processEventActivityCompleted(course, act);

                        eventData.put(LOGDATA_TIMETAKEN, intent.getLongExtra(EVENTDATA_TIMETAKEN, 0));
                        eventData.put(LOGDATA_READALOUD, intent.getBooleanExtra(EVENTDATA_READALOUD, false));

                        trackerDigest = act.getDigest();
                    }
                }
                else if (SERVICE_EVENT_QUIZ.equals(eventName) || SERVICE_EVENT_FEEDBACK.equals(eventName)){
                    act = (Activity) intent.getSerializableExtra(SERVICE_ACTIVITY);
                    course = (Course) intent.getSerializableExtra(SERVICE_COURSE);
                    Quiz quiz = (Quiz) intent.getSerializableExtra(SERVICE_QUIZ);
                    QuizAttempt qa = new QuizAttempt();
                    DbHelper db = DbHelper.getInstance(this);

                    long timetaken = intent.getLongExtra(EVENTDATA_TIMETAKEN, 0);
                    long userId = db.getUserId(SessionManager.getUsername(this));

                    qa.setCourseId(course.getCourseId());
                    qa.setUserId(userId);
                    qa.setActivityDigest(act.getDigest());
                    qa.setPassed(isCompleted);
                    qa.setTimetaken(timetaken);

                    if (SERVICE_EVENT_QUIZ.equals(eventName)){
                        float score = intent.getFloatExtra(SERVICE_QUIZ_SCORE, 0f);
                        event = gEngine.processEventQuizAttempt(course, act, score);
                        eventData.put(LOGDATA_SCORE, score);
                        qa.setType(QuizAttempt.TYPE_QUIZ);
                        qa.setScore(quiz.getUserscore());
                        qa.setMaxscore(quiz.getMaxscore());
                    }
                    else { // FEEDBACK
                        event = gEngine.processEventFeedbackActivity(course, act);
                        qa.setType(QuizAttempt.TYPE_FEEDBACK);
                    }

                    Log.d(TAG,"quiz points:" + event.getPoints());
                    // save results ready to send back to the quiz server
                    JSONObject result = quiz.getResultObject(event);
                    result.put(LOGDATA_TIMETAKEN, timetaken);
                    qa.setData(result.toString());

                    qa.setSent(false);
                    qa.setEvent(event.getEvent());
                    db.insertQuizAttempt(qa);

                    if (event.getPoints() > 0){
                        long now = System.currentTimeMillis()/1000;
                        prefs.edit().putLong(PrefsActivity.PREF_TRIGGER_POINTS_REFRESH, now).apply();
                    }

                    eventData.put(LOGDATA_TIMETAKEN, timetaken);
                    eventData.put(LOGDATA_QUIZ_ID, quiz.getID());
                    eventData.put(LOGDATA_INSTANCE, quiz.getInstanceID());
                    trackerDigest = act.getDigest();

                }
                else if (SERVICE_EVENT_RESOURCE.equals(eventName)){
                    if (isCompleted){
                        act = (Activity) intent.getSerializableExtra(SERVICE_ACTIVITY);
                        course = (Course) intent.getSerializableExtra(SERVICE_COURSE);
                        event = gEngine.processEventResourceActivity(course, act);

                        eventData.put(LOGDATA_TIMETAKEN, intent.getLongExtra(EVENTDATA_TIMETAKEN, 0));

                        trackerDigest = act.getDigest();
                    }
                }
                else if (SERVICE_EVENT_MEDIAPLAYBACK.equals(eventName)){
                    act = (Activity) intent.getSerializableExtra(SERVICE_ACTIVITY);
                    course = (Course) intent.getSerializableExtra(SERVICE_COURSE);
                    String filename = intent.getStringExtra(EVENTDATA_MEDIA_FILENAME);
                    long timetaken = intent.getLongExtra(EVENTDATA_TIMETAKEN, 0);
                    boolean endReached = intent.getBooleanExtra(EVENTDATA_MEDIA_END_REACHED, false);

                    event = gEngine.processEventMediaPlayed(course, act, filename, timetaken, endReached);
                    eventData.put(LOGDATA_TIMETAKEN, timetaken);
                    eventData.put(LOGDATA_MEDIAFILE, filename);
                    eventData.put(LOGDATA_MEDIA_EVENT, "played");
                    eventData.put(LOGDATA_MEDIA_ENDREACHED, endReached);

                    Media m = act.getMedia(filename);
                    trackerDigest = (m != null) ? m.getDigest() : act.getDigest();
                }

                if (event == null)
                    return;

                Tracker t = new Tracker(this);
                t.saveTracker(course.getCourseId(), trackerDigest, eventData, event.isCompleted() || isCompleted || isBaseline, event);

                if (event.getPoints() > 0){

                    if (SERVICE_EVENT_MEDIAPLAYBACK.equals(eventName)){
                        // We add some delay to broadcast the media event as it can get lost while destroying the VideoPlayer activity
                        final GamificationEvent e = event;
                        final Activity a = act;
                        final Course c = course;
                        new Handler(Looper.getMainLooper()).postDelayed(() ->
                                broadcastEvent(e, a, c), 500);
                    }
                    else{
                        broadcastEvent(event, act, course);
                    }
                    
                }
            }
        } catch (JSONException e) {
            Analytics.logException(e);
            Log.d(TAG, "JSON error: ", e);
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
