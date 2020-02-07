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
import androidx.recyclerview.widget.RecyclerView;

public class GlobalQuizAttemptsAdapter extends RecyclerView.Adapter<GlobalQuizAttemptsAdapter.ViewHolder>{

    private final Context ctx;
    private final List<QuizAttempt> quizAttempts;

    private CourseQuizzesAdapter.OnItemClickListener itemClickListener;

    public GlobalQuizAttemptsAdapter(Context context, List<QuizAttempt> quizAttempts) {
        this.ctx = context;
        this.quizAttempts = quizAttempts;
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView date;
        private TextView score;
        private TextView courseTitle;
        private TextView quizTitle;

        public ViewHolder(View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.attempt_date);
            score = itemView.findViewById(R.id.score);
            courseTitle = itemView.findViewById(R.id.course_title);
            quizTitle = itemView.findViewById(R.id.quiz_title);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemClickListener != null) {
                        itemClickListener.onItemClick(getAdapterPosition());
                    }
                }
            });
        }
    }


    public void setOnItemClickListener(CourseQuizzesAdapter.OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.row_quiz_attempt_global, parent, false);
        return new GlobalQuizAttemptsAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        final QuizAttempt quiz = getItemAtPosition(position);

        viewHolder.quizTitle.setText(quiz.getDisplayTitle(ctx));
        String course = quiz.getCourseTitle();
        viewHolder.courseTitle.setText(course == null ? ctx.getString(R.string.quiz_attempts_unkwnown_course) : course);
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
