package org.digitalcampus.mobile.learning.task;

import java.io.File;

import org.digitalcampus.mobile.learning.application.MobileLearning;
import org.digitalcampus.mobile.learning.listener.ScanMediaListener;
import org.digitalcampus.mobile.learning.model.Media;

import android.os.AsyncTask;

public class ScanMediaTask extends AsyncTask<Payload, String, Payload>{

	public final static String TAG = ScanMediaTask.class.getSimpleName();
	private ScanMediaListener mStateListener;
	
	protected Payload doInBackground(Payload... params) {
		Payload payload = params[0];
		for (Object o: payload.data){
			Media m = (Media) o;
			publishProgress(m.getFilename());
			String filename = MobileLearning.MEDIA_PATH + m.getFilename();
			File mediaFile = new File(filename);
			if(!mediaFile.exists()){
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
