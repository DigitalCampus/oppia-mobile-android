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

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.listener.SubmitListener;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.task.ResetTask;
import org.digitalcampus.oppia.utils.UIUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import javax.inject.Inject;

public class ResetFragment extends AppFragment implements SubmitListener{

	private EditText usernameField;
    private ProgressDialog pDialog;
    private Button resetButton;

	@Inject
	ApiEndpoint apiEndpoint;
	
	public static ResetFragment newInstance() {
        return new ResetFragment();
	}

	public ResetFragment(){
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_reset, container, false);
		getAppComponent().inject(this);

		usernameField = v.findViewById(R.id.reset_username_field);
		resetButton = v.findViewById(R.id.reset_btn);
		return v;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		resetButton.setOnClickListener(v -> onResetClick());
	}

	public void submitComplete(Payload response) {
		pDialog.dismiss();
		if (response.isResult()) {
			UIUtils.showAlert(super.getActivity(),R.string.reset_alert_title,response.getResultResponse());
		} else {
			Context ctx = super.getActivity();
			if (ctx != null){
				try {
					JSONObject jo = new JSONObject(response.getResultResponse());
					UIUtils.showAlert(super.getActivity(),R.string.error,jo.getString("error"));
				} catch (JSONException je) {
					UIUtils.showAlert(super.getActivity(),R.string.error,response.getResultResponse());
				}
			}
		}
	}

	public void onResetClick() {
		// get form fields
		String username = usernameField.getText().toString();

		// do validation
		// check firstname
		if (username.length() == 0) {
			UIUtils.showAlert(super.getActivity(),R.string.error,R.string.error_register_no_username);
			return;
		}

		pDialog = new ProgressDialog(super.getActivity());
		pDialog.setTitle(R.string.reset_alert_title);
		pDialog.setMessage(getString(R.string.reset_process));
		pDialog.setCancelable(true);
		pDialog.show();

		ArrayList<Object> users = new ArrayList<>();
    	User u = new User();
		u.setUsername(username);
		users.add(u);
		Payload p = new Payload(users);
		ResetTask rt = new ResetTask(super.getActivity(), apiEndpoint);
		rt.setResetListener(this);
		rt.execute(p);
	}
}
