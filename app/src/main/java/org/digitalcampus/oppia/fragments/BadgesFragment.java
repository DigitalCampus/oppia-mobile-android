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
import org.digitalcampus.oppia.adapter.BadgesListAdapter;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.listener.APIRequestListener;
import org.digitalcampus.oppia.model.Badges;
import org.digitalcampus.oppia.task.APIUserRequestTask;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.utils.UIUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.splunk.mint.Mint;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;

public class BadgesFragment extends Fragment implements APIRequestListener {

	public static final String TAG = BadgesFragment.class.getSimpleName();
	private JSONObject json;
    private ArrayList<Badges> badges;
    private BadgesListAdapter badgesAdapter;
	
	public static BadgesFragment newInstance() {
		BadgesFragment myFragment = new BadgesFragment();
	    return myFragment;
	}

	public BadgesFragment(){
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View vv = super.getLayoutInflater(savedInstanceState).inflate(R.layout.fragment_badges, null);
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

        badges = new ArrayList<Badges>();
        badgesAdapter = new BadgesListAdapter(super.getActivity(), badges);
        ListView listView = (ListView) this.getView().findViewById(R.id.badges_list);
        listView.setAdapter(badgesAdapter);

		getBadges();
	}
	
	private void getBadges(){		
		APIUserRequestTask task = new APIUserRequestTask(super.getActivity());
		Payload p = new Payload(MobileLearning.SERVER_AWARDS_PATH);
		task.setAPIRequestListener(this);
		task.execute(p);
	}

	private void refreshBadgesList() {

        badges.clear();
		try {
			TextView tv = (TextView) this.getView().findViewById(R.id.fragment_badges_title);
			if(json.getJSONArray("objects").length() == 0){
				tv.setText(R.string.info_no_badges);
				return;
			}
			for (int i = 0; i < (json.getJSONArray("objects").length()); i++) {
				JSONObject json_obj = (JSONObject) json.getJSONArray("objects").get(i);
				Badges b = new Badges();
				b.setDescription(json_obj.getString("description"));
				b.setDateTime(json_obj.getString("award_date"));
				badges.add(b);
			}
			tv.setVisibility(View.GONE);
            badgesAdapter.notifyDataSetChanged();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public void apiRequestComplete(Payload response) {
        //If the fragment has been detached, we don't process the result, as is not going to be shown
        // and could cause NullPointerExceptions
        if (super.getActivity() == null) return;

		if(response.isResult()){
			try {
				json = new JSONObject(response.getResultResponse());
				Log.d(TAG,json.toString(4));
				refreshBadgesList();
			} catch (JSONException e) {
				Mint.logException(e);
				UIUtils.showAlert(super.getActivity(), R.string.loading, R.string.error_connection);
				e.printStackTrace();
			}
		} else {
			TextView tv = (TextView) this.getView().findViewById(R.id.fragment_badges_title);
			tv.setVisibility(View.VISIBLE);
			tv.setText(R.string.error_connection_required);
		} 		
	}


}
