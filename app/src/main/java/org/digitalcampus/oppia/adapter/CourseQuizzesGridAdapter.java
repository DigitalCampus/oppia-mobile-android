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
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.model.QuizStats;

import java.util.ArrayList;

public class CourseQuizzesGridAdapter  extends ArrayAdapter<QuizStats> {

    private static final int NUM_COLUMNS = 6;
    private final Context ctx;
    private final ArrayList<QuizStats> quizzesList;

    public CourseQuizzesGridAdapter(Context context, ArrayList<QuizStats> quizzesList) {
        super(context, R.layout.scorecard_quiz_item, quizzesList);
        this.ctx = context;
        this.quizzesList = quizzesList;
    }

    static class QuizStatsViewHolder{
        TextView percent;
        View baseView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        QuizStatsViewHolder viewHolder;

        GridView grid = (GridView)parent;
        int cellSize = grid.getWidth() / NUM_COLUMNS;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView  = inflater.inflate(R.layout.scorecard_quiz_item, parent, false);
            viewHolder = new QuizStatsViewHolder();
            viewHolder.percent = (TextView) convertView.findViewById(R.id.percent_label);
            viewHolder.baseView = convertView;
            convertView.setTag(viewHolder);
        }
        else{
            viewHolder = (QuizStatsViewHolder) convertView.getTag();
        }

        if (cellSize>0) viewHolder.baseView.setLayoutParams(new GridView.LayoutParams(cellSize, cellSize));
        QuizStats quiz = quizzesList.get(position);
        if (quiz.isAttempted()){
            viewHolder.percent.setText(""+quiz.getPercent()+"%");
            viewHolder.percent.setVisibility(View.VISIBLE);
            if (quiz.isPassed()){
                viewHolder.percent.setBackgroundResource(R.drawable.scorecard_quiz_item_passed);
            }
            else{
                viewHolder.percent.setBackgroundResource(R.drawable.scorecard_quiz_item_attempted);
            }
        }
        else{
            viewHolder.percent.setText("");
            viewHolder.percent.setBackgroundResource(R.drawable.scorecard_quiz_item);
        }


        return convertView;

    }

    @Override
    public QuizStats getItem(int position){
        return quizzesList.get(position);
    }
}
