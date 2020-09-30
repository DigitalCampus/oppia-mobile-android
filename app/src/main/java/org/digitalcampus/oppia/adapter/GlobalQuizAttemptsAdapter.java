package org.digitalcampus.oppia.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.model.QuizAttempt;
import org.digitalcampus.oppia.utils.DateUtils;

import java.util.List;

import androidx.annotation.NonNull;

public class GlobalQuizAttemptsAdapter extends RecyclerViewClickableAdapter<GlobalQuizAttemptsAdapter.GlobalQuizAttemptsViewHolder>{

    private final Context ctx;
    private final List<QuizAttempt> quizAttempts;

    public GlobalQuizAttemptsAdapter(Context context, List<QuizAttempt> quizAttempts) {
        this.ctx = context;
        this.quizAttempts = quizAttempts;
    }

    class GlobalQuizAttemptsViewHolder extends RecyclerViewClickableAdapter.ViewHolder {

        private TextView date;
        private TextView score;
        private TextView courseTitle;
        private TextView quizTitle;

        GlobalQuizAttemptsViewHolder(View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.attempt_date);
            score = itemView.findViewById(R.id.score);
            courseTitle = itemView.findViewById(R.id.course_title);
            quizTitle = itemView.findViewById(R.id.quiz_title);

        }
    }

    @NonNull
    @Override
    public GlobalQuizAttemptsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.row_quiz_attempt_global, parent, false);
        return new GlobalQuizAttemptsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull GlobalQuizAttemptsViewHolder viewHolder, int position) {
        final QuizAttempt quiz = getItemAtPosition(position);

        viewHolder.quizTitle.setText(quiz.getDisplayTitle(ctx));
        String course = quiz.getCourseTitle();
        viewHolder.courseTitle.setText(course == null ? ctx.getString(R.string.quiz_attempts_unknown_course) : course);
        viewHolder.date.setText(DateUtils.DISPLAY_DATETIME_FORMAT.print(quiz.getDatetime()));
        viewHolder.score.setText(quiz.getScorePercentLabel());
        viewHolder.score.setBackgroundResource(
                quiz.isPassed()
                        ? R.drawable.scorecard_quiz_item_passed
                        : R.drawable.scorecard_quiz_item_attempted);
    }

    @Override
    public int getItemCount() {
        return quizAttempts.size();
    }

    public QuizAttempt getItemAtPosition(int position) {
        return quizAttempts.get(position);
    }
}
