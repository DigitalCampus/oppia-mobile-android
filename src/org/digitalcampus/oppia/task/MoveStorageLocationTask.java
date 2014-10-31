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

package org.digitalcampus.oppia.task;

import java.io.File;

import org.digitalcampus.oppia.utils.FileUtils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class MoveStorageLocationTask extends AsyncTask<Payload, Object, Payload> {
	
	public static final String TAG = MoveStorageLocationTask.class.getSimpleName();
	
	public static final int SOURCE = 0;
	public static final int DESTINATION = 1;
	
	private Context ctx;
	
	public MoveStorageLocationTask(Context c) {
		this.ctx = c;
	}
	
	@Override
	protected Payload doInBackground(Payload... params) {
		Payload payload = params[0];
		
		File source = new File((String) payload.getData().get(SOURCE) + "/" + FileUtils.APP_ROOT_DIR_NAME + "/");
		File destination = new File((String) payload.getData().get(DESTINATION) + "/" + FileUtils.APP_ROOT_DIR_NAME + "/");
		
		Log.d(TAG,"Task source: " + source);
		Log.d(TAG,"Task destination: " + destination);
		
		boolean success = false;
		
		success = source.renameTo(destination);

		Log.d(TAG, String.valueOf(success));
		return payload;
	}
}
