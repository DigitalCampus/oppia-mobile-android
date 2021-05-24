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
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.databinding.FragmentBadgesBinding;
import org.digitalcampus.oppia.adapter.BadgesAdapter;
import org.digitalcampus.oppia.analytics.Analytics;
import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.api.Paths;
import org.digitalcampus.oppia.application.PermissionsManager;
import org.digitalcampus.oppia.listener.APIRequestListener;
import org.digitalcampus.oppia.model.Badge;
import org.digitalcampus.oppia.service.DownloadOppiaDataService;
import org.digitalcampus.oppia.task.APIUserRequestTask;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.utils.UIUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import javax.inject.Inject;

public class BadgesFragment extends AppFragment implements APIRequestListener, DownloadOppiaDataService.DownloadOppiaDataListener {

	private static final String STR_JSON_OBJECTS = "objects";

	private JSONObject json;
    private BadgesAdapter adapterBadges;

	@Inject
	ApiEndpoint apiEndpoint;

	@Inject
	List<Badge> badges;
	private FragmentBadgesBinding binding;
	private DownloadOppiaDataService downloadOppiaDataService;

	public static BadgesFragment newInstance() {
	    return new BadgesFragment();
	}

	public BadgesFragment(){
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentBadgesBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getAppComponent().inject(this);

        adapterBadges = new BadgesAdapter(super.getActivity(), badges);
        adapterBadges.setOnItemClickListener(position -> checkPermissionAndDownloadCertificate(badges.get(position)));
		binding.recyclerBadges.setAdapter(adapterBadges);

		downloadOppiaDataService = new DownloadOppiaDataService(getActivity());
		downloadOppiaDataService.setDownloadOppiaDataListener(this);
		downloadOppiaDataService.setShowOpenDownloadsDialogOnSuccess(true);

		getBadges();
	}

	@Override
	public void onResume() {
		super.onResume();

		binding.permissionsExplanation.setVisibility(View.GONE);
		downloadOppiaDataService.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		downloadOppiaDataService.onPause();
	}

	private void getBadges(){
		APIUserRequestTask task = new APIUserRequestTask(super.getActivity(), apiEndpoint);
		String url = Paths.SERVER_AWARDS_PATH;
		task.setAPIRequestListener(this);
		task.execute(url);
	}

	private void refreshBadgesList() {

        badges.clear();
		try {

			binding.emptyState.setVisibility(View.GONE);
			binding.loadingBadges.setVisibility(View.GONE);
			binding.errorState.setVisibility(View.GONE);

			if(json.getJSONArray(STR_JSON_OBJECTS).length() == 0){
				binding.emptyState.setVisibility(View.VISIBLE);
				return;
			}
			for (int i = 0; i < (json.getJSONArray(STR_JSON_OBJECTS).length()); i++) {
				JSONObject jsonObj = (JSONObject) json.getJSONArray(STR_JSON_OBJECTS).get(i);
				Badge badge = new Badge();
				badge.setDescription(jsonObj.getString("description"));
				badge.setDateTime(jsonObj.getString("award_date"));
				if (!jsonObj.isNull("certificate_pdf")) {
					badge.setCertificatePdf(jsonObj.getString("certificate_pdf"));
				}

				badges.add(badge);
			}

            adapterBadges.notifyDataSetChanged();
		} catch (Exception e) {
			Analytics.logException(e);
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
				Analytics.logException(e);
				UIUtils.showAlert(super.getActivity(), R.string.loading, R.string.error_connection);
				Log.d(TAG, "Error connecting to server: ", e);
			}
		}

		//If we reach this statement there was some error
		binding.loadingBadges.setVisibility(View.GONE);
		binding.emptyState.setVisibility(View.GONE);
		binding.errorState.setVisibility(View.VISIBLE);
	}

	private void checkPermissionAndDownloadCertificate(Badge badge) {

		boolean hasPermissions = PermissionsManager.checkPermissionsAndInform(getActivity(),
				PermissionsManager.STORAGE_PERMISSIONS, binding.permissionsExplanation);

		if (hasPermissions) {
			downloadCertificate(badge);
		}
	}

	private void downloadCertificate(Badge badge) {
		downloadOppiaDataService.downloadOppiaData(badge.getCertificatePdf(), null);
	}

	@Override
	public void onDownloadStarted() {
		showProgressDialog(getString(R.string.downloading));
	}

	@Override
	public void onDownloadFinished(boolean success, String errorMessage) {
		hideProgressDialog();
		if (!TextUtils.isEmpty(errorMessage)) {
			toast(errorMessage);
		}
	}
}
