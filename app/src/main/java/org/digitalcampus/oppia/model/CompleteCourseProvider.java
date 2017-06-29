package org.digitalcampus.oppia.model;

import android.content.Context;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.CourseIndexActivity;
import org.digitalcampus.oppia.exception.InvalidXMLException;
import org.digitalcampus.oppia.task.ParseCourseXMLTask;
import org.digitalcampus.oppia.utils.UIUtils;
import org.digitalcampus.oppia.utils.xmlreaders.CourseXMLReader;

import java.util.concurrent.Callable;



public class CompleteCourseProvider {

    public CompleteCourse getCompleteCourseSync(Context ctx, Course course){
        try {
            CourseXMLReader cxr = new CourseXMLReader(course.getCourseXMLLocation(), course.getCourseId(), ctx);
            cxr.parse(CourseXMLReader.ParseMode.COMPLETE);
            return cxr.getParsedCourse();
        } catch (InvalidXMLException e) {
            e.printStackTrace();
            showErrorMessage(ctx);
            return null;
        }
    }

    public void getCompleteCourseAsync(Context ctx, Course course){
        ParseCourseXMLTask task = new ParseCourseXMLTask(ctx);
        task.setListener((ParseCourseXMLTask.OnParseXmlListener) ctx);
        task.execute(course);
    }

    private void showErrorMessage(final Context ctx){
        UIUtils.showAlert(ctx, R.string.error, R.string.error_reading_xml, new Callable<Boolean>() {
            public Boolean call() throws Exception {
                ((CourseIndexActivity) ctx).finish();
                return true;
            }
        });
    }
}
