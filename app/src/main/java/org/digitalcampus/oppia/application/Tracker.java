/* 
 * This file is part of OppiaMobile - https://digital-campus.org/
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

import java.util.UUID;

import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.gamification.Gamification;
import org.digitalcampus.oppia.model.GamificationEvent;
import org.digitalcampus.oppia.utils.MetaDataUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;
import android.util.Log;

import com.splunk.mint.Mint;

public class Tracker {

	public static final String TAG = Tracker.class.getSimpleName();
	public static final String SEARCH_TYPE = "search";
	public static final String MISSING_MEDIA_TYPE = "missing_media";

	private final Context ctx;
	
	public Tracker(Context context){
		this.ctx = context;
	}

	private void saveTracker(int courseId, String digest, JSONObject data, String type, boolean completed, GamificationEvent gamificationEvent){
		// add tracker UUID
		UUID guid = java.util.UUID.randomUUID();
		try {
			data.put("uuid", guid.toString());
		} catch (JSONException e) {
			Mint.logException(e);
			Log.d(TAG, "error with uuid: ", e);
		}
		DbHelper db = DbHelper.getInstance(this.ctx);

		db.insertTracker(courseId, digest, data.toString(), type, completed, gamificationEvent.getEvent(), gamificationEvent.getPoints());

		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this.ctx).edit();
        long now = System.currentTimeMillis()/1000;
        editor.putLong(PrefsActivity.PREF_TRIGGER_POINTS_REFRESH, now).apply();
	}

	public void saveTracker(int courseId, String digest, JSONObject data, boolean completed, GamificationEvent gamificationEvent){
		saveTracker(courseId, digest, data, "", completed, gamificationEvent);
	}

    public void saveSearchTracker(String searchTerm, int count){

		try {
			JSONObject searchData = new MetaDataUtils(ctx).getMetaData();
			searchData.put("query", searchTerm);
			searchData.put("results_count", count);

			saveTracker(0, "", searchData, SEARCH_TYPE, true, Gamification.GAMIFICATION_SEARCH_PERFORMED);

		} catch (JSONException e) {
			Mint.logException(e);
			Log.d(TAG, "Errors saving search tracker: ", e);
		}
    }

	public void saveMissingMediaTracker(String filename){

		try {
			JSONObject missingMedia = new MetaDataUtils(ctx).getMetaData();
			missingMedia.put("filename", filename);
			saveTracker(0, "", missingMedia, MISSING_MEDIA_TYPE, true, Gamification.GAMIFICATION_MEDIA_MISSING);

		} catch (JSONException e) {
			Mint.logException(e);
			Log.d(TAG, "Error saving missing media tracker: ", e);
		}
	}

	public void saveRegisterTracker(){

		try {
			JSONObject registerData = new MetaDataUtils(ctx).getMetaData();
			saveTracker(0, "", registerData, Gamification.EVENT_NAME_REGISTER, true, Gamification.GAMIFICATION_REGISTER);

		} catch (JSONException e) {
			Mint.logException(e);
			Log.d(TAG, "Error saving register tracker:", e);
		}
	}

}
