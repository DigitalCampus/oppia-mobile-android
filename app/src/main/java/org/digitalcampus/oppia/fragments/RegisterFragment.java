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
import org.digitalcampus.oppia.activity.OppiaMobileActivity;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.application.Tracker;
import org.digitalcampus.oppia.gamification.GamificationEngine;
import org.digitalcampus.oppia.listener.SubmitListener;
import org.digitalcampus.oppia.model.GamificationEvent;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.task.RegisterTask;
import org.digitalcampus.oppia.utils.UIUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout.LayoutParams;

public class RegisterFragment extends AppFragment implements SubmitListener {


	public static final String TAG = RegisterFragment.class.getSimpleName();
	private SharedPreferences prefs;
	private EditText usernameField;
	private EditText emailField;
	private EditText passwordField;
	private EditText passwordAgainField;
	private EditText firstnameField;
	private EditText lastnameField;
	private EditText jobTitleField;
	private EditText organisationField;
	private EditText phoneNoField;
	private Button registerButton;
	private ProgressDialog pDialog;
	
	public static RegisterFragment newInstance() {
	    return new RegisterFragment();
	}

	public RegisterFragment(){
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		prefs = PreferenceManager.getDefaultSharedPreferences(super.getActivity());
		View vv = super.getLayoutInflater(savedInstanceState).inflate(R.layout.fragment_register, null);
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

		usernameField = (EditText) super.getActivity().findViewById(R.id.register_form_username_field);
		emailField = (EditText) super.getActivity().findViewById(R.id.register_form_email_field);
		passwordField = (EditText) super.getActivity().findViewById(R.id.register_form_password_field);
		passwordAgainField = (EditText) super.getActivity().findViewById(R.id.register_form_password_again_field);
		firstnameField = (EditText) super.getActivity().findViewById(R.id.register_form_firstname_field);
		lastnameField = (EditText) super.getActivity().findViewById(R.id.register_form_lastname_field);
		jobTitleField = (EditText) super.getActivity().findViewById(R.id.register_form_jobtitle_field);
		organisationField = (EditText) super.getActivity().findViewById(R.id.register_form_organisation_field);
		phoneNoField = (EditText) super.getActivity().findViewById(R.id.register_form_phoneno_field);
		
		registerButton = (Button) super.getActivity().findViewById(R.id.register_btn);
		registerButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				onRegisterClick();
			}
		});
	}

	public void submitComplete(Payload response) {
		pDialog.dismiss();
		if (response.isResult()) {
			User user = (User) response.getData().get(0);
            SessionManager.loginUser(getActivity(), user);

			// registration gamification
			GamificationEngine gamificationEngine = new GamificationEngine(super.getActivity());
			gamificationEngine.processEventRegister();

            //Save the search tracker
            new Tracker(super.getActivity()).saveRegisterTracker();

	    	startActivity(new Intent(getActivity(), OppiaMobileActivity.class));
	    	super.getActivity().finish();
		} else {
			Context ctx = super.getActivity();
			if (ctx != null){
				try {
					JSONObject jo = new JSONObject(response.getResultResponse());
					UIUtils.showAlert(ctx,R.string.error,jo.getString("error"));
				} catch (JSONException je) {
					UIUtils.showAlert(ctx,R.string.error,response.getResultResponse());
				}
			}
		}
	}

	public void onRegisterClick() {
		// get form fields
		String username = usernameField.getText().toString().trim();
		String email = emailField.getText().toString();
		String password = passwordField.getText().toString();
		String passwordAgain = passwordAgainField.getText().toString();
		String firstname = firstnameField.getText().toString();
		String lastname = lastnameField.getText().toString();
		String phoneNo = phoneNoField.getText().toString();
		String jobTitle = jobTitleField.getText().toString();
		String organisation = organisationField.getText().toString();
		
		// do validation
		// check username
		if (username.length() == 0) {
			UIUtils.showAlert(super.getActivity(),R.string.error,R.string.error_register_no_username);
			return;
		}
			
		if (username.contains(" ")) {
			UIUtils.showAlert(super.getActivity(),R.string.error,R.string.error_register_username_spaces);
			return;
		}

		if (email.length() == 0) {
			UIUtils.showAlert(super.getActivity(),R.string.error,R.string.error_register_no_email);
			return;
		}
		
		// check password length
		if (password.length() < MobileLearning.PASSWORD_MIN_LENGTH) {
			UIUtils.showAlert(super.getActivity(),R.string.error,getString(R.string.error_register_password,  MobileLearning.PASSWORD_MIN_LENGTH ));
			return;
		}
		
		// check password match
		if (!password.equals(passwordAgain)) {
			UIUtils.showAlert(super.getActivity(),R.string.error,R.string.error_register_password_no_match);
			return;
		}
		
		// check firstname
		if (firstname.length() < 2) {
			UIUtils.showAlert(super.getActivity(),R.string.error,R.string.error_register_no_firstname);
			return;
		}

		// check lastname
		if (lastname.length() < 2) {
			UIUtils.showAlert(super.getActivity(),R.string.error,R.string.error_register_no_lastname);
			return;
		}

		// check phone no
		if (phoneNo.length() < 8) {
			UIUtils.showAlert(super.getActivity(),R.string.error,R.string.error_register_no_phoneno);
			return;
		}
				
		pDialog = new ProgressDialog(super.getActivity());
		pDialog.setTitle(R.string.register_alert_title);
		pDialog.setMessage(getString(R.string.register_process));
		pDialog.setCancelable(true);
		pDialog.show();

		ArrayList<Object> users = new ArrayList<Object>();
    	User u = new User();
		u.setUsername(username);
		u.setPassword(password);
		u.setPasswordAgain(passwordAgain);
		u.setFirstname(firstname);
		u.setLastname(lastname);
		u.setEmail(email);
		u.setJobTitle(jobTitle);
		u.setOrganisation(organisation);
		u.setPhoneNo(phoneNo);
		users.add(u);
		Payload p = new Payload(users);
		RegisterTask rt = new RegisterTask(super.getActivity());
		rt.setRegisterListener(this);
		rt.execute(p);
	}
}
