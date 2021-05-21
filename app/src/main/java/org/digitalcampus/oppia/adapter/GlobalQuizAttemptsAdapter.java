package org.digitalcampus.oppia.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.databinding.RowQuizAttemptGlobalBinding;
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
        
        private final RowQuizAttemptGlobalBinding binding;

        GlobalQuizAttemptsViewHolder(View itemView) {
            super(itemView);
            binding = RowQuizAttemptGlobalBinding.bind(itemView);

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

        viewHolder.binding.quizTitle.setText(quiz.getDisplayTitle(ctx));
        String course = quiz.getCourseTitle();
        viewHolder.binding.courseTitle.setText(course == null ? ctx.getString(R.string.quiz_attempts_unknown_course) : course);
        viewHolder.binding.attemptDate.setText(DateUtils.DISPLAY_DATETIME_FORMAT.print(quiz.getDatetime()));
        viewHolder.binding.score.percentLabel.setText(quiz.getScorePercentLabel());
        viewHolder.binding.score.percentLabel.setBackgroundResource(
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
