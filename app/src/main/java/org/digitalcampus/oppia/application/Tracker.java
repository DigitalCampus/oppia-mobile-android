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

import org.digitalcampus.oppia.utils.MetaDataUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

public class Tracker {

	public static final String TAG = Tracker.class.getSimpleName();
	public static final String SEARCH_TYPE = "search";
	public static final String MISSING_MEDIA_TYPE = "missing_media";

	private final Context ctx;
	
	public Tracker(Context context){
		this.ctx = context;
	}

	private void saveTracker(int courseId, String digest, JSONObject data, String type, boolean completed){
		// add tracker UUID
		UUID guid = java.util.UUID.randomUUID();
		try {
			data.put("uuid", guid.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		DbHelper db = DbHelper.getInstance(this.ctx);

		db.insertTracker(courseId, digest, data.toString(), type, completed);

	}

	public void saveTracker(int courseId, String digest, JSONObject data, boolean completed){
		saveTracker(courseId, digest, data, "", completed);
	}

    public void saveSearchTracker(String searchTerm, int count){

		try {
			JSONObject searchData = new JSONObject();
			searchData = new MetaDataUtils(ctx).getMetaData(searchData);
			searchData.put("query", searchTerm);
			searchData.put("results_count", count);

			saveTracker(0, "", searchData, SEARCH_TYPE, true);

		} catch (JSONException e) {
			e.printStackTrace();
		}
    }

	public void saveMissingMediaTracker(String filename){

		try {
			JSONObject missingMedia = new JSONObject();
			missingMedia = new MetaDataUtils(ctx).getMetaData(missingMedia);
			missingMedia.put("filename", filename);
			saveTracker(0, "", missingMedia, MISSING_MEDIA_TYPE, true);

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

}
