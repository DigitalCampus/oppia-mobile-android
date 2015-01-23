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

import java.util.ArrayList;
import java.util.Locale;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.adapter.ScorecardListAdapter;
import org.digitalcampus.oppia.application.DatabaseManager;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.utils.ScorecardPieChart;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;

import com.androidplot.pie.PieChart;
import com.androidplot.pie.PieRenderer;
import com.androidplot.pie.Segment;
import com.androidplot.pie.SegmentFormatter;

public class ScorecardFragment extends Fragment{

	public static final String TAG = ScorecardFragment.class.getSimpleName();
	private SharedPreferences prefs;
	private Course course = null;
	
    private PieChart pie;

    private Segment segmentCompleted;
    private Segment segmentStarted;
    private Segment segmentNotStarted;
    
    private ScorecardListAdapter scorecardListAdapter;
	
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
		View vv = null;
		if( getArguments() != null && getArguments().containsKey(Course.TAG)){
			vv = super.getLayoutInflater(savedInstanceState).inflate(R.layout.fragment_scorecard, null);
			LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			vv.setLayoutParams(lp);
			// refresh course to get most recent info (otherwise gets the info from when course first opened)
			this.course = (Course) getArguments().getSerializable(Course.TAG);
			DbHelper db = new DbHelper(super.getActivity());
			long userId = db.getUserId(prefs.getString(PrefsActivity.PREF_USER_NAME, ""));
			this.course = db.getCourse(this.course.getCourseId(), userId);
			DatabaseManager.getInstance().closeDatabase();	
		} else {
			vv = super.getLayoutInflater(savedInstanceState).inflate(R.layout.fragment_scorecards, null);
			LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			vv.setLayoutParams(lp);
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
			PieChart pie = (PieChart) super.getActivity().findViewById(R.id.scorecardPieChart);
			ScorecardPieChart spc = new ScorecardPieChart(super.getActivity(), pie, this.course);
			spc.drawChart(50, true);
		} else {
			DbHelper db = new DbHelper(super.getActivity());
			long userId = db.getUserId(prefs.getString(PrefsActivity.PREF_USER_NAME, ""));
			ArrayList<Course> courses = db.getCourses(userId);
			DatabaseManager.getInstance().closeDatabase();
			scorecardListAdapter = new ScorecardListAdapter(super.getActivity(), courses);
			ListView listView = (ListView) super.getActivity().findViewById(R.id.scorecards_list);
			listView.setAdapter(scorecardListAdapter);
		}
	}

}
