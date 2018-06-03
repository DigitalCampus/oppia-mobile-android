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

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.adapter.PointsListAdapter;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.listener.APIRequestListener;
import org.digitalcampus.oppia.model.Points;
import org.digitalcampus.oppia.task.APIUserRequestTask;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.utils.UIUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.splunk.mint.Mint;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;

import javax.inject.Inject;

public class PointsFragment extends AppFragment {

	public static final String TAG = PointsFragment.class.getSimpleName();

    private JSONObject json;
    @Inject ArrayList<Points> points;
	private PointsListAdapter pointsAdapter;

	public static PointsFragment newInstance() {
        return new PointsFragment();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View vv = super.getLayoutInflater(savedInstanceState).inflate(R.layout.fragment_points, null);
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
		initializeDagger();
        getPoints();

        pointsAdapter = new PointsListAdapter(super.getActivity(), points);
        ListView listView = (ListView) getView().findViewById(R.id.points_list);
        listView.setAdapter(pointsAdapter);
	}

	private void initializeDagger() {
		MobileLearning app = (MobileLearning) getActivity().getApplication();
		app.getComponent().inject(this);
	}
	
	private void getPoints(){
		DbHelper db = DbHelper.getInstance(super.getActivity());
		long userId = db.getUserId(SessionManager.getUsername(super.getActivity()));
		points = db.getUserPoints(userId);
        TextView tv = (TextView) super.getActivity().findViewById(R.id.fragment_points_title);
        tv.setVisibility(View.GONE);
	}
}
