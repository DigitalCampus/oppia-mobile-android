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
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.model.Course;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Color;

import com.androidplot.pie.PieChart;
import com.androidplot.pie.PieRenderer;
import com.androidplot.pie.Segment;
import com.androidplot.pie.SegmentFormatter;

public class ScorecardPieChart {

	public static final String TAG = ScorecardPieChart.class.getSimpleName();
	
	private PieChart pie;

    private Segment segmentCompleted = new Segment("",0);
    private Segment segmentStarted = new Segment("",0);
    private Segment segmentNotStarted = new Segment("",0);
    
    private Activity activity;
    private Course course;
    
	public ScorecardPieChart(Activity activity, PieChart pie, Course course){
		this.activity = activity;
		this.pie = pie;
		this.course = course;
	}

	public void drawChart(int margin, boolean showSegementTitles){
		
        pie.setPlotMargins(margin, margin, margin, margin);
        SegmentFormatter sfNotStarted = new SegmentFormatter();
        sfNotStarted.configure(activity.getApplicationContext(), R.xml.scorecard_pie_segment_not_started);

        SegmentFormatter sfCompleted = new SegmentFormatter();
        sfCompleted.configure(activity.getApplicationContext(), R.xml.scorecard_pie_segment_completed);

        SegmentFormatter sfStarted = new SegmentFormatter();
        sfStarted.configure(activity.getApplicationContext(), R.xml.scorecard_pie_segment_started);

        int numCompleted = course.getNoActivitiesCompleted();
        if (numCompleted != 0){
            segmentCompleted.setTitle( showSegementTitles ? "Completed (" + numCompleted + ")" : "");
            segmentCompleted.setValue(numCompleted);
            pie.addSeries(segmentCompleted, sfCompleted);
        }

        int numStarted = course.getNoActivitiesStarted();
        if (numStarted != 0){
            segmentStarted.setTitle( showSegementTitles ? "Started (" + numStarted + ")" : "");
            segmentStarted.setValue(numStarted);
            pie.addSeries(segmentStarted, sfStarted);
        }

        int numNotStarted = course.getNoActivitiesNotStarted();
        segmentNotStarted.setTitle( (showSegementTitles && numNotStarted != 0)? "Not Started (" + numNotStarted + ")" : "");
        segmentNotStarted.setValue(numNotStarted);
        pie.addSeries(segmentNotStarted, sfNotStarted);
        
        pie.getRenderer(PieRenderer.class).setDonutSize(60/100f, PieRenderer.DonutMode.PERCENT);

        pie.getBorderPaint().setColor(Color.TRANSPARENT);
        pie.getBackgroundPaint().setColor(Color.TRANSPARENT);

        createAnimator(numCompleted, numStarted, numNotStarted);
		
	}

    private void createAnimator(int numCompleted, int numStarted, int numNotStarted){

        int total = numCompleted + numStarted + numNotStarted;
        //We create the thre valueHolders for the animation
        PropertyValuesHolder completedHolder = PropertyValuesHolder.ofInt("completed", 0, numCompleted);
        PropertyValuesHolder startedHolder = PropertyValuesHolder.ofInt("started", 0, numStarted);
        //The notStarted animates from the total number of activities to its number
        PropertyValuesHolder notStartedHolder = PropertyValuesHolder.ofInt("notStarted", total, numNotStarted);

        //We create and start the animation assigning its listener
        ValueAnimator anim = ObjectAnimator.ofPropertyValuesHolder(completedHolder, startedHolder, notStartedHolder);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                segmentCompleted.setValue((Integer) animator.getAnimatedValue("completed"));
                segmentStarted.setValue((Integer) animator.getAnimatedValue("started"));
                segmentNotStarted.setValue((Integer)animator.getAnimatedValue("notStarted"));
                pie.invalidate();
            }
        });
        anim.setDuration(MobileLearning.SCORECARD_ANIM_DURATION).start();
    }

}
