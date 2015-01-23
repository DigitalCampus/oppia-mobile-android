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
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.utils.ImageUtils;

import com.androidplot.pie.PieChart;
import com.androidplot.pie.PieRenderer;
import com.androidplot.pie.Segment;
import com.androidplot.pie.SegmentFormatter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ScorecardListAdapter extends ArrayAdapter<Course> {

	public static final String TAG = ScorecardListAdapter.class.getSimpleName();

	private final Context ctx;
	private final ArrayList<Course> courseList;
	private SharedPreferences prefs;
	
	private PieChart pie;
	private Segment s1;
    private Segment s2;
    private Segment s3;
    
	public ScorecardListAdapter(Activity context, ArrayList<Course> courseList) {
		super(context, R.layout.scorecard_list_row, courseList);
		this.ctx = context;
		this.courseList = courseList;
		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
	}


    static class ScorecardViewHolder{
        TextView courseTitle;
        PieChart pie;
    }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ScorecardViewHolder viewHolder;

		Course c = courseList.get(position);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView  = inflater.inflate(R.layout.scorecard_list_row, parent, false);
            viewHolder = new ScorecardViewHolder();
            viewHolder.courseTitle = (TextView) convertView.findViewById(R.id.course_title);
            viewHolder.pie = (PieChart) convertView.findViewById(R.id.scorecardPieChart);
            drawChartForCourse(viewHolder.pie,c);
            convertView.setTag(viewHolder);
        }
        else{
            viewHolder = (ScorecardViewHolder) convertView.getTag();
        }


        viewHolder.courseTitle.setText(c.getTitle(prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage())));
        
	  
		
	    return convertView;
	}
	
	
	private void drawChartForCourse(PieChart pie, Course course){
		
		Log.d(TAG,course.getTitleJSONString());


        //pie.setTitle(course.getTitle(prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage())));
        
        s1 = new Segment("", course.getNoActivitiesNotStarted());
        s2 = new Segment("", course.getNoActivitiesCompleted());
        s3 = new Segment("", course.getNoActivitiesStarted());

        SegmentFormatter sf1 = new SegmentFormatter();
        sf1.configure(ctx.getApplicationContext(), R.xml.scorecard_pie_segment_not_started);

        SegmentFormatter sf2 = new SegmentFormatter();
        sf2.configure(ctx.getApplicationContext(), R.xml.scorecard_pie_segment_completed);

        SegmentFormatter sf3 = new SegmentFormatter();
        sf3.configure(ctx.getApplicationContext(), R.xml.scorecard_pie_segment_started);


        pie.addSeries(s1, sf1);
        pie.addSeries(s2, sf2);
        pie.addSeries(s3, sf3);
        
        pie.getRenderer(PieRenderer.class).setDonutSize(60/100f, PieRenderer.DonutMode.PERCENT);

        pie.getBorderPaint().setColor(Color.TRANSPARENT);
        pie.getBackgroundPaint().setColor(Color.TRANSPARENT);
		
	}

}
