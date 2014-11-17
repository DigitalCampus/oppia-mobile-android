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
import java.io.IOException;

import org.digitalcampus.oppia.utils.FileUtils;

import android.os.AsyncTask;
import android.util.Log;

public class MoveStorageLocationTask extends AsyncTask<Payload, Object, Payload> {
	
	public static final String TAG = MoveStorageLocationTask.class.getSimpleName();
	
	public static final int SOURCE = 0;
	public static final int DESTINATION = 1;
	
	
	public MoveStorageLocationTask() {
	}
	
	@Override
	protected Payload doInBackground(Payload... params) {
		Payload payload = params[0];
		
		File downloadSource = new File((String) payload.getData().get(SOURCE) + File.separator + FileUtils.APP_DOWNLOAD_DIR_NAME);
		File mediaSource = new File((String) payload.getData().get(SOURCE) + File.separator + FileUtils.APP_MEDIA_DIR_NAME);
		File courseSource = new File((String) payload.getData().get(SOURCE) + File.separator + FileUtils.APP_COURSES_DIR_NAME);
		File destination = new File((String) payload.getData().get(DESTINATION));
		
		Log.d(TAG,"Task source: " + downloadSource);
		Log.d(TAG,"Task destination: " + destination);
		
		boolean success = false;
		
		// delete anything that already exists in the destination dir
		try {
			org.apache.commons.io.FileUtils.forceDelete(new File (destination + File.separator + FileUtils.APP_DOWNLOAD_DIR_NAME ));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.d(TAG,"failed to delete: " + destination + File.separator + FileUtils.APP_DOWNLOAD_DIR_NAME );
			e.printStackTrace();
		}
		
		try {
			org.apache.commons.io.FileUtils.forceDelete(new File (destination + File.separator + FileUtils.APP_MEDIA_DIR_NAME ));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.d(TAG,"failed to delete: " + destination + File.separator + FileUtils.APP_MEDIA_DIR_NAME );
			e.printStackTrace();
		}
		
		try {
			org.apache.commons.io.FileUtils.forceDelete(new File (destination + File.separator + FileUtils.APP_COURSES_DIR_NAME ));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.d(TAG,"failed to delete: " + destination + File.separator + FileUtils.APP_COURSES_DIR_NAME );
			e.printStackTrace();
		}
		
		
		// now copy over 
		try {
			
			org.apache.commons.io.FileUtils.moveDirectoryToDirectory(downloadSource,destination,true);
			Log.d(TAG,"completed");
			success = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.d(TAG,"failed");
			e.printStackTrace();
		}

		try {
			org.apache.commons.io.FileUtils.moveDirectoryToDirectory(mediaSource,destination,true);
			Log.d(TAG,"completed");
			success = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.d(TAG,"failed");
			e.printStackTrace();
		}
		
		try {
			org.apache.commons.io.FileUtils.moveDirectoryToDirectory(courseSource,destination,true);
			Log.d(TAG,"completed");
			success = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.d(TAG,"failed");
			e.printStackTrace();
		}
		Log.d(TAG, String.valueOf(success));
		return payload;
	}
}
