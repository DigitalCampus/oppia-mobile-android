/*
 * This file is part of OppiaMobile - https://digital-campus.org/
 *
 * OppiaMobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OppiaMobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OppiaMobile. If not, see <http://www.gnu.org/licenses/>.
 */
package org.digitalcampus.oppia.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.digitalcampus.mobile.learning.R
import org.digitalcampus.mobile.learning.databinding.RowQuizScorecardBinding
import org.digitalcampus.oppia.adapter.CourseQuizzesAdapter.CourseQuizzesViewHolder
import org.digitalcampus.oppia.model.QuizStats

class CourseQuizzesAdapter(private val context: Context, private val quizzesList: List<QuizStats>) : RecyclerViewClickableAdapter<CourseQuizzesViewHolder>() {

    inner class CourseQuizzesViewHolder(itemView: View) : ViewHolder(itemView) {
        val binding: RowQuizScorecardBinding

        init {
            binding = RowQuizScorecardBinding.bind(itemView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseQuizzesViewHolder {
        val v = LayoutInflater.from(context).inflate(R.layout.row_quiz_scorecard, parent, false)
        return CourseQuizzesViewHolder(v)
    }

    override fun onBindViewHolder(viewHolder: CourseQuizzesViewHolder, position: Int) {
        val quiz = getItemAtPosition(position)
        viewHolder.binding.quizTitle.text = quiz.quizTitle
        viewHolder.binding.sectionTitle.text = quiz.sectionTitle
        if (quiz.isAttempted()) {
            viewHolder.binding.score.percentLabel.text = "" + quiz.getPercent() + "%"
            viewHolder.binding.score.percentLabel.visibility = View.VISIBLE
            if (quiz.isPassed) {
                viewHolder.binding.score.percentLabel.setBackgroundResource(R.drawable.scorecard_quiz_item_passed)
            } else {
                viewHolder.binding.score.percentLabel.setBackgroundResource(R.drawable.scorecard_quiz_item_attempted)
            }
        } else {
            viewHolder.binding.score.percentLabel.text = ""
            viewHolder.binding.score.percentLabel.setBackgroundResource(R.drawable.scorecard_quiz_item)
        }
    }

    override fun getItemCount(): Int {
        return quizzesList.size
    }

    fun getItemAtPosition(position: Int): QuizStats {
        return quizzesList[position]
    }
}