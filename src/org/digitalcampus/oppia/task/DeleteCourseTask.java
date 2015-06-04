package org.digitalcampus.oppia.task;

import android.content.Context;
import android.os.AsyncTask;

import org.digitalcampus.oppia.application.DatabaseManager;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.listener.DeleteCourseListener;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.utils.storage.FileUtils;

import java.io.File;

public class DeleteCourseTask extends AsyncTask<Payload, String, Payload> {

    public final static String TAG = ScanMediaTask.class.getSimpleName();
    private DeleteCourseListener mStateListener;
    private Context ctx;

    public DeleteCourseTask(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    protected Payload doInBackground(Payload... params) {

        Payload payload = params[0];
        Course course = (Course) payload.getData().get(0);

        DbHelper db = new DbHelper(ctx);
        db.deleteCourse(course.getCourseId());
        DatabaseManager.getInstance().closeDatabase();

        // remove files
        String courseLocation = course.getLocation();
        File f = new File(courseLocation);

        boolean success = FileUtils.deleteDir(f);
        payload.setResult(success);

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
