package org.digitalcampus.oppia.task;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.CourseBackup;
import org.digitalcampus.oppia.service.courseinstall.CourseInstall;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String lang = prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage());

        for (Course course : courses){
            File backup = CourseInstall.savedBackupCourse(ctx, course.getShortname());
            if (backup != null){
                CourseBackup courseBackup = new CourseBackup();
                courseBackup.setTitle(course.getMultiLangInfo().getTitle(lang));
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
