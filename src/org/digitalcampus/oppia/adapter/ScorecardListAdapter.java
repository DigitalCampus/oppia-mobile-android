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
    
	public ScorecardListAdapter(Activity context, ArrayList<Course> courseList) {
		super(context, R.layout.scorecard_list_row, courseList);
		this.ctx = context;
		this.courseList = courseList;
		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
	}


    static class ScorecardViewHolder{
        TextView courseTitle;
        PieChart pie;
        ScorecardPieChart spc; 
        SegmentFormatter sfNotStarted;
        SegmentFormatter sfCompleted;
        SegmentFormatter sfStarted;
        Segment segmentCompleted;
        Segment segmentStarted;
        Segment segmentNotStarted;        
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
            viewHolder.sfNotStarted = new SegmentFormatter();
            viewHolder.sfNotStarted.configure(ctx, R.xml.scorecard_pie_segment_not_started);

            viewHolder.sfCompleted = new SegmentFormatter();
            viewHolder.sfCompleted.configure(ctx, R.xml.scorecard_pie_segment_completed);

            viewHolder.sfStarted = new SegmentFormatter();
            viewHolder.sfStarted.configure(ctx, R.xml.scorecard_pie_segment_started);
            
            viewHolder.segmentCompleted = new Segment("Completed (" + course.getNoActivitiesCompleted() + ")", course.getNoActivitiesCompleted());
            Log.i(TAG, viewHolder.segmentCompleted.getTitle());
            viewHolder.segmentStarted = new Segment("Started (" + course.getNoActivitiesStarted() + ")", course.getNoActivitiesStarted());
            Log.i(TAG, viewHolder.segmentStarted.getTitle());
            viewHolder.segmentNotStarted = new Segment("Not Started (" + course.getNoActivitiesNotStarted() + ")", course.getNoActivitiesNotStarted());
            Log.i(TAG, viewHolder.segmentNotStarted.getTitle()); 
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ScorecardViewHolder) convertView.getTag();
        }

    	
        viewHolder.courseTitle.setText(course.getTitle(prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage())));
        viewHolder.pie.clear();
        viewHolder.pie.setPlotMargins(0, 0, 0, 0);
        
                   
        
        if (course.getNoActivitiesCompleted() != 0){
          	viewHolder.pie.addSeries(viewHolder.segmentCompleted, viewHolder.sfCompleted);
          }
          if (course.getNoActivitiesStarted() != 0){
          	viewHolder.pie.addSeries(viewHolder.segmentStarted, viewHolder.sfStarted);
          }
          if (course.getNoActivitiesNotStarted() != 0){
          	viewHolder.pie.addSeries(viewHolder.segmentNotStarted, viewHolder.sfNotStarted);
          }
          
          viewHolder.pie.getRenderer(PieRenderer.class).setDonutSize(60/100f, PieRenderer.DonutMode.PERCENT);

          viewHolder.pie.getBorderPaint().setColor(Color.TRANSPARENT);
          viewHolder.pie.getBackgroundPaint().setColor(Color.TRANSPARENT);

	    return convertView;
	}
}
