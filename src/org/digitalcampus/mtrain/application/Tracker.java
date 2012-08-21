package org.digitalcampus.mtrain.application;

import org.json.JSONException;
import org.json.JSONObject;

import com.bugsense.trace.BugSenseHandler;

import android.content.Context;

public class Tracker {

	public static final String TAG = "Tracker"; 
	private final Context ctx;
	
	public Tracker(Context context){
		this.ctx = context;
	}
	
	public void activityComplete(int modId, String digest, long timeTaken, String lang){
		// add to the db log
		DbHelper db = new DbHelper(this.ctx); 
		JSONObject jsonObj = new JSONObject();
		try {
			jsonObj.put("activity", "completed");
			jsonObj.put("timetaken", timeTaken);
			jsonObj.put("lang", lang);
			String data = jsonObj.toString();
			db.insertLog(modId, digest, data);
		} catch (JSONException e) {
			e.printStackTrace();
			BugSenseHandler.log(TAG, e);
		}
		
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
