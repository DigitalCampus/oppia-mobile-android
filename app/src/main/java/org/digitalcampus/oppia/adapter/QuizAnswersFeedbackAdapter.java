package org.digitalcampus.oppia.adapter;


import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.databinding.RowWidgetQuizFeedbackBinding;
import org.digitalcampus.mobile.quiz.Quiz;
import org.digitalcampus.oppia.model.QuizAnswerFeedback;

import java.util.List;

public class QuizAnswersFeedbackAdapter extends RecyclerView.Adapter<QuizAnswersFeedbackAdapter.QuizFeedbackViewHolder> {


    private List<QuizAnswerFeedback> quizFeedbacks;
    private Context context;
    private OnItemClickListener itemClickListener;


    public QuizAnswersFeedbackAdapter(Context context, List<QuizAnswerFeedback> quizAnswerFeedbacks) {
        this.context = context;
        this.quizFeedbacks = quizAnswerFeedbacks;
    }

    @Override
    public QuizFeedbackViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View contactView = LayoutInflater.from(context).inflate(R.layout.row_widget_quiz_feedback, parent, false);

        // Return a new holder instance
        return new QuizFeedbackViewHolder(contactView);
    }


    @Override
    public void onBindViewHolder(final QuizFeedbackViewHolder viewHolder, final int position) {

        final QuizAnswerFeedback qf = getItemAtPosition(position);

        StringBuilder userResponseText = new StringBuilder();
        for (int i=0; i<qf.getUserResponse().size();i++){
            userResponseText.append(qf.getUserResponse().get(i));
            if (i+1<qf.getUserResponse().size()){
                userResponseText.append("\n");
            }
        }

        viewHolder.binding.quizQuestionText.setText(HtmlCompat.fromHtml(qf.getQuestionText(), HtmlCompat.FROM_HTML_MODE_LEGACY));
        if (qf.isSurvey() && TextUtils.isEmpty(userResponseText)){
            viewHolder.binding.quizQuestionUserResponseTitle.setText(R.string.widget_quiz_feedback_response_skipped);
            viewHolder.binding.quizQuestionUserResponseText.setVisibility(View.GONE);
        }
        else{
            viewHolder.binding.quizQuestionUserResponseTitle.setText(R.string.widget_quiz_feedback_response_title);
            viewHolder.binding.quizQuestionUserResponseText.setVisibility(View.VISIBLE);
            viewHolder.binding.quizQuestionUserResponseText.setText(HtmlCompat.fromHtml(userResponseText.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY));
        }



        if (qf.getFeedbackText() != null && !qf.getFeedbackText().equals("")){
            viewHolder.binding.quizQuestionUserFeedbackTitle.setVisibility(View.VISIBLE);
            viewHolder.binding.quizQuestionUserFeedbackText.setVisibility(View.VISIBLE);
            viewHolder.binding.quizQuestionUserFeedbackText.setText(HtmlCompat.fromHtml(qf.getFeedbackText(), HtmlCompat.FROM_HTML_MODE_LEGACY));

        } else {
            //If there's no feedback to show, hide both text and title
            viewHolder.binding.quizQuestionUserFeedbackTitle.setVisibility(View.GONE);
            viewHolder.binding.quizQuestionUserFeedbackText.setVisibility(View.GONE);
        }

        if (qf.isSurvey()){
            viewHolder.binding.quizQuestionFeedbackImage.setVisibility(View.GONE);
        }else {
            viewHolder.binding.quizQuestionFeedbackImage.setVisibility(View.VISIBLE);
            viewHolder.binding.quizQuestionFeedbackImage.setImageResource(
                    (qf.getScore() >= Quiz.QUIZ_QUESTION_PASS_THRESHOLD)?
                            R.drawable.quiz_tick:
                            R.drawable.quiz_cross
            );
        }


    }

    @Override
    public int getItemCount() {
        return quizFeedbacks.size();
    }

    public QuizAnswerFeedback getItemAtPosition(int position) {
        return quizFeedbacks.get(position);
    }


    public class QuizFeedbackViewHolder extends RecyclerView.ViewHolder {

        private final RowWidgetQuizFeedbackBinding binding;

        public QuizFeedbackViewHolder(View itemView) {

            super(itemView);

            binding = RowWidgetQuizFeedbackBinding.bind(itemView);

            itemView.setOnClickListener(v -> {
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(v, getAdapterPosition());
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
 

