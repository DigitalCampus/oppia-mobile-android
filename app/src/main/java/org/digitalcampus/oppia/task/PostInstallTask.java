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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.listener.PostInstallListener;
import org.digitalcampus.oppia.model.DownloadProgress;
import org.digitalcampus.oppia.utils.storage.Storage;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

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
		
		String[] courseDirectory = null;
		
		// copy over any courses not already installed
        try {
            courseDirectory = this.ctx.getAssets().list(MobileLearning.PRE_INSTALL_COURSES_DIR);
        } catch (IOException e) {
            Log.d(TAG,"couldn't open pre-install course directory", e);
        }

        if( courseDirectory!=null){
            for (String file : courseDirectory) {
                if (file.endsWith(".zip")) {
                    FileOutputStream f = null;
                    InputStream is = null;
                    try {
                        f = new FileOutputStream(new File(Storage.getDownloadPath(ctx), file));
                        is = this.ctx.getAssets().open(MobileLearning.PRE_INSTALL_COURSES_DIR + File.separator + file);
                        byte[] buffer = new byte[1024];
                        int len = 0;
                        while ((len = is.read(buffer)) > 0) {
                            f.write(buffer, 0, len);
                        }
                    } catch (IOException e) {
                        Log.d(TAG,"couldn't open course file", e);
                    } finally {
                        if (is != null){
                            try {
                                is.close();
                            } catch (IOException ioe) {
                                Log.d(TAG, "couldn't close InputStream object", ioe);
                            }
                        }
                        if (f != null){
                            try {
                                f.close();
                            } catch (IOException ioe) {
                                Log.d(TAG, "couldn't close FileOutputStream object", ioe);
                            }
                        }
                    }
                }
            }
		}

        String[] mediaDirectory = null;

        try {
            mediaDirectory = this.ctx.getAssets().list(MobileLearning.PRE_INSTALL_MEDIA_DIR);
        } catch (IOException e) {
            Log.d(TAG,"couldn't open pre-install media directory", e);
        }
	    // copy over any media not already installed
        if( mediaDirectory!=null){
            for (String file : mediaDirectory) {
                if (!file.endsWith(".txt")) {
                    FileOutputStream f = null;
                    InputStream is = null;
                    try {
                        f = new FileOutputStream(new File(Storage.getMediaPath(ctx), file));
                        is = this.ctx.getAssets().open(MobileLearning.PRE_INSTALL_MEDIA_DIR + File.separator + file);
                        byte[] buffer = new byte[1024];
                        int len = 0;
                        while ((len = is.read(buffer)) > 0) {
                            f.write(buffer, 0, len);
                        }
                    } catch (IOException e) {
                        Log.d(TAG,"couldn't open course file", e);
                    } finally {
                        if (is != null){
                            try {
                                is.close();
                            } catch (IOException ioe) {
                                Log.d(TAG, "couldn't close InputStream object", ioe);
                            }
                        }
                        if (f != null){
                            try {
                                f.close();
                            } catch (IOException ioe) {
                                Log.d(TAG, "couldn't close FileOutputStream object", ioe);
                            }
                        }
                    }
                }
            }
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
