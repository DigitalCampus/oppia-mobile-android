package org.digitalcampus.oppia.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.model.QuizAttempt;

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
        //private TextView timetaken;
        private TextView score;
        private TextView course_title;
        private TextView quiz_title;

        public ViewHolder(View itemView) {
            super(itemView);
            //timetaken = itemView.findViewById(R.id.attempt_timetaken);
            date = itemView.findViewById(R.id.attempt_date);
            score = itemView.findViewById(R.id.score);
            course_title = itemView.findViewById(R.id.course_title);
            quiz_title = itemView.findViewById(R.id.quiz_title);

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

        viewHolder.quiz_title.setText(quiz.getDisplayTitle(ctx));
        String course = quiz.getCourseTitle();
        viewHolder.course_title.setText(course == null ? ctx.getString(R.string.quiz_attempts_unkwnown_course) : course);
        //viewHolder.timetaken.setText(quiz.getHumanTimetaken());
        viewHolder.date.setText(MobileLearning.DISPLAY_DATETIME_FORMAT.print(quiz.getDatetime()));
        viewHolder.score.setText(Math.round(quiz.getScoreAsPercent()) + "%");
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
