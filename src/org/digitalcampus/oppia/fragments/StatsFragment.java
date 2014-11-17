/* 
 * This file is part of OppiaMobile - http://oppia-mobile.org/
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
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.DatabaseManager;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.listener.TrackerServiceListener;
import org.digitalcampus.oppia.task.SubmitTrackerMultipleTask;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class StatsFragment extends Fragment implements TrackerServiceListener {

	public static final String TAG = StatsFragment.class.getSimpleName();
	private SharedPreferences prefs;
	private TextView sentTV;
	private TextView unsentTV;
	private Button sendBtn;
	private long userId;
	
	public static StatsFragment newInstance() {
		StatsFragment myFragment = new StatsFragment();
	    return myFragment;
	}

	public StatsFragment(){
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		prefs = PreferenceManager.getDefaultSharedPreferences(super.getActivity());
		View vv = super.getLayoutInflater(savedInstanceState).inflate(R.layout.fragment_stats, null);
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		vv.setLayoutParams(lp);
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
		
		DbHelper db = new DbHelper(super.getActivity());
		userId = db.getUserId(prefs.getString(PrefsActivity.PREF_USER_NAME, ""));
		DatabaseManager.getInstance().closeDatabase();
		
		sentTV = (TextView) super.getActivity().findViewById(R.id.stats_submitted);
		this.updateSent();
		
		unsentTV = (TextView) super.getActivity().findViewById(R.id.stats_unsubmitted);
		this.updateUnsent();
		
		sendBtn = (Button) super.getActivity().findViewById(R.id.submit_stats_btn);
		sendBtn.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				onSendClick();
			}
		});
		
	}
	
	protected void onSendClick(){
		sendBtn.setEnabled(false);
		MobileLearning app = (MobileLearning) super.getActivity().getApplication();
		if(app.omSubmitTrackerMultipleTask == null){
			Log.d(TAG,"Sumitting trackers multiple task");
			app.omSubmitTrackerMultipleTask = new SubmitTrackerMultipleTask(this.getActivity());
			app.omSubmitTrackerMultipleTask.setTrackerServiceListener(this);
			app.omSubmitTrackerMultipleTask.execute();
		}
	}
	
	private void updateSent(){
		DbHelper db = new DbHelper(super.getActivity());
		sentTV.setText(String.valueOf(db.getSentTrackersCount(userId)));
		DatabaseManager.getInstance().closeDatabase();
	}
	
	private void updateUnsent(){
		DbHelper db = new DbHelper(super.getActivity());
		unsentTV.setText(String.valueOf(db.getUnsentTrackersCount(userId)));
		DatabaseManager.getInstance().closeDatabase();
	}

	public void trackerComplete() {
		this.updateSent();
		this.updateUnsent();
		sendBtn.setEnabled(true);
		
	}

	public void trackerProgressUpdate() {
		this.updateSent();
		this.updateUnsent();	
	}
}
