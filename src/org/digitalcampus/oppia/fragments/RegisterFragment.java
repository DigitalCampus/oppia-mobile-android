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

import java.util.ArrayList;
import java.util.Locale;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.OppiaMobileActivity;
import org.digitalcampus.oppia.activity.RegisterActivity;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.task.RegisterTask;
import org.digitalcampus.oppia.utils.FileUtils;

import com.bugsense.trace.BugSenseHandler;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class RegisterFragment extends Fragment{


	public static final String TAG = RegisterFragment.class.getSimpleName();
	private WebView webView;
	private SharedPreferences prefs;
	
	public static RegisterFragment newInstance() {
		RegisterFragment myFragment = new RegisterFragment();
	    return myFragment;
	}

	public RegisterFragment(){
		
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

		usernameField = (EditText) findViewById(R.id.register_form_username_field);
		emailField = (EditText) findViewById(R.id.register_form_email_field);
		passwordField = (EditText) findViewById(R.id.register_form_password_field);
		passwordAgainField = (EditText) findViewById(R.id.register_form_password_again_field);
		firstnameField = (EditText) findViewById(R.id.register_form_firstname_field);
		lastnameField = (EditText) findViewById(R.id.register_form_lastname_field);
	}

	public void submitComplete(Payload response) {
		pDialog.dismiss();
		if (response.isResult()) {
			User u = (User) response.getData().get(0);
			// set params
			Editor editor = prefs.edit();
	    	editor.putString(getString(R.string.prefs_username), usernameField.getText().toString());
	    	editor.putString(getString(R.string.prefs_api_key), u.getApi_key());
	    	editor.putString(getString(R.string.prefs_display_name), u.getDisplayName());
	    	editor.putInt(getString(R.string.prefs_points), u.getPoints());
	    	editor.putInt(getString(R.string.prefs_points), u.getBadges());
	    	editor.putBoolean(getString(R.string.prefs_scoring_enabled), u.isScoringEnabled());
	    	editor.putBoolean(getString(R.string.prefs_badging_enabled), u.isBadgingEnabled());
	    	editor.commit();

			showAlert("Register", "Registration successful", ONCLICK_TASK_REGISTERED);

		} else {
			showAlert("Register", response.getResultResponse(), ONCLICK_TASK_NULL);
		}

	}

	public void onRegisterClick(View view) {
		// get form fields
		String username = (String) usernameField.getText().toString();
		String email = (String) emailField.getText().toString();
		String password = (String) passwordField.getText().toString();
		String passwordAgain = (String) passwordAgainField.getText().toString();
		String firstname = (String) firstnameField.getText().toString();
		String lastname = (String) lastnameField.getText().toString();

		// do validation
		// TODO change to be proper lang strings
		// check firstname
		if (username.length() == 0) {
			this.showAlert(getString(R.string.error), "Please enter a username", ONCLICK_TASK_NULL);
			return;
		}
				
		// TODO check valid email address format
		// android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
		if (email.length() == 0) {
			this.showAlert(getString(R.string.error), "Please enter an email address", ONCLICK_TASK_NULL);
			return;
		}
		// check password length
		if (password.length() < MobileLearning.PASSWORD_MIN_LENGTH) {
			this.showAlert(getString(R.string.error), "Your password must be "+ MobileLearning.PASSWORD_MIN_LENGTH +" or more characters", ONCLICK_TASK_NULL);
			return;
		}
		// check password match
		if (!password.equals(passwordAgain)) {
			this.showAlert(getString(R.string.error), "Your passwords don't match", ONCLICK_TASK_NULL);
			return;
		}
		// check firstname
		if (firstname.length() < 2) {
			this.showAlert(getString(R.string.error), "Please enter your firstname", ONCLICK_TASK_NULL);
			return;
		}

		// check lastname
		if (lastname.length() < 2) {
			this.showAlert(getString(R.string.error), "Please enter your lastname", ONCLICK_TASK_NULL);
			return;
		}

		pDialog = new ProgressDialog(this);
		pDialog.setTitle("Register");
		pDialog.setMessage("Registering...");
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
		users.add(u);
		Payload p = new Payload(users);
		RegisterTask lt = new RegisterTask(this);
		lt.setLoginListener(this);
		lt.execute(p);
	}

	private void showAlert(String title, String msg, int onClickTask) {
		AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
		builder.setTitle(title);
		builder.setMessage(msg);
		switch (onClickTask) {
			case ONCLICK_TASK_NULL:
				builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
	
					public void onClick(DialogInterface dialog, int which) {
						// do nothing
	
					}
	
				});
				break;
			case ONCLICK_TASK_REGISTERED :
				builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						// return to main activity
						RegisterActivity.this.startActivity(new Intent(RegisterActivity.this, OppiaMobileActivity.class));
						RegisterActivity.this.finish();
					}
	
				});
				break;
		}
		builder.show();
	}
}
