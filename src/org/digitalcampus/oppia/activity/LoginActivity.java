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

package org.digitalcampus.oppia.activity;

import java.util.ArrayList;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.listener.SubmitListener;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.task.LoginTask;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.utils.UIUtils;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class LoginActivity extends AppActivity implements SubmitListener  {

	public static final String TAG = LoginActivity.class.getSimpleName();
	private SharedPreferences prefs;
	
	private EditText usernameField;
	private EditText passwordField;
	private ProgressDialog pDialog;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		this.drawHeader();
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		usernameField = (EditText) findViewById(R.id.login_username_field);
        passwordField = (EditText) findViewById(R.id.login_password_field);
	}
	
	public void onLoginClick(View view){
		String username = usernameField.getText().toString();
    	//check valid email address format
    	if(username.length() == 0){
    		UIUtils.showAlert(this,R.string.error,R.string.error_no_username);
    		return;
    	}
    	
    	String password = passwordField.getText().toString();
    	
    	// show progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setTitle(R.string.title_login);
        pDialog.setMessage(this.getString(R.string.login_process));
        pDialog.setCancelable(true);
        pDialog.show();
        
    	ArrayList<Object> users = new ArrayList<Object>();
    	User u = new User();
    	u.setUsername(username);
    	u.setPassword(password);
    	users.add(u);
    	
    	Payload p = new Payload(users);
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
		try {
			pDialog.dismiss();
		} catch (IllegalArgumentException iae){
			//
		}
		if(response.isResult()){
			User u = (User) response.getData().get(0);
			// set params
			Editor editor = prefs.edit();
	    	editor.putString(getString(R.string.prefs_username), usernameField.getText().toString());
	    	editor.putString(getString(R.string.prefs_api_key), u.getApi_key());
	    	editor.putString(getString(R.string.prefs_display_name), u.getDisplayName());
	    	editor.putInt(getString(R.string.prefs_points), u.getPoints());
	    	editor.putInt(getString(R.string.prefs_badges), u.getBadges());
	    	editor.commit();
	    	
			// return to main activity
			finish();
		} else {
			UIUtils.showAlert(this, R.string.title_login, response.getResultResponse());
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

