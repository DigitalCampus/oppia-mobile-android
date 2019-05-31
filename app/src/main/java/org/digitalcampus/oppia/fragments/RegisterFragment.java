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
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.digitalcampus.mobile.learning.BuildConfig;
import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.OppiaMobileActivity;
import org.digitalcampus.oppia.activity.WelcomeActivity;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.application.Tracker;
import org.digitalcampus.oppia.gamification.GamificationEngine;
import org.digitalcampus.oppia.listener.SubmitListener;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.task.RegisterTask;
import org.digitalcampus.oppia.utils.UIUtils;
import org.digitalcampus.oppia.utils.ui.ValidableTextInputLayout;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class RegisterFragment extends AppFragment implements SubmitListener, RegisterTask.RegisterListener {

	public static final String TAG = RegisterFragment.class.getSimpleName();


	private ValidableTextInputLayout usernameField;
	private ValidableTextInputLayout emailField;
	private ValidableTextInputLayout passwordField;
	private ValidableTextInputLayout passwordAgainField;
	private ValidableTextInputLayout firstnameField;
	private ValidableTextInputLayout lastnameField;
	private ValidableTextInputLayout jobTitleField;
	private ValidableTextInputLayout organisationField;
	private ValidableTextInputLayout phoneNoField;
	private List<ValidableTextInputLayout> fields;

	private Button registerButton;
	private Button loginButton;
	private ProgressDialog pDialog;
	
	public static RegisterFragment newInstance() {
	    return new RegisterFragment();
	}

	public RegisterFragment(){
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View vv = inflater.inflate(R.layout.fragment_register, container, false);

		usernameField = vv.findViewById(R.id.register_form_username_field);
		emailField = vv.findViewById(R.id.register_form_email_field);
		passwordField = vv.findViewById(R.id.register_form_password_field);
		passwordAgainField = vv.findViewById(R.id.register_form_password_again_field);
		firstnameField = vv.findViewById(R.id.register_form_firstname_field);
		lastnameField = vv.findViewById(R.id.register_form_lastname_field);
		jobTitleField = vv.findViewById(R.id.register_form_jobtitle_field);
		organisationField = vv.findViewById(R.id.register_form_organisation_field);
		//phoneNoField = vv.findViewById(R.id.register_form_phoneno_field);
		registerButton = vv.findViewById(R.id.register_btn);
		//loginButton = vv.findViewById(R.id.action_login_btn);

		fields = Arrays.asList(usernameField, emailField, passwordField, passwordAgainField,
				firstnameField, lastnameField, jobTitleField, organisationField);

		return vv;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		registerButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				onRegisterClick();
			}
		});

		for (ValidableTextInputLayout field : fields){
			field.initialize();
		}

		/*loginButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				WelcomeActivity wa = (WelcomeActivity) RegisterFragment.super.getActivity();
				wa.switchTab(WelcomeActivity.TAB_LOGIN);
			}
		});*/

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

		String username = usernameField.getCleanedValue();
		String email = emailField.getCleanedValue();
		String password = passwordField.getCleanedValue();
		String passwordAgain = passwordAgainField.getCleanedValue();
		String firstname = firstnameField.getCleanedValue();
		String lastname = lastnameField.getCleanedValue();
		//String phoneNo = phoneNoField.getCleanedValue();
		String jobTitle = jobTitleField.getCleanedValue();
		String organisation = organisationField.getCleanedValue();

		boolean valid = true;
		for (ValidableTextInputLayout field : fields){
			valid = field.validate() && valid;
		}

		/*if (email.length() == 0) {
			UIUtils.showAlert(super.getActivity(),R.string.error,R.string.error_register_no_email);
			return;
		}*/

		// check password length
		if (password.length() < MobileLearning.PASSWORD_MIN_LENGTH) {
			passwordField.setErrorEnabled(true);
			passwordField.setError(getString(R.string.error_register_password,  MobileLearning.PASSWORD_MIN_LENGTH ));
			passwordField.requestFocus();
		    return;
		}
		
		// check password match
		if (!password.equals(passwordAgain)) {
            passwordField.setErrorEnabled(true);
            passwordField.setError(getString(R.string.error_register_password_no_match ));
			passwordField.requestFocus();
            return;
		}

		// check phone no
		/*if (phoneNo.length() < 8) {
            phoneNoField.setErrorEnabled(true);
            phoneNoField.setError(getString(R.string.error_register_no_phoneno ));
			phoneNoField.requestFocus();

			return;
		}*/

		if (valid){
            User u = new User();
            u.setUsername(username);
            u.setPassword(password);
            u.setPasswordAgain(passwordAgain);
            u.setFirstname(firstname);
            u.setLastname(lastname);
            u.setEmail(email);
            u.setJobTitle(jobTitle);
            u.setOrganisation(organisation);
            //u.setPhoneNo(phoneNo);
            executeRegisterTask(u);
        }

	}

	@Override
	public void onSubmitComplete(User registeredUser) {
		pDialog.dismiss();
		SessionManager.loginUser(getActivity(), registeredUser);
		// registration gamification
		GamificationEngine gamificationEngine = new GamificationEngine(super.getActivity());
		gamificationEngine.processEventRegister();

		//Save the search tracker
		new Tracker(super.getActivity()).saveRegisterTracker();
		startActivity(new Intent(getActivity(), OppiaMobileActivity.class));
		super.getActivity().finish();
	}

	@Override
	public void onSubmitError(String error) {
		pDialog.dismiss();
		Context ctx = super.getActivity();
		if (ctx != null){
			UIUtils.showAlert(getActivity(), R.string.error, error);
		}
	}

	@Override
	public void onConnectionError(String error, final User u) {
		pDialog.dismiss();
		Context ctx = super.getActivity();
		if (ctx == null){
			return;
		}
		if (BuildConfig.OFFLINE_REGISTER_ENABLED){
			AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
			builder.setCancelable(false);
			builder.setTitle(error);
			builder.setMessage(R.string.offline_register_confirm);
			builder.setPositiveButton(R.string.register_offline, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					u.setOfflineRegister(true);
					executeRegisterTask(u);
				}
			});
			builder.setNegativeButton(R.string.cancel, null);
			builder.show();
		}
		else{
			UIUtils.showAlert(ctx,R.string.error,error);
		}
	}

	private void executeRegisterTask(User u){

		pDialog = new ProgressDialog(super.getActivity());
		pDialog.setTitle(R.string.register_alert_title);
		pDialog.setMessage(getString(R.string.register_process));
		pDialog.setCancelable(true);
		pDialog.show();

		Payload p = new Payload(Arrays.asList(u));
		RegisterTask rt = new RegisterTask(super.getActivity());
		rt.setRegisterListener(this);
		rt.execute(p);
	}
}
