package org.digitalcampus.oppia.model;

import android.content.Context;

import org.digitalcampus.mobile.quiz.Quiz;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.SessionManager;
import org.joda.time.DateTime;

import java.util.List;

public class QuizAttemptRepository {

    public List<QuizAttempt> getQuizAttempts(Context ctx, QuizStats quiz){
        DbHelper db = DbHelper.getInstance(ctx);
        long userId = db.getUserId(SessionManager.getUsername(ctx));
        List<QuizAttempt> attempts = db.getQuizAttempts(quiz.getDigest(), userId);

        QuizAttempt attempt = new QuizAttempt();
        attempt.setScore(10);
        attempt.setDatetime(new DateTime());
        attempts.add(attempt);

        return attempts;
    }
}
