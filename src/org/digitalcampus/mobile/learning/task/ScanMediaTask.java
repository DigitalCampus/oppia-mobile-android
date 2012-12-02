package org.digitalcampus.mobile.learning.task;

import java.io.File;

import org.digitalcampus.mobile.learning.application.MobileLearning;
import org.digitalcampus.mobile.learning.listener.ScanMediaListener;
import org.digitalcampus.mobile.learning.model.Media;

import android.os.AsyncTask;
import android.util.Log;

public class ScanMediaTask extends AsyncTask<Payload, Media, Payload>{

	private final static String TAG = "ScanMediaTask";
	private ScanMediaListener mStateListener;
	
	protected Payload doInBackground(Payload... params) {
		Payload payload = params[0];
		for (Object o: payload.data){
			Media m = (Media) o;
			Log.d(TAG,m.getFilename());
			publishProgress(m);
			String filename = MobileLearning.MEDIA_PATH + m.getFilename();
			File mediaFile = new File(filename);
			if(mediaFile.exists()){
				Log.d(TAG,"found: "+m.getFilename());
			} else {
				Log.d(TAG,"missing: "+m.getFilename());
				payload.responseData.add(m);
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
	protected void onProgressUpdate(Media... progress){
		synchronized (this) {
            if (mStateListener != null) {
                // update progress
                mStateListener.scanProgressUpdate(progress[0].getFilename());
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
