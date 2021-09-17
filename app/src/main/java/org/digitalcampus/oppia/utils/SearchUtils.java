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

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.digitalcampus.oppia.analytics.Analytics;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.exception.InvalidXMLException;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.CompleteCourse;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.utils.xmlreaders.CourseXMLReader;

import java.util.ArrayList;
import java.util.List;

public class SearchUtils {

	public static final String TAG = SearchUtils.class.getSimpleName();

    private SearchUtils() {
        throw new IllegalStateException("Utility class");
    }

	public static void reindexAll(Context ctx){
		SearchReIndexTask task = new SearchReIndexTask(ctx);
		task.execute();
	}

    public static void indexAddCourse(Context ctx, CompleteCourse course){
        ArrayList<Activity> activities = (ArrayList<Activity>) course.getActivities(course.getCourseId());
        DbHelper db = DbHelper.getInstance(ctx);

        db.beginTransaction();
        for( Activity a : activities) {
            ArrayList<Lang> langs = (ArrayList<Lang>) course.getLangs();
            StringBuilder fileContents = new StringBuilder();

            for (Lang l : langs) {
                String langContents = a.getFileContents(course.getLocation(), l.getLanguage());
                if (langContents == null){
                    continue;
                }
                // strip out all html tags from string (not needed for search)
                langContents = langContents.replaceAll("\\<.*?\\>", "").trim();
                fileContents.append(langContents);
            }

		    Activity act = db.getActivityByDigest(a.getDigest());
            if ((act != null) && (fileContents.length() > 0)) {
                db.insertActivityIntoSearchTable(course.getTitleJSONString(),
                        course.getSection(a.getSectionId()).getTitleJSONString(),
                        a.getTitleJSONString(),
                        act.getDbId(),
                        fileContents.toString());
            }
        }
        db.endTransaction(true);
    }
	
	public static void indexAddCourse(Context ctx, Course course){
        try {
            CourseXMLReader cxr = new CourseXMLReader(course.getCourseXMLLocation(),course.getCourseId(), ctx);
            cxr.parse(CourseXMLReader.ParseMode.COMPLETE);
            indexAddCourse(ctx, cxr.getParsedCourse());
        } catch (InvalidXMLException e) {
            // Ignore course
            Analytics.logException(e);
            Log.d(TAG, "InvalidXMLException:", e);
        }
	}


	private static class SearchReIndexTask extends AsyncTask<Void, Void, Void> {
		
		private Context ctx;
		
		public SearchReIndexTask(Context ctx){
			this.ctx = ctx;
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			DbHelper db = DbHelper.getInstance(ctx);
			db.deleteSearchIndex();
			List<Course> courses  = db.getAllCourses();
			for (Course c : courses){
				Log.d(TAG,"indexing: " + c.getTitle("en"));
				SearchUtils.indexAddCourse(ctx, c);
			}

            return null;
        }
	}
}
