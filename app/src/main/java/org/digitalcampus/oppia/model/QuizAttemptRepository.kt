package org.digitalcampus.oppia.model

import android.content.Context
import androidx.preference.PreferenceManager
import org.digitalcampus.oppia.activity.PrefsActivity
import org.digitalcampus.oppia.application.SessionManager
import org.digitalcampus.oppia.database.DbHelper
import java.util.Locale

class QuizAttemptRepository {
    fun getQuizAttempts(ctx: Context?, quiz: QuizStats): List<QuizAttempt> {
        val db = DbHelper.getInstance(ctx)
        val userId = db.getUserId(SessionManager.getUsername(ctx))
        return db.getQuizAttempts(quiz.digest, userId)
    }

    fun getGlobalQuizAttempts(ctx: Context?): List<QuizAttempt> {
        val db = DbHelper.getInstance(ctx)
        val userId = db.getUserId(SessionManager.getUsername(ctx))
        val prefs = PreferenceManager.getDefaultSharedPreferences(ctx!!)
        val lang = prefs.getString(PrefsActivity.PREF_CONTENT_LANGUAGE, Locale.getDefault().language)
        return db.getGlobalQuizAttempts(userId, lang)
    }

    fun getQuizAttemptStats(ctx: Context?, courseId: Int, digest: String?): QuizStats {
        val db = DbHelper.getInstance(ctx)
        val userId = db.getUserId(SessionManager.getUsername(ctx))
        return db.getQuizAttemptStats(digest, courseId, userId)
    }
}