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

        private TextView title;
        private TextView section;
        private TextView score;

        public CourseQuizzesViewHolder(View itemView) {

            super(itemView);
            section = itemView.findViewById(R.id.section_title);
            title = itemView.findViewById(R.id.quiz_title);
            score = itemView.findViewById(R.id.score);

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

        viewHolder.title.setText(quiz.getQuizTitle());
        viewHolder.section.setText(quiz.getSectionTitle());

        if (quiz.isAttempted()){
            viewHolder.score.setText(""+quiz.getPercent()+"%");
            viewHolder.score.setVisibility(View.VISIBLE);
            if (quiz.isPassed()){
                viewHolder.score.setBackgroundResource(R.drawable.scorecard_quiz_item_passed);
            }
            else{
                viewHolder.score.setBackgroundResource(R.drawable.scorecard_quiz_item_attempted);
            }
        }
        else{
            viewHolder.score.setText("");
            viewHolder.score.setBackgroundResource(R.drawable.scorecard_quiz_item);
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
