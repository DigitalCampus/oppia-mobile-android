package org.digitalcampus.oppia.gamification

import android.content.Context
import android.content.Intent
import org.digitalcampus.mobile.quiz.Quiz
import org.digitalcampus.oppia.gamification.GamificationService
import org.digitalcampus.oppia.model.Activity
import org.digitalcampus.oppia.model.Course

class GamificationServiceDelegate(private val ctx: Context) {

    val TAG = GamificationServiceDelegate::class.simpleName
    private var serviceIntent: Intent? = null
    private val extraData = HashMap<String, String>()

    fun createActivityIntent(c: Course, act: Activity?, isCompleted: Boolean, isBaseline: Boolean): GamificationServiceDelegate {
        serviceIntent = Intent(ctx, GamificationService::class.java).apply {
            putExtra(GamificationService.SERVICE_COURSE, c)
            putExtra(GamificationService.SERVICE_ACTIVITY, act)
            putExtra(GamificationService.EVENTDATA_IS_COMPLETED, isCompleted)
            putExtra(GamificationService.EVENTDATA_IS_BASELINE, isBaseline)
        }
        return this
    }

    fun addExtraEventData(key: String, data: String) {
        extraData[key] = data
    }

    private fun putExtraDataIfAny() {
        if (extraData.isNotEmpty()) {
            serviceIntent?.putExtra(GamificationService.EVENTDATA_EXTRA, extraData)
        }
    }

    fun registerPageActivityEvent(timetaken: Long, isReadAloud: Boolean) {
        if (serviceIntent == null) return
        serviceIntent?.apply {
            putExtra(GamificationService.SERVICE_EVENT, GamificationService.SERVICE_EVENT_ACTIVITY)
            putExtra(GamificationService.EVENTDATA_TIMETAKEN, timetaken)
            putExtra(GamificationService.EVENTDATA_READALOUD, isReadAloud)
            putExtraDataIfAny()
        }

        ctx.startService(serviceIntent)
        serviceIntent = null
    }

    fun registerCourseDownloadEvent(c: Course?) {
        serviceIntent = Intent(ctx, GamificationService::class.java).apply {
            putExtra(GamificationService.SERVICE_EVENT, GamificationService.SERVICE_EVENT_DOWNLOAD)
            putExtra(GamificationService.SERVICE_COURSE, c)
            putExtraDataIfAny()
        }

        ctx.startService(serviceIntent)
    }

    fun registerQuizAttemptEvent(timetaken: Long, quiz: Quiz?, score: Float) {
        if (serviceIntent == null) return
        serviceIntent?.apply {
            putExtra(GamificationService.SERVICE_QUIZ, quiz)
            putExtra(GamificationService.SERVICE_EVENT, GamificationService.SERVICE_EVENT_QUIZ)
            putExtra(GamificationService.EVENTDATA_TIMETAKEN, timetaken)
            putExtra(GamificationService.SERVICE_QUIZ_SCORE, score)
            putExtraDataIfAny()
        }

        ctx.startService(serviceIntent)
        serviceIntent = null
    }

    fun registerResourceEvent(timetaken: Long) {
        if (serviceIntent == null) return
        serviceIntent?.apply {
            putExtra(GamificationService.SERVICE_EVENT, GamificationService.SERVICE_EVENT_RESOURCE)
            putExtra(GamificationService.EVENTDATA_TIMETAKEN, timetaken)
            putExtraDataIfAny()
        }

        ctx.startService(serviceIntent)
        serviceIntent = null
    }

    fun registerFeedbackEvent(timetaken: Long, feedback: Quiz?, quizId: Int, instanceId: String?) {
        if (serviceIntent == null) return
        serviceIntent?.apply {
            putExtra(GamificationService.SERVICE_QUIZ, feedback)
            putExtra(GamificationService.SERVICE_EVENT, GamificationService.SERVICE_EVENT_FEEDBACK)
            putExtra(GamificationService.EVENTDATA_TIMETAKEN, timetaken)
            putExtra(GamificationService.EVENTDATA_QUIZID, quizId)
            putExtra(GamificationService.EVENTDATA_INSTANCEID, instanceId)
            putExtraDataIfAny()
        }

        ctx.startService(serviceIntent)
        serviceIntent = null
    }

    fun registerMediaPlaybackEvent(timetaken: Long, mediaFileName: String, videoEndReached: Boolean) {
        if (serviceIntent == null) return
        serviceIntent?.apply {
            putExtra(GamificationService.SERVICE_EVENT, GamificationService.SERVICE_EVENT_MEDIAPLAYBACK)
            putExtra(GamificationService.EVENTDATA_TIMETAKEN, timetaken)
            putExtra(GamificationService.EVENTDATA_MEDIA_FILENAME, mediaFileName)
            putExtra(GamificationService.EVENTDATA_MEDIA_END_REACHED, videoEndReached)
            putExtraDataIfAny()
        }

        ctx.startService(serviceIntent)
        serviceIntent = null
    }
}