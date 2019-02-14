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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.adapter.PointsListAdapter;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.model.Points;
import org.json.JSONObject;

import java.util.List;

import javax.inject.Inject;

public class PointsFragment extends AppFragment {

	public static final String TAG = PointsFragment.class.getSimpleName();

	@Inject
	List<Points> points;
	private PointsListAdapter pointsAdapter;

	public static PointsFragment newInstance() {
        return new PointsFragment();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_points, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		initializeDagger();
        getPoints();

        pointsAdapter = new PointsListAdapter(super.getActivity(), points);
        ListView listView = getView().findViewById(R.id.points_list);
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
        TextView tv = super.getActivity().findViewById(R.id.fragment_points_title);
        tv.setVisibility(View.GONE);
	}
}
