package org.digitalcampus.oppia.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.digitalcampus.mobile.learning.R
import org.digitalcampus.mobile.learning.databinding.RowQuizAttemptGlobalBinding
import org.digitalcampus.oppia.model.QuizAttempt
import org.digitalcampus.oppia.utils.DateUtils

class GlobalQuizAttemptsAdapter(
    private val ctx: Context,
    private val quizAttempts: List<QuizAttempt>
) : RecyclerViewClickableAdapter<GlobalQuizAttemptsAdapter.GlobalQuizAttemptsViewHolder>() {

    inner class GlobalQuizAttemptsViewHolder(itemView: View) : ViewHolder(itemView) {
        val binding: RowQuizAttemptGlobalBinding

        init {
            binding = RowQuizAttemptGlobalBinding.bind(itemView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GlobalQuizAttemptsViewHolder {
        val v = LayoutInflater.from(ctx).inflate(R.layout.row_quiz_attempt_global, parent, false)
        return GlobalQuizAttemptsViewHolder(v)
    }

    override fun onBindViewHolder(viewHolder: GlobalQuizAttemptsViewHolder, position: Int) {
        val quiz = getItemAtPosition(position)
        viewHolder.binding.quizTitle.text = quiz.getDisplayTitle(ctx)
        val course = quiz.courseTitle
        viewHolder.binding.courseTitle.text = course ?: ctx.getString(R.string.quiz_attempts_unknown_course)
        viewHolder.binding.attemptDate.text = DateUtils.DISPLAY_DATETIME_FORMAT.print(quiz.datetime)
        viewHolder.binding.score.percentLabel.text = quiz.getScorePercentLabel()
        viewHolder.binding.score.percentLabel.setBackgroundResource(
            if (quiz.isPassed) R.drawable.scorecard_quiz_item_passed
            else R.drawable.scorecard_quiz_item_attempted
        )
    }

    override fun getItemCount(): Int {
        return quizAttempts.size
    }

    fun getItemAtPosition(position: Int): QuizAttempt {
        return quizAttempts[position]
    }
}