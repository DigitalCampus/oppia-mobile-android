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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.listener.PostInstallListener;
import org.digitalcampus.oppia.model.DownloadProgress;

import android.content.Context;
import android.os.AsyncTask;

public class PostInstallTask extends AsyncTask<Payload, DownloadProgress, Payload>{

	public final static String TAG = PostInstallTask.class.getSimpleName();
	private Context ctx;
	private PostInstallListener mStateListener;
	
	public PostInstallTask(Context ctx) {
		this.ctx = ctx;
	}
	
	@Override
	protected Payload doInBackground(Payload... params) {
		Payload payload = params[0];
		
		String[] directory;
		
		// copy over any courses not already installed
		try {
			directory = this.ctx.getAssets().list(MobileLearning.PRE_INSTALL_COURSES_DIR);
			for (int index = 0; index < directory.length; index++)  {   
		       if (directory[index].toString().endsWith(".zip")){
		    	   FileOutputStream f = new FileOutputStream(new File(MobileLearning.DOWNLOAD_PATH,directory[index].toString()));
		    	   InputStream is = this.ctx.getAssets().open(MobileLearning.PRE_INSTALL_COURSES_DIR + "/" + directory[index].toString());
		    	   byte[] buffer = new byte[1024];
		            int len = 0;
		            while ((len = is.read(buffer)) > 0) {
		                f.write(buffer, 0, len);
		            }
		            f.close();
		       }
		    }
		} catch (IOException e) {
			e.printStackTrace();
		}   

	    // copy over any media not already installed
		try {
			directory = this.ctx.getAssets().list(MobileLearning.PRE_INSTALL_MEDIA_DIR);
			for (int index = 0; index < directory.length; index++)  {   
		       if (!directory[index].toString().endsWith(".txt")){
		    	   FileOutputStream f = new FileOutputStream(new File(MobileLearning.MEDIA_PATH,directory[index].toString()));
		    	   InputStream is = this.ctx.getAssets().open(MobileLearning.PRE_INSTALL_MEDIA_DIR + "/" + directory[index].toString());
		    	   byte[] buffer = new byte[1024];
		            int len = 0;
		            while ((len = is.read(buffer)) > 0) {
		                f.write(buffer, 0, len);
		            }
		            f.close();
		       }
		    }
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return payload;
	}
	
	@Override
	protected void onPostExecute(Payload p) {
		synchronized (this) {
            if (mStateListener != null) {
               mStateListener.postInstallComplete(p);
            }
        }
	}
	
	public void setPostInstallListener(PostInstallListener pil) {
        synchronized (this) {
            mStateListener = pil;
        }
    }
}
