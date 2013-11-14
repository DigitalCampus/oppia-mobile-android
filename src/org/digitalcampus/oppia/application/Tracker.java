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

import org.json.JSONObject;

import android.content.Context;

public class Tracker {

	public static final String TAG = Tracker.class.getSimpleName(); 
	private final Context ctx;
	
	public Tracker(Context context){
		this.ctx = context;
	}
	
	public void saveTracker(int modId, String digest, JSONObject data, boolean completed){
		DbHelper db = new DbHelper(this.ctx);
		db.insertLog(modId, digest, data.toString(), completed);
		db.close();
	}

}
