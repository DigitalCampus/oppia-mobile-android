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

import org.digitalcampus.oppia.application.App;
import org.digitalcampus.oppia.listener.PostInstallListener;
import org.digitalcampus.oppia.model.DownloadProgress;
import org.digitalcampus.oppia.utils.storage.Storage;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class PostInstallTask extends AsyncTask<Payload, DownloadProgress, Payload>{

	public static final String TAG = PostInstallTask.class.getSimpleName();
	private Context ctx;
	private PostInstallListener mStateListener;
	
	public PostInstallTask(Context ctx) {
		this.ctx = ctx;
	}
	
	@Override
	protected Payload doInBackground(Payload... params) {
		Payload payload = params[0];
		copyCourses();
        copyMedia();
		
		return payload;
	}

	private void copyCourses(){
        copyAssetFilesWithExtension(App.PRE_INSTALL_COURSES_DIR, "zip", Storage.getDownloadPath(ctx));
    }

    private void copyMedia(){
        copyAssetFilesWithExtension(App.PRE_INSTALL_MEDIA_DIR, null, Storage.getMediaPath(ctx));
    }

    private void copyAssetFilesWithExtension(String assetsDir, String extension, String destinationPath){

        String[] directory = null;
        try {
            directory = this.ctx.getAssets().list(assetsDir);
        } catch (IOException e) {
            Log.d(TAG,"couldn't open pre-install course directory", e);
        }

        if( directory != null){
            for (String file : directory) {
                if (extension == null || file.endsWith(extension)) {
                    copyFile(file, assetsDir, destinationPath);
                }
            }
        }
    }


	private void copyFile(String file, String sourcePath, String destinationPath){
        try (
                InputStream is = this.ctx.getAssets().open(sourcePath + File.separator + file);
                FileOutputStream f = new FileOutputStream(new File(destinationPath, file))
        ) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) > 0) {
                f.write(buffer, 0, len);
            }
        } catch (IOException e) {
            Log.d(TAG,"couldn't open course file", e);
        }
    }
	
	@Override
	protected void onPostExecute(Payload p) {
		synchronized (this) {
            if (mStateListener != null) {
               mStateListener.postInstallComplete();
            }
        }
	}
	
	public void setPostInstallListener(PostInstallListener pil) {
        synchronized (this) {
            mStateListener = pil;
        }
    }
}
