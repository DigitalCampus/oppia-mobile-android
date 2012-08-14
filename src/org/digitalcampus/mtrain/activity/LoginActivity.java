package org.digitalcampus.mtrain.activity;

import org.apache.commons.validator.EmailValidator;
import org.digitalcampus.mtrain.R;
import org.digitalcampus.mtrain.listener.LoginListener;
import org.digitalcampus.mtrain.model.User;
import org.digitalcampus.mtrain.task.LoginTask;
import org.digitalcampus.mtrain.task.Payload;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class LoginActivity extends Activity implements OnSharedPreferenceChangeListener, LoginListener  {

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
		prefs.registerOnSharedPreferenceChangeListener(this);
		
        emailField = (EditText) findViewById(R.id.login_email_field);
        passwordField = (EditText) findViewById(R.id.login_password_field);
	}
	
	public void onLoginClick(View view){
		Log.d(TAG,"Logging in");
		String email = emailField.getText().toString();
		Log.d(TAG,email);
    	//check valid email address format
    	//boolean isValidEmail = EmailValidator.getInstance().isValid(email);
    	//if(!isValidEmail){
    	if(email.length()<4){
    		// TODO change to proper lang strings
    		this.showAlert("Error","Please enter a valid email address format");
    		return;
    	}
    	
    	// get text from email
    	String password = passwordField.getText().toString();
    	//check length
    	if(password.length()<6){
    		// TODO change to proper lang strings
    		this.showAlert("Error","You password should be 6 characters or more");
    		return;
    	}
    	
    	// show progress dialog
    	// TODO set proper lang strings
        pDialog = new ProgressDialog(this);
        pDialog.setTitle("Login");
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
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		// TODO Auto-generated method stub
		
	}
	
	public void onRegisterClick(View view){
		Log.d(TAG,"Registering");
		
	}
	
    private void showAlert(String title, String msg){
    	AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
		builder.setTitle(title);
		builder.setMessage(msg);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}

	     });
		builder.show();
    }

	public void loginComplete(Payload response) {
		pDialog.dismiss();
		Log.d(TAG,"Login activity reports: " + response.resultResponse);
		if(response.result){
			// set params
			Editor editor = prefs.edit();
	    	editor.putString("prefUsername", emailField.getText().toString());
	    	editor.putString("prefPassword", passwordField.getText().toString());
	    	editor.commit();
	    	
			// return to main activity
	    	Intent i = new Intent(this,MTrainActivity.class);
			startActivity(i);
			finish();
		} else {
			showAlert("Login", response.resultResponse);
		}
	}
}

