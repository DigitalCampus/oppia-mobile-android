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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.splunk.mint.Mint;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.adapter.BadgesAdapter;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.listener.APIRequestListener;
import org.digitalcampus.oppia.model.Badges;
import org.digitalcampus.oppia.task.APIUserRequestTask;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.utils.UIUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import javax.inject.Inject;

public class BadgesFragment extends AppFragment implements APIRequestListener {

	private JSONObject json;
    private BadgesAdapter adapterBadges;

	@Inject ArrayList<Badges> badges;
	
	public static BadgesFragment newInstance() {
	    return new BadgesFragment();
	}

	public BadgesFragment(){
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_badges, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		initializeDagger();

        adapterBadges = new BadgesAdapter(super.getActivity(), badges);
		RecyclerView recyclerBadges = this.getView().findViewById(R.id.recycler_badges);
		recyclerBadges.setAdapter(adapterBadges);

		getBadges();
	}

	private void initializeDagger() {
		MobileLearning app = (MobileLearning) getActivity().getApplication();
		app.getComponent().inject(this);
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

			this.getView().findViewById(R.id.empty_state).setVisibility(View.GONE);
			this.getView().findViewById(R.id.loading_badges).setVisibility(View.GONE);
			this.getView().findViewById(R.id.error_state).setVisibility(View.GONE);

			if(json.getJSONArray("objects").length() == 0){
				//tv.setText(R.string.info_no_badges);
				this.getView().findViewById(R.id.empty_state).setVisibility(View.VISIBLE);
				return;
			}
			for (int i = 0; i < (json.getJSONArray("objects").length()); i++) {
				JSONObject json_obj = (JSONObject) json.getJSONArray("objects").get(i);
				Badges b = new Badges();
				b.setDescription(json_obj.getString("description"));
				b.setDateTime(json_obj.getString("award_date"));
				badges.add(b);
			}

            adapterBadges.notifyDataSetChanged();
		} catch (Exception e) {
			Mint.logException(e);
			Log.d(TAG, "Error refreshing badges list: ", e);
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
				return;

			} catch (JSONException e) {
				Mint.logException(e);
				UIUtils.showAlert(super.getActivity(), R.string.loading, R.string.error_connection);
				Log.d(TAG, "Error connecting to server: ", e);
			}
		}

		//If we reach this statement there was some error
		this.getView().findViewById(R.id.loading_badges).setVisibility(View.GONE);
		this.getView().findViewById(R.id.empty_state).setVisibility(View.GONE);
		this.getView().findViewById(R.id.error_state).setVisibility(View.VISIBLE);
	}


}
