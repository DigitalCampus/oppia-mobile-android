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
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.listener.TrackerServiceListener;
import org.digitalcampus.oppia.task.SubmitTrackerMultipleTask;

import android.os.Bundle;
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
	private TextView sentTV;
	private TextView unsentTV;
	private Button sendBtn;
	
	public static StatsFragment newInstance() {
        return new StatsFragment();
	}

	public StatsFragment(){
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View vv = inflater.inflate(R.layout.fragment_stats, null);
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
		DbHelper db = DbHelper.getInstance(super.getActivity());
		sentTV.setText(String.valueOf(db.getSentTrackersCount()));
	}
	
	private void updateUnsent(){
		DbHelper db = DbHelper.getInstance(super.getActivity());
		unsentTV.setText(String.valueOf(db.getUnsentTrackersCount()));
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
