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

package org.digitalcampus.oppia.application;

import org.json.JSONException;
import org.json.JSONObject;

import com.bugsense.trace.BugSenseHandler;

import android.content.Context;
import android.util.Log;

public class Tracker {

	public static final String TAG = Tracker.class.getSimpleName(); 
	private final Context ctx;
	
	public Tracker(Context context){
		this.ctx = context;
	}
	
	public void saveTracker(int modId, String digest, JSONObject data, boolean completed){
		// add to the db log
		DbHelper db = new DbHelper(this.ctx);
		//Log.d(TAG,data.toString());
		db.insertLog(modId, digest, data.toString(), completed);
		db.close();
	}
	
	public void mediaPlayed(int modId, String digest, String media, long timetaken, boolean completed){
		// add to the db log
		DbHelper db = new DbHelper(this.ctx); 
		JSONObject jsonObj = new JSONObject();
		try {
			jsonObj.put("media", "played");
			jsonObj.put("mediafile", media);
			jsonObj.put("timetaken", timetaken);
		} catch (JSONException e) {
			e.printStackTrace();
			BugSenseHandler.sendException(e);
		}
		String data = jsonObj.toString();
		db.insertLog(modId, digest, data, completed);
		db.close();
	}

}
