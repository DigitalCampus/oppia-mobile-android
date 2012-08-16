package org.digitalcampus.mtrain.activity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.digitalcampus.mtrain.R;
import org.digitalcampus.mtrain.adapter.DownloadListAdapter;
import org.digitalcampus.mtrain.application.DbHelper;
import org.digitalcampus.mtrain.application.MTrain;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ListView;

public class DownloadActivity extends Activity {

	public static final String TAG = "DownloadActivity";
	
	private ProgressDialog pDialog;
	private SharedPreferences prefs;
	private JSONArray json;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(this); 
        setContentView(R.layout.activity_download);
		// Get Module list
		getModuleList();
       
    }
	
	private void getModuleList(){
		// show progress dialog
        pDialog = new ProgressDialog(this);
        // TODO change these to be lang strings
        pDialog.setTitle("Loading");
        pDialog.setMessage("Getting list of modules");
        pDialog.setCancelable(true);
        pDialog.show();
        
        GetModuleListTask task = new GetModuleListTask();
        String[] url = new String[1];

        url[0] = prefs.getString("prefServer", getString(R.string.prefServerDefault))+MTrain.SERVER_MODULES_PATH;
        task.execute(url);
	}
	
	// TODO move this task to be its own class
	private class GetModuleListTask extends AsyncTask<String, String, String>{
    	
    	@Override
    	protected String doInBackground(String... urls){
    		
    		String toRet = "";
    		for (String url : urls) {
    			String response = "";
    			
    			HttpParams httpParameters = new BasicHttpParams();
    			int timeoutConnection = 10000;
    			try {
    				timeoutConnection = Integer.parseInt(prefs.getString("prefServerTimeoutConnection", "10000"));
    			} catch (NumberFormatException e){
    				// do nothing - will remain as default as above
    				e.printStackTrace();
    			}
    			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
    			int timeoutSocket = 10000;
    			try {
    				timeoutSocket= Integer.parseInt(prefs.getString("prefServerTimeoutConnection", "10000"));
    			} catch (NumberFormatException e){
    				// do nothing - will remain as default as above
    				e.printStackTrace();
    			}
    			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

    			DefaultHttpClient client = new DefaultHttpClient(httpParameters);
    			HttpPost httpPost = new HttpPost(url);
    			Log.d(TAG,"connecting to: "+url);
    			try {
    				// add post params
    				//List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
    				//nameValuePairs.add(new BasicNameValuePair("username", prefs.getString("prefUsername", "")));
    				//nameValuePairs.add(new BasicNameValuePair("password", prefs.getString("prefPassword", "")));
    				//httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
    				
    				// make request
					HttpResponse execute = client.execute(httpPost);
				
					// read response
					InputStream content = execute.getEntity().getContent();
					BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
					String s = "";
					while ((s = buffer.readLine()) != null) {
						response += s;
					}
					
					toRet = response;
					
    			} catch (Exception e) {
    				e.printStackTrace();
					toRet = "Connection error or invalid response from server";
				}
			}
			return toRet;
    	}
    	
    	@Override
    	protected void onPostExecute(String response) {
    		// close dialog and process results
    		pDialog.dismiss();
			try {
				json = new JSONArray(response);
				refreshModuleList();
			} catch (JSONException e){
				showAlert("Error", response);
				e.printStackTrace();
			}
			
    	}
    }
	
    public void refreshModuleList(){
    	//process the response and display on screen in listview
    	// Create an array of Strings, that will be put to our ListActivity
    	
    	try {
    		
    		ArrayList<DownloadModule> modules = new ArrayList<DownloadModule>();
    		DbHelper db = new DbHelper(this);
    		for(int i=0;i<(json.length());i++){
			    JSONObject json_obj=json.getJSONObject(i); 
			    DownloadModule dm = new DownloadModule();
			    dm.title = json_obj.getString("title");
			    dm.shortname = json_obj.getString("shortname");
			    dm.version = json_obj.getDouble("version");
			    dm.downloadUrl = json_obj.getString("url");
			    dm.installed = db.isInstalled(dm.shortname);
			    dm.toUpdate = db.toUpdate(dm.shortname, dm.version);
			    modules.add(dm);
			}
    		db.close();

    		DownloadListAdapter mla = new DownloadListAdapter(this, modules);
    		ListView listView = (ListView) findViewById(R.id.module_list);
    		listView.setAdapter(mla);
			
		} catch (Exception e){
			e.printStackTrace();
			showAlert("Error","Error processing server response");
		}
    	
		
    }
    
	private void showAlert(String title, String msg){
    	AlertDialog alertDialog = new AlertDialog.Builder(DownloadActivity.this).create();
		alertDialog.setTitle(title);
		alertDialog.setMessage(msg);
		alertDialog.setButton("Close", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}});
		alertDialog.show();
    }
	
	// TODO make this into proper class (with getters/setters etc)
	public class DownloadModule{
		public String title;
		public Double version;
		public String shortname;
		public String downloadUrl;
		public boolean installed = false;
		public boolean toUpdate = false;
		public DownloadModule(){
			
		}
	}
	
}
