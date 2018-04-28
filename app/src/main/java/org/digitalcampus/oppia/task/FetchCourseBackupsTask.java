package org.digitalcampus.oppia.task;

import android.content.Context;
import android.os.AsyncTask;

import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.CourseBackup;
import org.digitalcampus.oppia.service.courseinstall.CourseInstall;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FetchCourseBackupsTask extends AsyncTask<Payload, String, List<CourseBackup>> {

    public interface FetchBackupsListener{
        void onFetchComplete(List<CourseBackup> backups);
    }

    FetchBackupsListener listener;
    private Context ctx;

    public FetchCourseBackupsTask(Context ctx) {
        this.ctx = ctx;
    }


    @Override
    protected List<CourseBackup> doInBackground(Payload... payloads) {

        DbHelper db = DbHelper.getInstance(ctx);
        ArrayList<CourseBackup> backups = new ArrayList<>();
        ArrayList<Course> courses = db.getAllCourses();

        for (Course course : courses){
            File backup = CourseInstall.savedBackupCourse(ctx, course.getShortname());
            if (backup != null){
                CourseBackup courseBackup = new CourseBackup();
                courseBackup.setFilename(backup.getName());
                courseBackup.setShortname(course.getShortname());
                courseBackup.setVersionId(course.getVersionId());

                long filesize = backup.length();
                courseBackup.setFileSize(filesize);

                if (filesize > 0){
                    backups.add(courseBackup);
                }
            }
        }
        return backups;
    }

    @Override
    protected void onPostExecute(List<CourseBackup> backups) {
        synchronized (this) {
            if (listener != null) {
                listener.onFetchComplete(backups);
            }
        }
    }


    public void setListener(FetchBackupsListener listener) {
        this.listener = listener;
    }
}
