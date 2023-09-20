package org.digitalcampus.oppia.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.digitalcampus.mobile.learning.R
import org.digitalcampus.mobile.learning.databinding.RowWidgetQuizFeedbackBinding
import org.digitalcampus.mobile.quiz.Quiz
import org.digitalcampus.oppia.adapter.QuizAnswersFeedbackAdapter.QuizFeedbackViewHolder
import org.digitalcampus.oppia.listener.OnItemClickListener
import org.digitalcampus.oppia.model.QuizAnswerFeedback
import org.digitalcampus.oppia.utils.TextUtilsJava
import org.digitalcampus.oppia.utils.UIUtils.getFromHtmlAndTrim

class QuizAnswersFeedbackAdapter(
    private val context: Context,
    private val quizFeedbacks: List<QuizAnswerFeedback>
) : RecyclerView.Adapter<QuizFeedbackViewHolder>() {
    private var itemClickListener: OnItemClickListener? = null

    inner class QuizFeedbackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding: RowWidgetQuizFeedbackBinding = RowWidgetQuizFeedbackBinding.bind(itemView)

        init {
            itemView.setOnClickListener {
                itemClickListener?.onItemClick(itemView, adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizFeedbackViewHolder {
        val contactView = LayoutInflater.from(context).inflate(R.layout.row_widget_quiz_feedback, parent, false)
        return QuizFeedbackViewHolder(contactView)
    }

    override fun onBindViewHolder(viewHolder: QuizFeedbackViewHolder, position: Int) {
        val qf = getItemAtPosition(position)
        val userResponseText = StringBuilder()
        for (i in qf.userResponse!!.indices) {
            userResponseText.append(qf.userResponse!![i])
            if (i + 1 < qf.userResponse!!.size) {
                userResponseText.append("<br>")
            }
        }
        viewHolder.binding.quizQuestionText.text = getFromHtmlAndTrim(qf.questionText)
        if (qf.isSurvey && TextUtilsJava.isEmpty(userResponseText)) {
            viewHolder.binding.quizQuestionUserResponseTitle.setText(R.string.widget_quiz_feedback_response_skipped)
            viewHolder.binding.quizQuestionUserResponseText.visibility = View.GONE
        } else {
            viewHolder.binding.quizQuestionUserResponseTitle.setText(R.string.widget_quiz_feedback_response_title)
            viewHolder.binding.quizQuestionUserResponseText.visibility = View.VISIBLE
            viewHolder.binding.quizQuestionUserResponseText.text = getFromHtmlAndTrim(userResponseText.toString())
        }
        if (qf.feedbackText != null && qf.feedbackText != "") {
            viewHolder.binding.quizQuestionUserFeedbackTitle.visibility = View.VISIBLE
            viewHolder.binding.quizQuestionUserFeedbackText.visibility = View.VISIBLE
            viewHolder.binding.quizQuestionUserFeedbackText.text = getFromHtmlAndTrim(qf.feedbackText)
        } else {
            //If there's no feedback to show, hide both text and title
            viewHolder.binding.quizQuestionUserFeedbackTitle.visibility = View.GONE
            viewHolder.binding.quizQuestionUserFeedbackText.visibility = View.GONE
        }
        if (qf.isSurvey) {
            viewHolder.binding.quizQuestionFeedbackImage.visibility = View.GONE
        } else {
            viewHolder.binding.quizQuestionFeedbackImage.visibility = View.VISIBLE

            val icon = when {
                qf.score >= Quiz.QUIZ_QUESTION_PASS_THRESHOLD -> R.drawable.quiz_tick
                qf.score > 0 -> R.drawable.quiz_partially_correct
                else -> R.drawable.quiz_cross
            }

            viewHolder.binding.quizQuestionFeedbackImage.setImageResource(icon)
        }
    }

    override fun getItemCount(): Int {
        return quizFeedbacks.size
    }

    fun getItemAtPosition(position: Int): QuizAnswerFeedback {
        return quizFeedbacks[position]
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        itemClickListener = listener
    }
}