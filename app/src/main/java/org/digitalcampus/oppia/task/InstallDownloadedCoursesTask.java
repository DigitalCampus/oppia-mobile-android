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

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.listener.InstallCourseListener;
import org.digitalcampus.oppia.model.DownloadProgress;
import org.digitalcampus.oppia.service.courseinstall.CourseInstall;
import org.digitalcampus.oppia.utils.storage.Storage;

import java.io.File;

public class InstallDownloadedCoursesTask extends AsyncTask<Payload, DownloadProgress, Payload>{
	
	public final static String TAG = InstallDownloadedCoursesTask.class.getSimpleName();
	private Context ctx;
	private InstallCourseListener mStateListener;
	
	public InstallDownloadedCoursesTask(Context ctx) {
		this.ctx = ctx;
	}

	@Override
	protected Payload doInBackground(Payload... params) {

		final Payload payload = params[0];

		File dir = new File(Storage.getDownloadPath(ctx));
		String[] children = dir.list();
		if (children != null) {

			for (final String course_filename : children) {

				CourseInstall.installDownloadedCourse(ctx, course_filename, "", new CourseInstall.CourseInstallingListener() {
					@Override
					public void onInstallProgress(int progress) {
						DownloadProgress dp = new DownloadProgress();
						dp.setMessage(ctx.getString(R.string.installing_course, course_filename));
						dp.setProgress(progress);
						publishProgress(dp);
					}

					@Override
					public void onError(String message) {
						payload.setResult(false);
						payload.setResultResponse(message);
					}

					@Override
					public void onFail(String message) {
						payload.setResult(false);
						payload.setResultResponse(message);
					}

					@Override
					public void onComplete() {
						payload.setResult(true);
					}
				});

			}
		}
		return payload;
	}

	@Override
	protected void onProgressUpdate(DownloadProgress... obj) {
		synchronized (this) {
            if (mStateListener != null) {
                // update progress and total
                mStateListener.installProgressUpdate(obj[0]);
            }
        }
	}

	@Override
	protected void onPostExecute(Payload p) {
		synchronized (this) {
            if (mStateListener != null) {
               mStateListener.installComplete(p);
            }
        }
	}

	public void setInstallerListener(InstallCourseListener srl) {
        synchronized (this) {
            mStateListener = srl;
        }
    }
	
}
