package org.digitalcampus.oppia.task;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.CourseTransferableFile;
import org.digitalcampus.oppia.service.courseinstall.CourseInstall;
import org.digitalcampus.oppia.utils.storage.Storage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FetchCourseTransferableFilesTask extends AsyncTask<Payload, String, List<CourseTransferableFile>> {

    public interface FetchBackupsListener{
        void onFetchComplete(List<CourseTransferableFile> backups);
    }

    FetchBackupsListener listener;
    private Context ctx;

    public FetchCourseTransferableFilesTask(Context ctx) {
        this.ctx = ctx;
    }


    @Override
    protected List<CourseTransferableFile> doInBackground(Payload... payloads) {

        DbHelper db = DbHelper.getInstance(ctx);
        ArrayList<CourseTransferableFile> transferableFiles = new ArrayList<>();
        List<Course> courses = db.getAllCourses();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String lang = prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage());

        for (Course course : courses){
            File backup = CourseInstall.savedBackupCourse(ctx, course.getShortname());
            if (backup != null){
                CourseTransferableFile courseBackup = new CourseTransferableFile();
                courseBackup.setType(CourseTransferableFile.TYPE_COURSE_BACKUP);
                courseBackup.setTitle(course.getMultiLangInfo().getTitle(lang));
                courseBackup.setFilename(backup.getName());
                courseBackup.setShortname(course.getShortname());
                courseBackup.setVersionId(course.getVersionId());
                courseBackup.setFile(backup);

                long filesize = backup.length();

                if (filesize > 0){
                    courseBackup.setFileSize(filesize);
                    transferableFiles.add(courseBackup);
                }
            }
        }

        File mediaPath = new File(Storage.getMediaPath(ctx));
        String[] mediaFiles = mediaPath.list();
        if (mediaFiles.length > 0){
            for (String mediaFile : mediaFiles) {
                File file = new File(mediaPath, mediaFile);
                CourseTransferableFile media = new CourseTransferableFile();
                media.setFilename(file.getName());
                media.setFile(file);
                media.setType(CourseTransferableFile.TYPE_COURSE_MEDIA);
                media.setShortname("");
                long filesize = file.length();
                if (filesize > 0){
                    media.setFileSize(filesize);
                    transferableFiles.add(media);
                }
            }
        }

        return transferableFiles;
    }

    @Override
    protected void onPostExecute(List<CourseTransferableFile> backups) {
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
