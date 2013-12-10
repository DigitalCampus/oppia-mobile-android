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
import java.util.Locale;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.exception.InvalidXMLException;
import org.digitalcampus.oppia.listener.InstallCourseListener;
import org.digitalcampus.oppia.model.DownloadProgress;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.utils.FileUtils;
import org.digitalcampus.oppia.utils.CourseScheduleXMLReader;
import org.digitalcampus.oppia.utils.CourseTrackerXMLReader;
import org.digitalcampus.oppia.utils.CourseXMLReader;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

public class InstallDownloadedCoursesTask extends AsyncTask<Payload, DownloadProgress, Payload>{
	
	public final static String TAG = InstallDownloadedCoursesTask.class.getSimpleName();
	private Context ctx;
	private InstallCourseListener mStateListener;
	private SharedPreferences prefs;
	
	public InstallDownloadedCoursesTask(Context ctx) {
		this.ctx = ctx;
		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
	}

	@Override
	protected Payload doInBackground(Payload... params) {

		Payload payload = params[0];
		
		// get folder
		File dir = new File(MobileLearning.DOWNLOAD_PATH);
		DownloadProgress dp = new DownloadProgress();
		String[] children = dir.list();
		if (children != null) {

			for (int i = 0; i < children.length; i++) {

				// extract to temp dir and check it's a valid package file
				File tempdir = new File(MobileLearning.OPPIAMOBILE_ROOT + "temp/");
				tempdir.mkdirs();
				boolean unzipResult = FileUtils.unzipFiles(MobileLearning.DOWNLOAD_PATH, children[i], tempdir.getAbsolutePath());
				
				if (!unzipResult){
					//then was invalid zip file and should be removed
					FileUtils.cleanUp(tempdir, MobileLearning.DOWNLOAD_PATH + children[i]);
					break;
				}
				String[] courseDirs = tempdir.list(); // use this to get the course
													// name
				
				String courseXMLPath = "";
				String courseScheduleXMLPath = "";
				String courseTrackerXMLPath = "";
				// check that it's unzipped etc correctly
				try {
					courseXMLPath = tempdir + "/" + courseDirs[0] + "/" + MobileLearning.COURSE_XML;
					courseScheduleXMLPath = tempdir + "/" + courseDirs[0] + "/" + MobileLearning.COURSE_SCHEDULE_XML;
					courseTrackerXMLPath = tempdir + "/" + courseDirs[0] + "/" + MobileLearning.COURSE_TRACKER_XML;
				} catch (ArrayIndexOutOfBoundsException aioobe){
					FileUtils.cleanUp(tempdir, MobileLearning.DOWNLOAD_PATH + children[i]);
					break;
				}
				
				// check a module.xml file exists and is a readable XML file
				CourseXMLReader cxr;
				CourseScheduleXMLReader csxr;
				CourseTrackerXMLReader ctxr;
				try {
					cxr = new CourseXMLReader(courseXMLPath);
					csxr = new CourseScheduleXMLReader(courseScheduleXMLPath);
					ctxr = new CourseTrackerXMLReader(courseTrackerXMLPath);
				} catch (InvalidXMLException e) {
					payload.setResult(false);
					return payload;
				}
				
				
				//HashMap<String, String> hm = mxr.getMeta();
				Course c = new Course();
				c.setVersionId(cxr.getVersionId());
				c.setTitles(cxr.getTitles());
				c.setLocation(MobileLearning.COURSES_PATH + courseDirs[0]);
				c.setShortname(courseDirs[0]);
				c.setImageFile(MobileLearning.COURSES_PATH + courseDirs[0] + "/" + cxr.getCourseImage());
				c.setLangs(cxr.getLangs());
				String title = c.getTitle(prefs.getString(ctx.getString(R.string.prefs_language), Locale.getDefault().getLanguage()));
				
				dp.setMessage(ctx.getString(R.string.installing_course, title));
				publishProgress(dp);
				
				DbHelper db = new DbHelper(ctx);
				long added = db.addOrUpdateCourse(c);
				
				if (added != -1) {
					payload.addResponseData(c);
					File src = new File(tempdir + "/" + courseDirs[0]);
					File dest = new File(MobileLearning.COURSES_PATH);

					db.insertActivities(cxr.getActivities(added));
					db.insertTrackers(ctxr.getTrackers(),added);
					// Delete old course
					File oldCourse = new File(MobileLearning.COURSES_PATH + courseDirs[0]);
					FileUtils.deleteDir(oldCourse);

					// move from temp to courses dir
					boolean success = src.renameTo(new File(dest, src.getName()));

					if (success) {
						payload.setResult(true);
						payload.setResultResponse(ctx.getString(R.string.install_course_complete, title));
					} else {
						payload.setResult(false);
						payload.setResultResponse(ctx.getString(R.string.error_installing_course, title));
					}
				}  else {
					payload.setResult(false);
					payload.setResultResponse(ctx.getString(R.string.error_latest_already_installed, title));
				}
				
				// add schedule
				// put this here so even if the course content isn't updated the schedule will be
				db.insertSchedule(csxr.getSchedule());
				db.updateScheduleVersion(added, csxr.getScheduleVersion());
				
				
				db.close();
				// delete temp directory
				FileUtils.deleteDir(tempdir);

				// delete zip file from download dir
				File zip = new File(MobileLearning.DOWNLOAD_PATH + children[i]);
				zip.delete();
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
