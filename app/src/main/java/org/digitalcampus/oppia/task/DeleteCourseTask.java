package org.digitalcampus.oppia.task;

import android.content.Context;
import android.os.AsyncTask;

import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.listener.DeleteCourseListener;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.service.courseinstall.CourseInstall;
import org.digitalcampus.oppia.utils.storage.FileUtils;

import java.io.File;

public class DeleteCourseTask extends AsyncTask<Payload, String, Payload> {

    public static final String TAG = DeleteCourseTask.class.getSimpleName();

    private DeleteCourseListener mStateListener;
    private Context ctx;

    public DeleteCourseTask(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    protected Payload doInBackground(Payload... params) {

        Payload payload = params[0];

        try {
           Course course = (Course) payload.getData().get(0);

            DbHelper db = DbHelper.getInstance(ctx);
            db.deleteCourse(course.getCourseId());

            // remove files
            String courseLocation = course.getLocation();
            File f = new File(courseLocation);
            boolean exists = f.exists();
            boolean success = exists && FileUtils.deleteDir(f);

            File courseBackup = CourseInstall.savedBackupCourse(ctx, course.getShortname());
            if (success && courseBackup != null){
                FileUtils.deleteFile(courseBackup);
            }

            payload.setResult(success);
        }catch(NullPointerException npe){
            payload.setResult(false);
        }

        return payload;
    }

    @Override
    protected void onPostExecute(Payload response) {
        synchronized (this) {
            if (mStateListener != null) {
                mStateListener.onCourseDeletionComplete(response);
            }
        }
    }

    public void setOnDeleteCourseListener(DeleteCourseListener srl) {
        synchronized (this) {
            mStateListener = srl;
        }
    }

}
