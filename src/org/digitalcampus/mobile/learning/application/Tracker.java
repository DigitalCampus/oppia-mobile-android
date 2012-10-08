package org.digitalcampus.mobile.learning.application;

import org.json.JSONException;
import org.json.JSONObject;

import com.bugsense.trace.BugSenseHandler;

import android.content.Context;
import android.util.Log;

public class Tracker {

	public static final String TAG = "Tracker"; 
	private final Context ctx;
	
	public Tracker(Context context){
		this.ctx = context;
	}
	
	public void activityComplete(int modId, String digest, JSONObject data){
		// add to the db log
		DbHelper db = new DbHelper(this.ctx);
		Log.d(TAG,data.toString());
		db.insertLog(modId, digest, data.toString());
		db.close();
	}
	
	public void mediaPlayed(int modId, String digest, String media){
		// add to the db log
		DbHelper db = new DbHelper(this.ctx); 
		JSONObject jsonObj = new JSONObject();
		try {
			jsonObj.put("media", "played");
			jsonObj.put("mediafile", media);
		} catch (JSONException e) {
			e.printStackTrace();
			BugSenseHandler.log(TAG, e);
		}
		String data = jsonObj.toString();
		db.insertLog(modId, digest, data);
		db.close();
	}

}
