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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.utils.ui.ScorecardPieChart;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.androidplot.pie.PieChart;
import com.androidplot.pie.SegmentFormatter;

public class ScorecardListAdapter extends ArrayAdapter<Course> {

	public static final String TAG = ScorecardListAdapter.class.getSimpleName();

	private final Activity ctx;
	private final List<Course> courseList;
	private SharedPreferences prefs;

    private SegmentFormatter sfCompleted;
    private SegmentFormatter sfStarted;
    private SegmentFormatter sfNotStarted;
    
	public ScorecardListAdapter(Activity context, List<Course> courseList) {
		super(context, R.layout.scorecard_list_row, courseList);
		this.ctx = context;
		this.courseList = courseList;
		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);

        //Initialization of SegmentFormatters
        sfCompleted = new SegmentFormatter();
        sfCompleted.configure(ctx, R.xml.scorecard_pie_segment_completed);
        sfStarted = new SegmentFormatter();
        sfStarted.configure(ctx, R.xml.scorecard_pie_segment_started);
        sfNotStarted = new SegmentFormatter();
        sfNotStarted.configure(ctx, R.xml.scorecard_pie_segment_not_started);
	}

    static class ScorecardViewHolder{
        TextView courseTitle;
        TextView activitiesCompleted;
        TextView activitiesTotal;
        ScorecardPieChart pieChart;
    }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ScorecardViewHolder viewHolder;

		Course course = courseList.get(position);
		
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView  = inflater.inflate(R.layout.scorecard_list_row, parent, false);
            viewHolder = new ScorecardViewHolder();
            viewHolder.courseTitle = convertView.findViewById(R.id.course_title);
            PieChart pie = convertView.findViewById(R.id.scorecard_pie_chart);
            viewHolder.pieChart = new ScorecardPieChart(ctx, pie, course);
            viewHolder.pieChart.configureChart(0, 0.79f, false);
            viewHolder.activitiesCompleted = convertView.findViewById(R.id.scorecard_activities_completed);
            viewHolder.activitiesTotal = convertView.findViewById(R.id.scorecard_activities_total);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ScorecardViewHolder) convertView.getTag();
        }

        viewHolder.courseTitle.setText(course.getMultiLangInfo().getTitle(prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage())));

        int numCompleted = course.getNoActivitiesCompleted();
        int numStarted = course.getNoActivitiesStarted();
        int numNotStarted = course.getNoActivitiesNotStarted();

        viewHolder.activitiesCompleted.setText(""+numCompleted);
        viewHolder.activitiesTotal.setText(""+course.getNoActivities());
	    viewHolder.pieChart.animate(numCompleted, numStarted, numNotStarted, false);
        return convertView;
	}

    private void createAnimator(PieSegmentsAnimator animatorListener, int numCompleted, int numStarted, int numNotStarted){

        int total = numCompleted + numStarted + numNotStarted;
        //We create the thre valueHolders for the animation
        PropertyValuesHolder completedHolder = PropertyValuesHolder.ofFloat("completed", 0, numCompleted);
        PropertyValuesHolder startedHolder = PropertyValuesHolder.ofFloat("started", 0, numStarted);
        //The notStarted animates from the total number of activities to its number
        PropertyValuesHolder notStartedHolder = PropertyValuesHolder.ofFloat("notStarted", total, numNotStarted);

        //We create and start the animation assigning its listener
        ValueAnimator anim = ObjectAnimator.ofPropertyValuesHolder(completedHolder, startedHolder, notStartedHolder);
        anim.addUpdateListener(animatorListener);
        anim.setDuration(MobileLearning.SCORECARD_ANIM_DURATION).start();
    }

    class PieSegmentsAnimator implements ValueAnimator.AnimatorUpdateListener{

        //reference to the view to wich the animation is going to be applied
        private ScorecardViewHolder viewHolder;

        public PieSegmentsAnimator (ScorecardViewHolder holder){
            viewHolder = holder;
        }
        
        //@Override
        public void onAnimationUpdate(ValueAnimator animator) {
            // no need to update animation
        }
    }
}
