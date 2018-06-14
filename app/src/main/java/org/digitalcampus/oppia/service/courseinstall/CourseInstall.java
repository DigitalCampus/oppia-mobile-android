package org.digitalcampus.oppia.service.courseinstall;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.application.Tracker;
import org.digitalcampus.oppia.exception.InvalidXMLException;
import org.digitalcampus.oppia.gamification.GamificationEngine;
import org.digitalcampus.oppia.model.CompleteCourse;
import org.digitalcampus.oppia.model.GamificationEvent;
import org.digitalcampus.oppia.model.Media;
import org.digitalcampus.oppia.utils.MetaDataUtils;
import org.digitalcampus.oppia.utils.SearchUtils;
import org.digitalcampus.oppia.utils.storage.FileUtils;
import org.digitalcampus.oppia.utils.storage.Storage;
import org.digitalcampus.oppia.utils.xmlreaders.CourseScheduleXMLReader;
import org.digitalcampus.oppia.utils.xmlreaders.CourseTrackerXMLReader;
import org.digitalcampus.oppia.utils.xmlreaders.CourseXMLReader;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class CourseInstall {

    public interface CourseInstallingListener{
        void onInstallProgress(int progress);
        void onError(String message);
        void onFail(String message);
        void onComplete();
    }

    public static final String TAG = CourseInstall.class.getSimpleName();

    public static void installDownloadedCourse(Context ctx, String filename, String shortname, CourseInstallingListener listener) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        File tempdir = new File(Storage.getStorageLocationRoot(ctx), "temp/");
        File zipFile = new File(Storage.getDownloadPath(ctx), filename);
        tempdir.mkdirs();

        long startTime = System.currentTimeMillis();

        listener.onInstallProgress(1);
        boolean unzipResult = FileUtils.unzipFiles(Storage.getDownloadPath(ctx), filename, tempdir.getAbsolutePath());

        if (!unzipResult){
            //then was invalid zip file and should be removed
            FileUtils.cleanUp(tempdir, Storage.getDownloadPath(ctx) + filename);
            listener.onError(ctx.getString(R.string.error_installing_course, shortname));
            return;
        }

        String courseDir;
        try {
            courseDir = tempdir.list()[0]; // use this to get the course name
        } catch (ArrayIndexOutOfBoundsException aioobe) {
            FileUtils.cleanUp(tempdir, Storage.getDownloadPath(ctx) + filename);
            aioobe.printStackTrace();
            Log.d(TAG, "Error: " + aioobe.getMessage());
            listener.onError(ctx.getString(R.string.error_installing_course));
            return;
        }
        listener.onInstallProgress(10);

        String courseXMLPath;
        String courseScheduleXMLPath;
        String courseTrackerXMLPath;
        // check that it's unzipped etc correctly
        try {
            courseXMLPath = tempdir + File.separator + courseDir + File.separator + MobileLearning.COURSE_XML;
            courseScheduleXMLPath = tempdir + File.separator + courseDir + File.separator + MobileLearning.COURSE_SCHEDULE_XML;
            courseTrackerXMLPath = tempdir + File.separator + courseDir + File.separator + MobileLearning.COURSE_TRACKER_XML;
        } catch (ArrayIndexOutOfBoundsException aioobe){
            FileUtils.cleanUp(tempdir, Storage.getDownloadPath(ctx) + filename);
            aioobe.printStackTrace();
            Log.d(TAG, "Error: " + aioobe.getMessage());
            listener.onError(ctx.getString(R.string.error_media_download));
            return;
        }

        // check a module.xml file exists and is a readable XML file
        CourseXMLReader cxr;
        CourseScheduleXMLReader csxr;
        CourseTrackerXMLReader ctxr;
        CompleteCourse c;
        try {
            cxr = new CourseXMLReader(courseXMLPath, 0, ctx);
            cxr.parse(CourseXMLReader.ParseMode.COMPLETE);
            c = cxr.getParsedCourse();

            csxr = new CourseScheduleXMLReader(courseScheduleXMLPath);
            ctxr = new CourseTrackerXMLReader(courseTrackerXMLPath);
        } catch (InvalidXMLException e) {
            listener.onError(e.getMessage());
            return;
        }

        c.setShortname(courseDir);
        String title = c.getMultiLangInfo().getTitle(prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage()));
        listener.onInstallProgress(20);

        boolean success = false;
        boolean latestVersion = false;

        DbHelper db = DbHelper.getInstance(ctx);
        long courseId = db.addOrUpdateCourse(c);
        if (courseId != -1) {
            File src = new File(tempdir, courseDir);
            File dest = new File(Storage.getCoursesPath(ctx));

            db.insertActivities(c.getActivities(courseId));
            listener.onInstallProgress(40);

            long userId = db.getUserId(SessionManager.getUsername(ctx));
            db.resetCourse(courseId, userId);
            db.insertTrackers(ctxr.getTrackers(courseId, userId));
            db.insertQuizAttempts(ctxr.getQuizAttempts(courseId, userId));

            listener.onInstallProgress(60);

            // Delete old course
            File oldCourse = new File(Storage.getCoursesPath(ctx) + courseDir);
            FileUtils.deleteDir(oldCourse);

            // move from temp to courses dir
            try {
                org.apache.commons.io.FileUtils.copyDirectory(src, new File(dest, src.getName()));
                success = true;
            } catch (IOException e) {
                e.printStackTrace();
                listener.onFail(ctx.getString(R.string.error_installing_course, title));
                return;
            }

        }  else {
            latestVersion = true;
            listener.onFail(ctx.getString(R.string.error_latest_already_installed, title) );
        }
        // add schedule
        // put this here so even if the course content isn't updated the schedule will be
        db.insertSchedule(csxr.getSchedule());
        db.updateScheduleVersion(courseId, csxr.getScheduleVersion());

        listener.onInstallProgress(70);
        if (success){ SearchUtils.indexAddCourse(ctx, c); }

        listener.onInstallProgress(80);

        if (success && !latestVersion){
            copyBackupCourse(ctx, tempdir, c);
        }

        listener.onInstallProgress(90);

        //We reset the media scan
        Media.resetMediaScan(prefs);

        // delete temp directory
        FileUtils.deleteDir(tempdir);
        // delete zip file from download dir
        FileUtils.deleteFile(zipFile);

        listener.onInstallProgress(95);

        // add to tracker
        GamificationEngine gamificationEngine = new GamificationEngine(ctx);
        GamificationEvent gamificationEvent = gamificationEngine.processEventCourseDownloaded(c);

        try {
            MetaDataUtils mdu = new MetaDataUtils(ctx);
            JSONObject obj = new JSONObject();
            Tracker t = new Tracker(ctx);
            t.saveTracker((int)courseId, "", mdu.getMetaData(obj), true, gamificationEvent);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        long estimatedTime = System.currentTimeMillis() - startTime;
        Log.d(TAG, "MeasureTime - " + c.getShortname() + ": " + estimatedTime + "ms");
        Log.d(TAG, shortname + " succesfully downloaded");
        listener.onComplete();
    }

    public static File savedBackupCourse(Context ctx, String shortname){

        //Look for previous backup files
        File backupDir = new File(Storage.getCourseBackupPath(ctx));
        backupDir.mkdirs();

        File previousBackup = null;
        String[] backups = backupDir.list();
        if (backups.length > 0){
            for (String backup : backups) {
                String backup_shortname = backup.substring(0, backup.lastIndexOf("_"));
                if (backup_shortname.equalsIgnoreCase(shortname)){
                    previousBackup = new File(Storage.getCourseBackupPath(ctx), backup);
                }
            }
        }
        return previousBackup;
    }


    private static void copyBackupCourse(Context ctx, File tempDir, CompleteCourse c) {
        //TODO: Add a BuildConfig to control if we want this functionality or not

        File courseFolder = new File(tempDir, tempDir.list()[0]);

        String shortname = c.getShortname();
        String version = "" + Math.round(c.getVersionId());
        String filename = shortname + "_" + version + ".zip";
        File destination = new File(Storage.getCourseBackupPath(ctx), filename);

        FileUtils.deleteFile(new File(courseFolder, MobileLearning.COURSE_TRACKER_XML));
        FileUtils.deleteFile(new File(courseFolder, MobileLearning.COURSE_SCHEDULE_XML));

        Log.d(TAG, courseFolder.getAbsolutePath());

        File previousBackup = savedBackupCourse(ctx, shortname);
        // Copy new course zip file
        Log.d(TAG, "Copying new backup file " + filename);
        FileUtils.zipFileAtPath(courseFolder, destination);
        //org.apache.commons.io.FileUtils.copyFile(zipFile, destination);

        if (previousBackup != null){
            Log.d(TAG, "Deleting previous backup file " + previousBackup.getPath());
            FileUtils.deleteFile(previousBackup);
        }

    }

}
