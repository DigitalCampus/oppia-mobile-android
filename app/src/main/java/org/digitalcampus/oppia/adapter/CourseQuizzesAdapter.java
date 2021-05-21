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

package org.digitalcampus.oppia.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.databinding.RowQuizScorecardBinding;
import org.digitalcampus.oppia.model.QuizStats;

import java.util.List;

import androidx.annotation.NonNull;

public class CourseQuizzesAdapter extends RecyclerViewClickableAdapter<CourseQuizzesAdapter.CourseQuizzesViewHolder> {

    private final Context ctx;
    private final List<QuizStats> quizzesList;



    public CourseQuizzesAdapter(Context context, List<QuizStats> quizzesList) {
        this.ctx = context;
        this.quizzesList = quizzesList;
    }

    public class CourseQuizzesViewHolder extends RecyclerViewClickableAdapter.ViewHolder {

        private final RowQuizScorecardBinding binding;

        public CourseQuizzesViewHolder(View itemView) {

            super(itemView);
            binding = RowQuizScorecardBinding.bind(itemView);

        }
    }

    @Override
    public CourseQuizzesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.row_quiz_scorecard, parent, false);
        return new CourseQuizzesViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final CourseQuizzesViewHolder viewHolder, final int position) {

        final QuizStats quiz = getItemAtPosition(position);

        viewHolder.binding.quizTitle.setText(quiz.getQuizTitle());
        viewHolder.binding.sectionTitle.setText(quiz.getSectionTitle());

        if (quiz.isAttempted()){
            viewHolder.binding.score.percentLabel.setText(""+quiz.getPercent()+"%");
            viewHolder.binding.score.percentLabel.setVisibility(View.VISIBLE);
            if (quiz.isPassed()){
                viewHolder.binding.score.percentLabel.setBackgroundResource(R.drawable.scorecard_quiz_item_passed);
            }
            else{
                viewHolder.binding.score.percentLabel.setBackgroundResource(R.drawable.scorecard_quiz_item_attempted);
            }
        }
        else{
            viewHolder.binding.score.percentLabel.setText("");
            viewHolder.binding.score.percentLabel.setBackgroundResource(R.drawable.scorecard_quiz_item);
        }


    }

    @Override
    public int getItemCount() {
        return quizzesList.size();
    }

    public QuizStats getItemAtPosition(int position) {
        return quizzesList.get(position);
    }

}
