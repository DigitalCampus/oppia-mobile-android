package org.digitalcampus.oppia.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.digitalcampus.mobile.learning.R
import org.digitalcampus.mobile.learning.databinding.RowQuizAttemptBinding
import org.digitalcampus.oppia.adapter.QuizAttemptAdapter.QuizAttemptViewHolder
import org.digitalcampus.oppia.model.QuizAttempt
import org.digitalcampus.oppia.utils.DateUtils

class QuizAttemptAdapter(private val ctx: Context, private val quizAttempts: List<QuizAttempt>) :
    RecyclerViewClickableAdapter<QuizAttemptViewHolder>() {

    inner class QuizAttemptViewHolder(itemView: View) : ViewHolder(itemView) {
        val binding: RowQuizAttemptBinding = RowQuizAttemptBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizAttemptViewHolder {
        val v = LayoutInflater.from(ctx).inflate(R.layout.row_quiz_attempt, parent, false)
        return QuizAttemptViewHolder(v)
    }

    override fun onBindViewHolder(viewHolder: QuizAttemptViewHolder, position: Int) {
        val quiz = getItemAtPosition(position)
        viewHolder.binding.attemptTimetaken.text = quiz.getHumanTimetaken()
        viewHolder.binding.attemptDate.text = DateUtils.DISPLAY_DATETIME_FORMAT.print(quiz.datetime)
        viewHolder.binding.score.percentLabel.text = quiz.getScorePercentLabel()
        viewHolder.binding.score.percentLabel.setBackgroundResource(
            if (quiz.isPassed) {
                R.drawable.scorecard_quiz_item_passed
            } else {
                R.drawable.scorecard_quiz_item_attempted
            }
        )
    }

    override fun getItemCount(): Int {
        return quizAttempts.size
    }

    fun getItemAtPosition(position: Int): QuizAttempt {
        return quizAttempts[position]
    }
}