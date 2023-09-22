package org.digitalcampus.oppia.gamification

import android.app.IntentService
import android.content.Intent
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.preference.PreferenceManager
import org.digitalcampus.mobile.quiz.Quiz
import org.digitalcampus.oppia.activity.PrefsActivity
import org.digitalcampus.oppia.analytics.Analytics.logException
import org.digitalcampus.oppia.application.SessionManager.getUsername
import org.digitalcampus.oppia.application.Tracker
import org.digitalcampus.oppia.database.DbHelper
import org.digitalcampus.oppia.model.Activity
import org.digitalcampus.oppia.model.Course
import org.digitalcampus.oppia.model.GamificationEvent
import org.digitalcampus.oppia.model.QuizAttempt
import org.digitalcampus.oppia.utils.MetaDataUtils
import org.json.JSONException
import java.util.Locale

class GamificationService : IntentService(TAG) {

    companion object {
        val TAG = GamificationService::class.simpleName
        const val BROADCAST_ACTION = "com.digitalcampus.oppia.GAMIFICATIONSERVICE"

        const val SERVICE_COURSE = "course"
        const val SERVICE_QUIZ = "quiz"
        const val SERVICE_ACTIVITY = "activity"
        const val SERVICE_EVENT = "event"
        const val SERVICE_MESSAGE = "message"
        const val SERVICE_POINTS = "points"
        const val SERVICE_QUIZ_SCORE = "quiz_score"
        const val SERVICE_EVENT_ACTIVITY = "activity_completed"
        const val SERVICE_EVENT_QUIZ = "quiz_attempt"
        const val SERVICE_EVENT_DOWNLOAD = "course_downloaded"
        const val SERVICE_EVENT_RESOURCE = "resource_completed"
        const val SERVICE_EVENT_FEEDBACK = "feedback"
        const val SERVICE_EVENT_MEDIAPLAYBACK = "media_playback"

        const val EVENTDATA_IS_BASELINE = "data_is_baseline"
        const val EVENTDATA_IS_COMPLETED = "data_is_completed"
        const val EVENTDATA_TIMETAKEN = "data_timetaken"
        const val EVENTDATA_READALOUD = "data_readaloud"
        const val EVENTDATA_QUIZID = "data_quiz_id"
        const val EVENTDATA_INSTANCEID = "data_instance_id"
        const val EVENTDATA_MEDIA_FILENAME = "data_media_filename"
        const val EVENTDATA_MEDIA_END_REACHED = "data_end_reached"
        const val EVENTDATA_EXTRA = "data_extra"

        private const val LOGDATA_LANG = "lang"
        private const val LOGDATA_READALOUD = "readaloud"
        private const val LOGDATA_TIMETAKEN = "timetaken"
        private const val LOGDATA_QUIZ_ID = "quiz_id"
        private const val LOGDATA_INSTANCE = "instance_id"
        private const val LOGDATA_SCORE = "score"
        private const val LOGDATA_MEDIAFILE = "mediafile"
        private const val LOGDATA_MEDIA_EVENT = "media"
        private const val LOGDATA_MEDIA_ENDREACHED = "media_end_reached"
    }

    private lateinit var prefs: SharedPreferences
    private lateinit var gEngine: GamificationEngine

    override fun onCreate() {
        super.onCreate()
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        gEngine = GamificationEngine(this)
    }

    override fun onHandleIntent(intent: Intent?) {
        if (intent == null) return
        try {
            if (intent.hasExtra(SERVICE_EVENT)) {
                val eventName = intent.getStringExtra(SERVICE_EVENT)
                var isCompleted = intent.getBooleanExtra(EVENTDATA_IS_COMPLETED, false)
                val isBaseline = intent.getBooleanExtra(EVENTDATA_IS_BASELINE, false)
                val eventData = MetaDataUtils(this).metaData
                val lang = prefs.getString(PrefsActivity.PREF_CONTENT_LANGUAGE, Locale.getDefault().language)
                eventData.put(LOGDATA_LANG, lang)
                var event: GamificationEvent? = null
                var course: Course? = null
                var act: Activity? = null
                var trackerDigest: String? = ""
                var type = ""
                if (SERVICE_EVENT_DOWNLOAD == eventName) {
                    course = intent.getSerializableExtra(SERVICE_COURSE) as Course?
                    event = gEngine.processEventCourseDownloaded(course)
                    isCompleted = true
                    type = Tracker.DOWNLOAD_TYPE
                } else if (SERVICE_EVENT_ACTIVITY == eventName) {
                    if (isCompleted) {
                        act = intent.getSerializableExtra(SERVICE_ACTIVITY) as Activity
                        course = intent.getSerializableExtra(SERVICE_COURSE) as Course
                        event = gEngine.processEventActivityCompleted(course, act)
                        eventData.put(LOGDATA_TIMETAKEN, intent.getLongExtra(EVENTDATA_TIMETAKEN, 0))
                        eventData.put(LOGDATA_READALOUD, intent.getBooleanExtra(EVENTDATA_READALOUD, false))
                        trackerDigest = act.digest
                    }
                } else if (SERVICE_EVENT_QUIZ == eventName || SERVICE_EVENT_FEEDBACK == eventName) {
                    act = intent.getSerializableExtra(SERVICE_ACTIVITY) as Activity
                    course = intent.getSerializableExtra(SERVICE_COURSE) as Course
                    val quiz = intent.getSerializableExtra(SERVICE_QUIZ) as Quiz
                    val qa = QuizAttempt()
                    val db = DbHelper.getInstance(this)
                    val timetaken = intent.getLongExtra(EVENTDATA_TIMETAKEN, 0)
                    val userId = db.getUserId(getUsername(this))
                    qa.courseId = course.courseId.toLong()
                    qa.userId = userId
                    qa.activityDigest = act.digest
                    qa.isPassed = isCompleted
                    qa.timetaken = timetaken
                    if (SERVICE_EVENT_QUIZ == eventName) {
                        val score = intent.getFloatExtra(SERVICE_QUIZ_SCORE, 0f)
                        event = gEngine.processEventQuizAttempt(course, act, score)
                        eventData.put(LOGDATA_SCORE, score.toDouble())
                        qa.type = QuizAttempt.TYPE_QUIZ
                        qa.score = quiz.userscore
                        qa.maxscore = quiz.maxscore
                    } else { // FEEDBACK
                        event = gEngine.processEventFeedbackActivity(course, act)
                        qa.type = QuizAttempt.TYPE_FEEDBACK
                    }
                    Log.d(TAG, "quiz points:" + event.points)
                    // save results ready to send back to the quiz server
                    val result = quiz.getResultObject(event)
                    result.put(LOGDATA_TIMETAKEN, timetaken)
                    qa.data = result.toString()
                    qa.isSent = false
                    qa.event = event.event
                    db.insertQuizAttempt(qa)
                    if (event.points > 0) {
                        val now = System.currentTimeMillis() / 1000
                        prefs.edit().putLong(PrefsActivity.PREF_TRIGGER_POINTS_REFRESH, now).apply()
                    }
                    eventData.put(LOGDATA_TIMETAKEN, timetaken)
                    eventData.put(LOGDATA_QUIZ_ID, quiz.id)
                    eventData.put(LOGDATA_INSTANCE, quiz.instanceID)
                    trackerDigest = act.digest
                } else if (SERVICE_EVENT_RESOURCE == eventName) {
                    if (isCompleted) {
                        act = intent.getSerializableExtra(SERVICE_ACTIVITY) as Activity
                        course = intent.getSerializableExtra(SERVICE_COURSE) as Course
                        event = gEngine.processEventResourceActivity(course, act)
                        eventData.put(LOGDATA_TIMETAKEN, intent.getLongExtra(EVENTDATA_TIMETAKEN, 0))
                        trackerDigest = act.digest
                    }
                } else if (SERVICE_EVENT_MEDIAPLAYBACK == eventName) {
                    act = intent.getSerializableExtra(SERVICE_ACTIVITY) as Activity
                    course = intent.getSerializableExtra(SERVICE_COURSE) as Course
                    val filename = intent.getStringExtra(EVENTDATA_MEDIA_FILENAME)
                    val timetaken = intent.getLongExtra(EVENTDATA_TIMETAKEN, 0)
                    val endReached = intent.getBooleanExtra(EVENTDATA_MEDIA_END_REACHED, false)
                    event = gEngine.processEventMediaPlayed(
                        course,
                        act,
                        filename,
                        timetaken,
                        endReached
                    )
                    eventData.put(LOGDATA_TIMETAKEN, timetaken)
                    eventData.put(LOGDATA_MEDIAFILE, filename)
                    eventData.put(LOGDATA_MEDIA_EVENT, "played")
                    eventData.put(LOGDATA_MEDIA_ENDREACHED, endReached)
                    val m = act.getMedia(filename!!)
                    trackerDigest = if (m != null) m.digest else act.digest
                }
                if (event == null) return
                if (intent.hasExtra(EVENTDATA_EXTRA)) {
                    val extraData =
                        intent.getSerializableExtra(EVENTDATA_EXTRA) as HashMap<String, String>?
                    for (key in extraData!!.keys) {
                        if (!eventData.has(key)) {
                            eventData.put(key, extraData[key])
                        }
                    }
                }
                val t = Tracker(this)
                t.saveTracker(
                    course!!.courseId,
                    trackerDigest,
                    eventData,
                    type,
                    event.isCompleted || isCompleted || isBaseline,
                    event
                )
                if (event.points > 0) {
                    if (SERVICE_EVENT_MEDIAPLAYBACK == eventName) {
                        // We add some delay to broadcast the media event as it can get lost while destroying the VideoPlayer activity
                        val e: GamificationEvent = event
                        val a = act
                        val c = course
                        Handler(Looper.getMainLooper()).postDelayed(
                            { broadcastEvent(e, a, c) },
                            500
                        )
                    } else {
                        broadcastEvent(event, act, course)
                    }
                }
            }
        }
        catch (e: JSONException) {
            logException(e)
            Log.d(TAG, "JSON error: ", e)
        }
    }

    private fun broadcastEvent(event: GamificationEvent, act: Activity?, c: Course?) {
        val message = gEngine.getEventMessage(event, c, act)
        val localIntent = Intent(BROADCAST_ACTION)
        localIntent.putExtra(SERVICE_MESSAGE, message)
        localIntent.putExtra(SERVICE_POINTS, event.points)

        // Broadcasts the Intent to receivers in this app.
        Log.d(TAG, message)
        sendOrderedBroadcast(localIntent, null)
    }
}