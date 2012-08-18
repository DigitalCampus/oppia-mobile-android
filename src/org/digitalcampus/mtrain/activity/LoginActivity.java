package org.digitalcampus.mtrain.activity;

import org.apache.commons.validator.EmailValidator;
import org.digitalcampus.mtrain.R;
import org.digitalcampus.mtrain.application.MTrain;
import org.digitalcampus.mtrain.listener.SubmitListener;
import org.digitalcampus.mtrain.model.User;
import org.digitalcampus.mtrain.task.LoginTask;
import org.digitalcampus.mtrain.task.Payload;

import android.app.Activity;
import android.app.ProgressDialog;
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

public class LoginActivity extends Activity implements SubmitListener  {

	public static final String TAG = "LoginActivity";
	private SharedPreferences prefs;
	
	private EditText emailField;
	private EditText passwordField;
	private ProgressDialog pDialog;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
        emailField = (EditText) findViewById(R.id.login_email_field);
        passwordField = (EditText) findViewById(R.id.login_password_field);
	}
	
	public void onLoginClick(View view){
		Log.d(TAG,"Logging in");
		String email = emailField.getText().toString();
		Log.d(TAG,email);
    	//check valid email address format
    	boolean isValidEmail = EmailValidator.getInstance().isValid(email);
    	if(!isValidEmail){
    		MTrain.showAlert(this,R.string.error,R.string.error_invalid_email_format);
    		return;
    	}
    	
    	// get text from email
    	String password = passwordField.getText().toString();
    	//check length
    	if(password.length()< MTrain.PASSWORD_MIN_LENGTH ){
    		MTrain.showAlert(this,R.string.error,getString(R.string.error_password_length,MTrain.PASSWORD_MIN_LENGTH));
    		return;
    	}
    	
    	// show progress dialog
    	// TODO set proper lang strings
        pDialog = new ProgressDialog(this);
        pDialog.setTitle(R.string.title_login);
        pDialog.setMessage("Logging in...");
        pDialog.setCancelable(true);
        pDialog.show();
        
    	User[] u = new User[1];
    	u[0] = new User();
    	u[0].username = email;
    	u[0].password = password;
    	Payload p = new Payload(0,u);
    	LoginTask lt = new LoginTask(this);
    	lt.setLoginListener(this);
    	lt.execute(p);
	}
	
	public void onRegisterClick(View view){
		Intent i = new Intent(this, RegisterActivity.class);
		startActivity(i);
		finish();
		
	}

	public void submitComplete(Payload response) {
		pDialog.dismiss();
		Log.d(TAG,"Login activity reports: " + response.resultResponse);
		if(response.result){
			// set params
			Editor editor = prefs.edit();
	    	editor.putString("prefUsername", emailField.getText().toString());
	    	editor.putString("prefPassword", passwordField.getText().toString());
	    	editor.commit();
	    	
			// return to main activity
			finish();
		} else {
			MTrain.showAlert(this, R.string.title_login, response.resultResponse);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_login, menu);
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

