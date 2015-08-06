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

import org.digitalcampus.oppia.exception.InvalidXMLException;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.utils.xmlreaders.CourseXMLReader;

public class ParseCourseXMLTask extends AsyncTask<Course, Object, CourseXMLReader> {

    //Interface to listen to this task results
    public interface OnParseXmlListener{
        void onParseComplete(CourseXMLReader parsed);
        void onParseError();
    }

    private Course courseToProcess;
    private CourseXMLReader cxr;
    private Context ctx;
    private boolean completeParse = true;
    private OnParseXmlListener listener;

    public ParseCourseXMLTask(Context ctx, boolean complete){
        this.ctx = ctx;
        completeParse = complete;
    }

    public void setListener(OnParseXmlListener listener){
        this.listener = listener;
    }

    @Override
    protected CourseXMLReader doInBackground(Course... courses) {

        courseToProcess = courses[0];
        try {
            cxr = new CourseXMLReader(
                    courseToProcess.getCourseXMLLocation(),
                    courseToProcess.getCourseId(),
                    this.ctx);

            if (completeParse){
                cxr.getSections(courseToProcess.getCourseId());
            }
            else{
                cxr.getDescriptions();
            }

        } catch (InvalidXMLException e) {
            e.printStackTrace();
        }
        return cxr;
    }

    @Override
    protected void onPostExecute(CourseXMLReader parseResults) {
        if (listener != null) {
            if (cxr == null) {
                listener.onParseError();
            } else {
                listener.onParseComplete(cxr);
            }
        }
    }

}