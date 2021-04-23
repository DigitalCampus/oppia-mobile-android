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
import android.util.Log;

import org.digitalcampus.oppia.analytics.Analytics;
import org.digitalcampus.oppia.exception.InvalidXMLException;
import org.digitalcampus.oppia.model.CompleteCourse;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.utils.xmlreaders.CourseXMLReader;

public class ParseCourseXMLTask extends AsyncTask<Course, Object, CompleteCourse> {

    private static final String TAG = ParseCourseXMLTask.class.getSimpleName();

    //Interface to listen to this task results
    public interface OnParseXmlListener{
        void onParseComplete(CompleteCourse parsedCourse);
        void onParseError();
    }

    private Context ctx;
    private CompleteCourse parsedCourse;
    private OnParseXmlListener listener;

    public ParseCourseXMLTask(Context ctx){
        this.ctx = ctx;
    }

    public void setListener(OnParseXmlListener listener){
        this.listener = listener;
    }

    @Override
    protected CompleteCourse doInBackground(Course... courses) {

        Course courseToProcess = courses[0];
        try {
            CourseXMLReader cxr = new CourseXMLReader(
                    courseToProcess.getCourseXMLLocation(),
                    courseToProcess.getCourseId(),
                    this.ctx);
            boolean success = cxr.parse(CourseXMLReader.ParseMode.COMPLETE);
            if (success){
                parsedCourse = cxr.getParsedCourse();
            }

        } catch (InvalidXMLException ixmle) {
            Log.d(TAG,"Invalid course xml error",ixmle);
            Analytics.logException(ixmle);
        }
        return parsedCourse;
    }

    @Override
    protected void onPostExecute(CompleteCourse parseResults) {
        if (listener != null) {
            if (parsedCourse == null) {
                listener.onParseError();
            } else {
                listener.onParseComplete(parsedCourse);
            }
        }
    }

}