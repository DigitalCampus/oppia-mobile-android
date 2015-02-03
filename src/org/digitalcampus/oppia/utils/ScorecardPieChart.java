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

package org.digitalcampus.oppia.utils;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.model.Course;

import android.app.Activity;
import android.graphics.Color;

import com.androidplot.pie.PieChart;
import com.androidplot.pie.PieRenderer;
import com.androidplot.pie.Segment;
import com.androidplot.pie.SegmentFormatter;

public class ScorecardPieChart {

	public static final String TAG = ScorecardPieChart.class.getSimpleName();
	
	private PieChart pie;

    private Segment segmentCompleted;
    private Segment segmentStarted;
    private Segment segmentNotStarted;
    
    private Activity activity;
    private Course course;
    
	public ScorecardPieChart(Activity activity, PieChart pie, Course course){
		this.activity = activity;
		this.pie = pie;
		this.course = course;
	}
	
	
	public void drawChart(int margin, boolean showSegementTitles){
		
        pie.setPlotMargins(margin, margin, margin, margin);
        
        if (showSegementTitles) {
	        segmentCompleted = new Segment("Completed (" + course.getNoActivitiesCompleted() + ")", course.getNoActivitiesCompleted());
	        segmentStarted = new Segment("Started (" + course.getNoActivitiesStarted() + ")", course.getNoActivitiesStarted());
	        segmentNotStarted = new Segment("Not Started (" + course.getNoActivitiesNotStarted() + ")", course.getNoActivitiesNotStarted());
        } else {
        	segmentCompleted = new Segment("", course.getNoActivitiesCompleted());
	        segmentStarted = new Segment("", course.getNoActivitiesStarted());
	        segmentNotStarted = new Segment("", course.getNoActivitiesNotStarted());
        }
        
        SegmentFormatter sfNotStarted = new SegmentFormatter();
        sfNotStarted.configure(activity.getApplicationContext(), R.xml.scorecard_pie_segment_not_started);

        SegmentFormatter sfCompleted = new SegmentFormatter();
        sfCompleted.configure(activity.getApplicationContext(), R.xml.scorecard_pie_segment_completed);

        SegmentFormatter sfStarted = new SegmentFormatter();
        sfStarted.configure(activity.getApplicationContext(), R.xml.scorecard_pie_segment_started);

        
        if (course.getNoActivitiesCompleted() != 0){
        	pie.addSeries(segmentCompleted, sfCompleted);
        }
        if (course.getNoActivitiesStarted() != 0){
        	pie.addSeries(segmentStarted, sfStarted);
        }
        if (course.getNoActivitiesNotStarted() != 0){
        	pie.addSeries(segmentNotStarted, sfNotStarted);
        }
        
        pie.getRenderer(PieRenderer.class).setDonutSize(60/100f, PieRenderer.DonutMode.PERCENT);

        pie.getBorderPaint().setColor(Color.TRANSPARENT);
        pie.getBackgroundPaint().setColor(Color.TRANSPARENT);
		
	}

}
