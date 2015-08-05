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