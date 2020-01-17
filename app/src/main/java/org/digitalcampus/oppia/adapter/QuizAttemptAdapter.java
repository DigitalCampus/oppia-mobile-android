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

public class QuizAttemptAdapter extends RecyclerView.Adapter<QuizAttemptAdapter.ViewHolder>{

    private final Context ctx;
    private final List<QuizAttempt> quizAttempts;

    private CourseQuizzesAdapter.OnItemClickListener itemClickListener;

    public QuizAttemptAdapter(Context context, List<QuizAttempt> quizAttempts) {
        this.ctx = context;
        this.quizAttempts = quizAttempts;
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView date;
        private TextView timetaken;
        private TextView score;

        public ViewHolder(View itemView) {

            super(itemView);
            timetaken = itemView.findViewById(R.id.attempt_timetaken);
            date = itemView.findViewById(R.id.attempt_date);
            score = itemView.findViewById(R.id.score);

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
        View v = LayoutInflater.from(ctx).inflate(R.layout.row_quiz_attempt, parent, false);
        return new QuizAttemptAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        final QuizAttempt quiz = getItemAtPosition(position);

        viewHolder.timetaken.setText(quiz.getHumanTimetaken());
        viewHolder.date.setText(MobileLearning.DISPLAY_DATETIME_FORMAT.print(quiz.getDatetime()));
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
