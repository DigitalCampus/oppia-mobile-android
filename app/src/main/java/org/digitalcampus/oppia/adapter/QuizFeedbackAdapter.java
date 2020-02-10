package org.digitalcampus.oppia.adapter;


import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.quiz.Quiz;
import org.digitalcampus.oppia.model.QuizFeedback;

import java.util.List;

public class QuizFeedbackAdapter extends RecyclerView.Adapter<QuizFeedbackAdapter.ViewHolder> {


    private List<QuizFeedback> quizFeedbacks;
    private Context context;
    private OnItemClickListener itemClickListener;


    public QuizFeedbackAdapter(Context context, List<QuizFeedback> quizFeedbacks) {
        this.context = context;
        this.quizFeedbacks = quizFeedbacks;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View contactView = LayoutInflater.from(context).inflate(R.layout.row_widget_quiz_feedback, parent, false);

        // Return a new holder instance
        return new ViewHolder(contactView);
    }


    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {

        final QuizFeedback qf = getItemAtPosition(position);

        StringBuilder userResponseText = new StringBuilder();
        for (int i=0; i<qf.getUserResponse().size();i++){
            userResponseText.append(qf.getUserResponse().get(i));
            if (i+1<qf.getUserResponse().size()){
                userResponseText.append("\n");
            }
        }

        if (Build.VERSION.SDK_INT >= 24){
            viewHolder.quizQuestion.setText(Html.fromHtml(qf.getQuestionText(), Html.FROM_HTML_MODE_LEGACY));
            viewHolder.quizUserResponse.setText(Html.fromHtml(userResponseText.toString(), Html.FROM_HTML_MODE_LEGACY));
        } else {
            viewHolder.quizQuestion.setText(Html.fromHtml(qf.getQuestionText()));
            viewHolder.quizUserResponse.setText(Html.fromHtml(userResponseText.toString()));
        }

        if (qf.getFeedbackText() != null && !qf.getFeedbackText().equals("")){
            viewHolder.quizFeedbackTitle.setVisibility(View.VISIBLE);
            viewHolder.quizFeedbackText.setVisibility(View.VISIBLE);
            if (Build.VERSION.SDK_INT >= 24) {
                viewHolder.quizFeedbackText.setText(Html.fromHtml(qf.getFeedbackText(), Html.FROM_HTML_MODE_LEGACY));
            } else {
                viewHolder.quizFeedbackText.setText(Html.fromHtml(qf.getFeedbackText()));
            }
        } else {
            //If there's no feedback to show, hide both text and title
            viewHolder.quizFeedbackTitle.setVisibility(View.GONE);
            viewHolder.quizFeedbackText.setVisibility(View.GONE);
        }

        viewHolder.quizFeedbackIcon.setImageResource(
                (qf.getScore() >= Quiz.QUIZ_QUESTION_PASS_THRESHOLD)?
                        R.drawable.quiz_tick:
                        R.drawable.quiz_cross
        );

    }

    @Override
    public int getItemCount() {
        return quizFeedbacks.size();
    }

    public QuizFeedback getItemAtPosition(int position) {
        return quizFeedbacks.get(position);
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        public View rootView;
        private TextView quizQuestion;
        private TextView quizUserResponse;
        private TextView quizFeedbackTitle;
        private TextView quizFeedbackText;
        private ImageView quizFeedbackIcon;

        public ViewHolder(View itemView) {

            super(itemView);

            quizQuestion = itemView.findViewById(R.id.quiz_question_text);
            quizUserResponse = itemView.findViewById(R.id.quiz_question_user_response_text);
            quizFeedbackText = itemView.findViewById(R.id.quiz_question_user_feedback_text);
            quizFeedbackTitle = itemView.findViewById(R.id.quiz_question_user_feedback_title);
            quizFeedbackIcon = itemView.findViewById(R.id.quiz_question_feedback_image);
            rootView = itemView;

            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemClickListener != null) {
                        itemClickListener.onItemClick(v, getAdapterPosition());
                    }
                }
            });
        }

    }


    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }
}
 

