package org.digitalcampus.oppia.model;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.application.SessionManager;

import java.util.List;
import java.util.Locale;

public class QuizAttemptRepository {

    public List<QuizAttempt> getQuizAttempts(Context ctx, QuizStats quiz){
        DbHelper db = DbHelper.getInstance(ctx);
        long userId = db.getUserId(SessionManager.getUsername(ctx));
        return db.getQuizAttempts(quiz.getDigest(), userId);
    }

    public List<QuizAttempt> getGlobalQuizAttempts(Context ctx){
        DbHelper db = DbHelper.getInstance(ctx);
        long userId = db.getUserId(SessionManager.getUsername(ctx));
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String lang = prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage());
        return db.getGlobalQuizAttempts(userId, lang);
    }
}
