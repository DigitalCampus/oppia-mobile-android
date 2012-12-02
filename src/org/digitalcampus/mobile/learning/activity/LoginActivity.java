package org.digitalcampus.mobile.learning.activity;

import java.util.ArrayList;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.application.MobileLearning;
import org.digitalcampus.mobile.learning.listener.SubmitListener;
import org.digitalcampus.mobile.learning.model.User;
import org.digitalcampus.mobile.learning.task.LoginTask;
import org.digitalcampus.mobile.learning.task.Payload;

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
	
	private EditText usernameField;
	private EditText passwordField;
	private ProgressDialog pDialog;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		usernameField = (EditText) findViewById(R.id.login_username_field);
        passwordField = (EditText) findViewById(R.id.login_password_field);
	}
	
	public void onLoginClick(View view){
		String username = usernameField.getText().toString();
    	//check valid email address format
    	if(username.length() == 0){
    		MobileLearning.showAlert(this,R.string.error,R.string.error_no_username);
    		return;
    	}
    	
    	String password = passwordField.getText().toString();
    	
    	// show progress dialog
    	// TODO set proper lang strings
        pDialog = new ProgressDialog(this);
        pDialog.setTitle(R.string.title_login);
        pDialog.setMessage("Logging in...");
        pDialog.setCancelable(true);
        pDialog.show();
        
    	ArrayList<Object> users = new ArrayList<Object>();
    	User u = new User();
    	u.username = username;
    	u.password = password;
    	users.add(u);
    	
    	Payload p = new Payload(0,users);
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
			User u = (User) response.data.get(0);
			// set params
			Editor editor = prefs.edit();
	    	editor.putString("prefUsername", usernameField.getText().toString());
	    	editor.putString("prefApiKey", u.api_key);
	    	editor.commit();
	    	
			// return to main activity
			finish();
		} else {
			MobileLearning.showAlert(this, R.string.title_login, response.resultResponse);
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

