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

public class QuizAttemptAdapter extends RecyclerViewClickableAdapter<QuizAttemptAdapter.QuizAttemptViewHolder>{

    private final Context ctx;
    private final List<QuizAttempt> quizAttempts;

    public QuizAttemptAdapter(Context context, List<QuizAttempt> quizAttempts) {
        this.ctx = context;
        this.quizAttempts = quizAttempts;
    }


    class QuizAttemptViewHolder extends RecyclerViewClickableAdapter.ViewHolder {

        private TextView date;
        private TextView timetaken;
        private TextView score;

        QuizAttemptViewHolder(View itemView) {
            super(itemView);
            timetaken = itemView.findViewById(R.id.attempt_timetaken);
            date = itemView.findViewById(R.id.attempt_date);
            score = itemView.findViewById(R.id.score);
        }
    }


    @NonNull
    @Override
    public QuizAttemptViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.row_quiz_attempt, parent, false);
        return new QuizAttemptViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull QuizAttemptViewHolder viewHolder, int position) {
        final QuizAttempt quiz = getItemAtPosition(position);

        viewHolder.timetaken.setText(quiz.getHumanTimetaken());
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
