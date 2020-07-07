package org.digitalcampus.oppia.task;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import androidx.preference.PreferenceManager;

import com.splunk.mint.Mint;

import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.exception.InvalidXMLException;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.CourseTransferableFile;
import org.digitalcampus.oppia.model.Media;
import org.digitalcampus.oppia.service.courseinstall.CourseInstall;
import org.digitalcampus.oppia.utils.storage.Storage;
import org.digitalcampus.oppia.utils.xmlreaders.CourseXMLReader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FetchCourseTransferableFilesTask extends AsyncTask<Payload, Boolean, Void> {

    public interface FetchBackupsListener{
        void coursesPendingToInstall(boolean pending);
        void onFetchComplete(List<CourseTransferableFile> backups, List<CourseTransferableFile> activityLogs);
    }

    private FetchBackupsListener listener;
    private Context ctx;
    private List<CourseTransferableFile> transferableFiles = new ArrayList<>();
    private List<CourseTransferableFile> activityLogs = new ArrayList<>();

    public FetchCourseTransferableFilesTask(Context ctx) {
        this.ctx = ctx;
    }


    @Override
    protected Void doInBackground(Payload... payloads) {

        File dir = new File(Storage.getDownloadPath(ctx));
        String[] children = dir.list();
        publishProgress(children != null && children.length > 0);

        fetchCourseBackups();
        fetchMediaFiles();
        fetchActivityLogs();

        return null;
    }

    private void fetchCourseBackups(){
        List<Course> courses = DbHelper.getInstance(ctx).getAllCourses();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String lang = prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage());

        for (Course course : courses){
            File backup = CourseInstall.savedBackupCourse(ctx, course.getShortname());
            if (backup != null){
                CourseTransferableFile courseBackup = new CourseTransferableFile();
                courseBackup.setType(CourseTransferableFile.TYPE_COURSE_BACKUP);
                courseBackup.setTitle(course.getTitle(lang));
                courseBackup.setFilename(backup.getName());
                courseBackup.setShortname(course.getShortname());
                courseBackup.setVersionId(course.getVersionId());
                courseBackup.setFile(backup);

                long filesize = backup.length();

                if (filesize > 0){
                    courseBackup.setFileSize(filesize);
                    transferableFiles.add(courseBackup);
                }

                List<String> courseRelatedMedia = new ArrayList<>();
                File courseXML = new File(course.getCourseXMLLocation());
                if (!courseXML.exists()){ continue; }

                CourseXMLReader cxr;
                try {
                    cxr = new CourseXMLReader(course.getCourseXMLLocation(), course.getCourseId(), ctx);
                    cxr.parse(CourseXMLReader.ParseMode.ONLY_MEDIA);
                    List<Media> media = cxr.getMediaResponses().getCourseMedia();
                    for(Media m: media){
                        courseRelatedMedia.add(m.getFilename());
                    }
                } catch (InvalidXMLException ixmle) {
                    Mint.logException(ixmle);
                }

                courseBackup.setRelatedMedia(courseRelatedMedia);
            }
        }
    }

    private void fetchMediaFiles(){
        File mediaPath = new File(Storage.getMediaPath(ctx));
        String[] mediaFiles = mediaPath.list();
        if (mediaFiles != null && mediaFiles.length > 0){
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
    }

    private void fetchActivityLogs(){
        File activityLogsDir = new File(Storage.getActivityPath(ctx));
        if (!activityLogsDir.exists()){
            return;
        }
        String[] logs = activityLogsDir.list();
        if (logs != null && logs.length > 0){
            for (String filename : logs){
                File file = new File(activityLogsDir, filename);
                CourseTransferableFile log = new CourseTransferableFile();
                log.setFilename(file.getName());
                log.setFile(file);
                log.setTitle(filename.substring(0, filename.indexOf('_')));
                log.setType(CourseTransferableFile.TYPE_ACTIVITY_LOG);
                log.setTitleFromFilename();
                log.setFileSize(file.length());
                log.setShortname("");
                activityLogs.add(log);
            }
        }

    }

    @Override
    protected void onProgressUpdate(Boolean... coursesToInstall){
        synchronized (this) {
            if (listener != null) {
                // update progress
                listener.coursesPendingToInstall(coursesToInstall[0]);
            }
        }
    }

    @Override
    protected void onPostExecute(Void params) {
        synchronized (this) {
            if (listener != null) {
                listener.onFetchComplete(transferableFiles, activityLogs);
            }
        }
    }


    public void setListener(FetchBackupsListener listener) {
        this.listener = listener;
    }
}
