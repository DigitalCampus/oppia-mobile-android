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
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.exception.InvalidXMLException;
import org.digitalcampus.oppia.listener.InstallCourseListener;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.DownloadProgress;
import org.digitalcampus.oppia.utils.storage.Storage;
import org.digitalcampus.oppia.utils.xmlreaders.CourseScheduleXMLReader;
import org.digitalcampus.oppia.utils.xmlreaders.CourseTrackerXMLReader;
import org.digitalcampus.oppia.utils.xmlreaders.CourseXMLReader;
import org.digitalcampus.oppia.utils.SearchUtils;
import org.digitalcampus.oppia.utils.storage.FileUtils;

import java.io.File;
import java.util.Locale;

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
		File dir = new File(Storage.getDownloadPath(ctx));
		DownloadProgress dp = new DownloadProgress();
		String[] children = dir.list();
		if (children != null) {

			for (int i = 0; i < children.length; i++) {

				// extract to temp dir and check it's a valid package file
				File tempdir = new File(Storage.getStorageLocationRoot(ctx) + "temp/");
				tempdir.mkdirs();

                dp.setMessage(ctx.getString(R.string.installing_course, children[i]));
                dp.setProgress(0);
                publishProgress(dp);

				File testDir = new File(Storage.getDownloadPath(ctx), children[i]);
				
				if (testDir.isDirectory()){
					continue;
				}
				boolean unzipResult = FileUtils.unzipFiles(Storage.getDownloadPath(ctx), children[i], tempdir.getAbsolutePath());

				
				if (!unzipResult){
					//then was invalid zip file and should be removed
					FileUtils.cleanUp(tempdir, Storage.getDownloadPath(ctx) + children[i]);
					break;

				}
				String[] courseDirs = tempdir.list(); // use this to get the course name

                dp.setMessage(ctx.getString(R.string.installing_course, children[i]));
                dp.setProgress(10);
                publishProgress(dp);

				String courseXMLPath = "";
				String courseScheduleXMLPath = "";
				String courseTrackerXMLPath = "";
				// check that it's unzipped etc correctly
				try {
					courseXMLPath = tempdir + File.separator + courseDirs[0] + File.separator + MobileLearning.COURSE_XML;
					courseScheduleXMLPath = tempdir + File.separator + courseDirs[0] + File.separator + MobileLearning.COURSE_SCHEDULE_XML;
					courseTrackerXMLPath = tempdir + File.separator + courseDirs[0] + File.separator + MobileLearning.COURSE_TRACKER_XML;
				} catch (ArrayIndexOutOfBoundsException aioobe){
					FileUtils.cleanUp(tempdir, Storage.getDownloadPath(ctx) + children[i]);
					continue;
				}
				
				// check a module.xml file exists and is a readable XML file
				CourseXMLReader cxr;
				CourseScheduleXMLReader csxr;
				CourseTrackerXMLReader ctxr;
				try {
					cxr = new CourseXMLReader(courseXMLPath, 0, ctx);
					csxr = new CourseScheduleXMLReader(courseScheduleXMLPath);
					File trackerXML = new File(courseTrackerXMLPath);
					ctxr = new CourseTrackerXMLReader(trackerXML);
				} catch (InvalidXMLException e) {
					payload.setResult(false);
					return payload;
				}
				
				Course c = new Course(prefs.getString(PrefsActivity.PREF_STORAGE_LOCATION, ""));
				c.setVersionId(cxr.getVersionId());
				c.setTitles(cxr.getTitles());
				c.setShortname(courseDirs[0]);
				c.setImageFile(cxr.getCourseImage());
				c.setLangs(cxr.getLangs());
				c.setDescriptions(cxr.getDescriptions());
				c.setPriority(cxr.getPriority());
                String sequencingMode = cxr.getCourseSequencingMode();
                if ((sequencingMode!=null) && (sequencingMode.equals(Course.SEQUENCING_MODE_COURSE) ||
                        sequencingMode.equals(Course.SEQUENCING_MODE_SECTION) || sequencingMode.equals(Course.SEQUENCING_MODE_NONE))){
                    c.setSequencingMode(sequencingMode);
                }

				String title = c.getTitle(prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage()));
				
				dp.setMessage(ctx.getString(R.string.installing_course, title));
                dp.setProgress(20);
				publishProgress(dp);
				
				boolean success = false;
				
				DbHelper db = DbHelper.getInstance(ctx);
				long courseId = db.addOrUpdateCourse(c);
				if (courseId != -1) {
					payload.addResponseData(c);
					File src = new File(tempdir + File.separator + courseDirs[0]);
					File dest = new File(Storage.getCoursesPath(ctx));

					db.insertActivities(cxr.getActivities(courseId));
                    dp.setProgress(50);
                    publishProgress(dp);

                    long userId = db.getUserId(SessionManager.getUsername(ctx));
                    
                    db.resetCourse(courseId, userId);
					db.insertTrackers(ctxr.getTrackers(courseId, userId));
					db.insertQuizAttempts(ctxr.getQuizAttempts(courseId, userId));
					
                    dp.setProgress(70);
                    publishProgress(dp);

					// Delete old course
					File oldCourse = new File(Storage.getCoursesPath(ctx) + courseDirs[0]);
					FileUtils.deleteDir(oldCourse);

					// move from temp to courses dir
					success = src.renameTo(new File(dest, src.getName()));

					if (success) {
						// add the course to the search index
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
				db.updateScheduleVersion(courseId, csxr.getScheduleVersion());

                dp.setProgress(80);
                publishProgress(dp);

				if (success){
					SearchUtils.indexAddCourse(this.ctx, c);
				}
				
				// delete temp directory
				FileUtils.deleteDir(tempdir);

                dp.setProgress(95);
                publishProgress(dp);

				// delete zip file from download dir
				File zip = new File(Storage.getDownloadPath(ctx) + children[i]);
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
