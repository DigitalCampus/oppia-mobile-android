package org.digitalcampus.mtrain.activity;

import org.apache.commons.validator.EmailValidator;
import org.digitalcampus.mtrain.R;
import org.digitalcampus.mtrain.application.MTrain;
import org.digitalcampus.mtrain.listener.SubmitListener;
import org.digitalcampus.mtrain.model.User;
import org.digitalcampus.mtrain.task.Payload;
import org.digitalcampus.mtrain.task.RegisterTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class RegisterActivity extends Activity implements SubmitListener {

	public static final String TAG = "RegisterActivity";

	private static final int ONCLICK_TASK_NULL = 0;
	private static final int ONCLICK_TASK_REGISTERED = 10;

	private SharedPreferences prefs;
	private EditText emailField;
	private EditText passwordField;
	private EditText passwordAgainField;
	private EditText firstnameField;
	private EditText lastnameField;
	private ProgressDialog pDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		emailField = (EditText) findViewById(R.id.register_form_email_field);
		passwordField = (EditText) findViewById(R.id.register_form_password_field);
		passwordAgainField = (EditText) findViewById(R.id.register_form_password_again_field);
		firstnameField = (EditText) findViewById(R.id.register_form_firstname_field);
		lastnameField = (EditText) findViewById(R.id.register_form_lastname_field);
	}

	public void submitComplete(Payload response) {
		pDialog.dismiss();
		Log.d(TAG, "Login activity reports: " + response.resultResponse);
		if (response.result) {
			// set params
			Editor editor = prefs.edit();
			editor.putString("prefUsername", emailField.getText().toString());
			editor.putString("prefPassword", passwordField.getText().toString());
			editor.commit();

			showAlert("Register", "Registration successful", ONCLICK_TASK_REGISTERED);

		} else {
			showAlert("Register", response.resultResponse, ONCLICK_TASK_NULL);
		}

	}

	public void onRegisterClick(View view) {
		// get form fields
		String email = (String) emailField.getText().toString();
		String password = (String) passwordField.getText().toString();
		String passwordAgain = (String) passwordAgainField.getText().toString();
		String firstname = (String) firstnameField.getText().toString();
		String lastname = (String) lastnameField.getText().toString();

		// do validation
		// check valid email address format
		boolean isValidEmail = EmailValidator.getInstance().isValid(email);
		if (!isValidEmail) {
			this.showAlert(getString(R.string.error), "Please enter a valid email address format", ONCLICK_TASK_NULL);
			return;
		}
		// check password length
		if (password.length() < MTrain.PASSWORD_MIN_LENGTH) {
			this.showAlert(getString(R.string.error), "Your password must be "+ MTrain.PASSWORD_MIN_LENGTH +" or more characters", ONCLICK_TASK_NULL);
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

		User[] u = new User[1];
		u[0] = new User();
		u[0].username = email;
		u[0].password = password;
		u[0].passwordAgain = passwordAgain;
		u[0].firstname = firstname;
		u[0].lastname = lastname;

		Payload p = new Payload(0, u);
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
						RegisterActivity.this.finish();
					}
	
				});
				break;
		}
		builder.show();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_register, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menu_settings:
			Intent i = new Intent(this, PrefsActivity.class);
			startActivity(i);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
