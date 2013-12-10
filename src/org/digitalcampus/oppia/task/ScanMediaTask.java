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
import java.util.ArrayList;

import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.exception.InvalidXMLException;
import org.digitalcampus.oppia.listener.ScanMediaListener;
import org.digitalcampus.oppia.model.Media;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.utils.CourseXMLReader;

import android.os.AsyncTask;

public class ScanMediaTask extends AsyncTask<Payload, String, Payload>{

	public final static String TAG = ScanMediaTask.class.getSimpleName();
	private ScanMediaListener mStateListener;
	
	protected Payload doInBackground(Payload... params) {
		Payload payload = params[0];
		for (Object obj: payload.getData()){
			Course course = (Course) obj;
			CourseXMLReader mxr;
			try {
				mxr = new CourseXMLReader(course.getCourseXMLLocation());
				ArrayList<Media> media = mxr.getMedia();
				for(Media m: media){
					publishProgress(m.getFilename());
					String filename = MobileLearning.MEDIA_PATH + m.getFilename();
					File mediaFile = new File(filename);
					if(!mediaFile.exists()){
						payload.addResponseData(m);
						payload.setResult(true);
					}
				}
			} catch (InvalidXMLException e) {
				e.printStackTrace();
				payload.setResult(false);
			}
			
		}
		return payload;
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
