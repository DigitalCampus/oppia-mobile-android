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

package org.digitalcampus.oppia.fragments;

import org.digitalcampus.mobile.learning.R;

import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.utils.ConnectionUtils;
import org.digitalcampus.oppia.utils.storage.FileUtils;


import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.PointF;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout.LayoutParams;

import com.androidplot.pie.PieChart;
import com.androidplot.pie.PieRenderer;
import com.androidplot.pie.Segment;
import com.androidplot.pie.SegmentFormatter;

public class ScorecardFragment extends Fragment{

	public static final String TAG = ScorecardFragment.class.getSimpleName();
	private SharedPreferences prefs;
	private Course course = null;
	
    private PieChart pie;

    private Segment s1;
    private Segment s2;
    private Segment s3;
    private Segment s4;
	
    public static ScorecardFragment newInstance() {
		ScorecardFragment myFragment = new ScorecardFragment();
	    return myFragment;
	}
    
	public static ScorecardFragment newInstance(Course course) {
		ScorecardFragment myFragment = new ScorecardFragment();
		Bundle args = new Bundle();
	    args.putSerializable(Course.TAG, course);
	    myFragment.setArguments(args);
	    return myFragment;
	}

	public ScorecardFragment(){
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		prefs = PreferenceManager.getDefaultSharedPreferences(super.getActivity());
		View vv = super.getLayoutInflater(savedInstanceState).inflate(R.layout.fragment_scorecard, null);
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		vv.setLayoutParams(lp);
		if( getArguments() != null && getArguments().containsKey(Course.TAG)){
			this.course = (Course) getArguments().getSerializable(Course.TAG);
		}
		return vv;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		prefs = PreferenceManager.getDefaultSharedPreferences(super.getActivity());

		if(this.course != null){
			this.drawChartForCourse(R.id.mySimplePieChart, course);
		}
	}
	
	private void drawChartForCourse(int chartViewId, Course course){
		
		
		Log.d(TAG,course.getTitleJSONString());
		Log.d(TAG,course.getTitleJSONString());
		
		// Get total no activities
		// Get no activities completed
		// Get activities attempted
		// Get no activities not started 
		
		
		// initialize our XYPlot reference:
        pie = (PieChart) super.getActivity().findViewById(chartViewId);

        // detect segment clicks:
        pie.setOnTouchListener(new View.OnTouchListener(){
        	
            public boolean onTouch(View view, MotionEvent motionEvent) {
                PointF click = new PointF(motionEvent.getX(), motionEvent.getY());
                if(pie.getPieWidget().containsPoint(click)) {
                    Segment segment = pie.getRenderer(PieRenderer.class).getContainingSegment(click);
                    if(segment != null) {
                        // handle the segment click...for now, just print
                        // the clicked segment's title to the console:
                        System.out.println("Clicked Segment: " + segment.getTitle());
                    }
                }
                return false;
            }
        });
  
        s1 = new Segment("s1", course.getProgress());
        s2 = new Segment("s2", 1);
        s3 = new Segment("s3", 10);

        //EmbossMaskFilter emf = new EmbossMaskFilter(new float[]{0, 0 ,0}, 0.4f, 10, 8.2f);

        SegmentFormatter sf1 = new SegmentFormatter();
        sf1.configure(super.getActivity().getApplicationContext(), R.xml.scorecard_pie_segment_attempted);
        //sf1.getFillPaint().setMaskFilter(emf);

        SegmentFormatter sf2 = new SegmentFormatter();
        sf2.configure(super.getActivity().getApplicationContext(), R.xml.scorecard_pie_segment_completed);
       //sf2.getFillPaint().setMaskFilter(emf);

        SegmentFormatter sf3 = new SegmentFormatter();
        sf3.configure(super.getActivity().getApplicationContext(), R.xml.scorecard_pie_segment_not_started);
        //sf3.getFillPaint().setMaskFilter(emf);


        pie.addSeries(s1, sf1);
        pie.addSeries(s2, sf2);
        pie.addSeries(s3, sf3);
        
        pie.getRenderer(PieRenderer.class).setDonutSize(60/100f, PieRenderer.DonutMode.PERCENT);

        pie.getBorderPaint().setColor(Color.TRANSPARENT);
        pie.getBackgroundPaint().setColor(Color.TRANSPARENT);
		
	}

}
