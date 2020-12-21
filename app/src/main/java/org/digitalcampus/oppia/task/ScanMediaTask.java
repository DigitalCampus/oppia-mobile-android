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

package org.digitalcampus.oppia.task;

import android.content.Context;
import android.os.AsyncTask;

import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.listener.ScanMediaListener;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.Media;
import org.digitalcampus.oppia.service.DownloadService;
import org.digitalcampus.oppia.utils.storage.Storage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ScanMediaTask extends AsyncTask<Payload, String, Payload>{

	public static final String TAG = ScanMediaTask.class.getSimpleName();
	private ScanMediaListener mStateListener;
	private Context ctx;

	private ArrayList<Object> currentMedia;
	private ArrayList<String> downloadingMedia;
	
	public ScanMediaTask(Context ctx) {
		this.ctx = ctx;
	}

	@Override
	protected Payload doInBackground(Payload... params) {

		Payload payload = params[0];
		currentMedia = (ArrayList<Object>) payload.getResponseData();
		downloadingMedia = (ArrayList<String>) DownloadService.getTasksDownloading();
		DbHelper db = DbHelper.getInstance(ctx);

        List<?> courseObjs = payload.getData();
		for (int i=0; i<courseObjs.size(); i++){
			Course course = (Course) courseObjs.get(i);
			List<Media> courseMedia = db.getCourseMedia(course.getCourseId());
			for(Media m: courseMedia){
				publishProgress(m.getFilename());
				checkMedia(course, m, payload);
			}
		}

		return payload;
	}

	private void checkMedia(Course course, Media m, Payload result){
		String filename = Storage.getMediaPath(ctx) + m.getFilename();
		File mediaFile = new File(filename);
		if((!mediaFile.exists()) || ( (downloadingMedia!=null)&&(downloadingMedia.contains(m.getDownloadUrl())) )) {
			// check media not already in list
			boolean add = true;
			for (Object cm: currentMedia){
				//We have to add it if there is not other object with that filename
				add = !((Media) cm).getFilename().equals(m.getFilename());
				if(!add){ ((Media) cm).getCourses().add(course); break; }
			}
			if (add){
				m.getCourses().add(course);
				if (downloadingMedia!=null && downloadingMedia.contains(m.getDownloadUrl())){
					m.setDownloading(true);
				}
				result.addResponseData(m);
				result.setResult(true);
			}
		}
	}

	@Override
	protected void onPreExecute(){
		synchronized (this) {
            if (mStateListener != null) {
               mStateListener.scanStart();
            }
        }
	}
	
	@Override
	protected void onProgressUpdate(String... progress){
		synchronized (this) {
            if (mStateListener != null) {
                // update progress
                mStateListener.scanProgressUpdate(progress[0]);
            }
        }
	}
	
	@Override
	protected void onPostExecute(Payload response) {
		synchronized (this) {
            if (mStateListener != null) {
               mStateListener.scanComplete(response);
            }
        }
	}
	
	public void setScanMediaListener(ScanMediaListener srl) {
        synchronized (this) {
            mStateListener = srl;
        }
    }

}
