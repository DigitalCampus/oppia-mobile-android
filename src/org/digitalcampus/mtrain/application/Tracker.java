package org.digitalcampus.mtrain.application;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

public class Tracker {

	private final Context ctx;
	
	public Tracker(Context context){
		this.ctx = context;
	}
	
	public void activityComplete(int modId, String digest){
		// add to the db log
		DbHelper db = new DbHelper(this.ctx); 
		JSONObject jsonObj = new JSONObject();
		try {
			jsonObj.put("activity", "completed");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String data = jsonObj.toString();
		db.insertLog(modId, digest, data);
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String data = jsonObj.toString();
		db.insertLog(modId, digest, data);
		db.close();
	}

}
