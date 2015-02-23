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
import java.util.Locale;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.utils.ScorecardPieChart;

import com.androidplot.pie.PieChart;
import com.androidplot.pie.PieRenderer;
import com.androidplot.pie.Segment;
import com.androidplot.pie.SegmentFormatter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ScorecardListAdapter extends ArrayAdapter<Course> {

	public static final String TAG = ScorecardListAdapter.class.getSimpleName();

	private final Activity ctx;
	private final ArrayList<Course> courseList;
	private SharedPreferences prefs;

    private SegmentFormatter sfCompleted;
    private SegmentFormatter sfStarted;
    private SegmentFormatter sfNotStarted;
    
	public ScorecardListAdapter(Activity context, ArrayList<Course> courseList) {
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
        PieChart pie;
    }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ScorecardViewHolder viewHolder;

		Course course = courseList.get(position);
		Log.i(TAG, course.getTitle("en") + ": " + position);
		
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView  = inflater.inflate(R.layout.scorecard_list_row, parent, false);
            viewHolder = new ScorecardViewHolder();
            viewHolder.courseTitle = (TextView) convertView.findViewById(R.id.course_title);
            viewHolder.pie = (PieChart) convertView.findViewById(R.id.scorecardPieChart);
            viewHolder.pie.setPlotMargins(0, 0, 0, 0);
            viewHolder.pie.getBorderPaint().setColor(Color.TRANSPARENT);
            viewHolder.pie.getBackgroundPaint().setColor(Color.TRANSPARENT);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ScorecardViewHolder) convertView.getTag();
        }

        viewHolder.courseTitle.setText(course.getTitle(prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage())));
        viewHolder.pie.clear();


        if (course.getNoActivitiesCompleted() != 0){
            Segment segmentCompleted = new Segment("Completed (" + course.getNoActivitiesCompleted() + ")", course.getNoActivitiesCompleted());
            viewHolder.pie.addSeries(segmentCompleted, sfCompleted);

          }
          if (course.getNoActivitiesStarted() != 0){
              Segment segmentStarted = new Segment("Started (" + course.getNoActivitiesStarted() + ")", course.getNoActivitiesStarted());
              viewHolder.pie.addSeries(segmentStarted, sfStarted);
          }
          if (course.getNoActivitiesNotStarted() != 0){
            Segment segmentNotStarted = new Segment("Not Started (" + course.getNoActivitiesNotStarted() + ")", course.getNoActivitiesNotStarted());
            viewHolder.pie.addSeries(segmentNotStarted, sfNotStarted);
          }

        viewHolder.pie.getRenderer(PieRenderer.class).setDonutSize(60/100f, PieRenderer.DonutMode.PERCENT);
	    return convertView;
	}
}
