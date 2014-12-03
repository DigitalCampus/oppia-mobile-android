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

package org.digitalcampus.oppia.utils;

import java.io.IOException;
import java.util.ArrayList;

import org.digitalcampus.oppia.application.DatabaseManager;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.exception.InvalidXMLException;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.task.Payload;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class SearchUtils {

	public static final String TAG = SearchUtils.class.getSimpleName();
	
	public static void reindexAll(Context ctx){
		SearchReIndexTask task = new SearchReIndexTask(ctx);
		Payload p = new Payload();
		task.execute(p);
	}
	
	public static void indexAddCourse(Context ctx, Course course){
		
		try {
			CourseXMLReader cxr = new CourseXMLReader(course.getCourseXMLLocation(),ctx);
			ArrayList<Activity> activities = cxr.getActivities(course.getCourseId());
			for( Activity a : activities){
				ArrayList<Lang> langs = course.getLangs();
				String fileContent = "";
				for (Lang l : langs){
					if (a.getLocation(l.getLang()) != null){
						String url = course.getLocation() + a.getLocation(l.getLang());
						try {
							fileContent += " " + FileUtils.readFile(url);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				
				if (!fileContent.equals("")){
					DbHelper db = new DbHelper(ctx);
					db.insertActivityIntoSearchTable(course.getTitleJSONString(),
							cxr.getSection(a.getSectionId()).getTitleJSONString(),
							a.getTitleJSONString(),
							db.getActivityByDigest(a.getDigest()).getDbId(), 
							fileContent);
					DatabaseManager.getInstance().closeDatabase();
				}
			
			}
		} catch (InvalidXMLException e) {
			// Ignore course
		}
		
	}
	
	

	private static class SearchReIndexTask extends AsyncTask<Payload, String, Payload> {
		
		private Context ctx;
		
		public SearchReIndexTask(Context ctx){
			this.ctx = ctx;
		}
		
		@Override
		protected Payload doInBackground(Payload... params) {
			Payload payload = params[0];
			DbHelper db = new DbHelper(ctx);
			db.deleteSearchIndex();
			ArrayList<Course> courses  = db.getAllCourses();
			DatabaseManager.getInstance().closeDatabase();
			for (Course c : courses){
				Log.d(TAG,"indexing: "+ c.getTitle("en"));
				SearchUtils.indexAddCourse(ctx,c);
			}

			
			return payload;
		}
	}
}
