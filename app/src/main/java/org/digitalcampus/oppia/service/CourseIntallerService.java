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

package org.digitalcampus.oppia.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.splunk.mint.Mint;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.exception.InvalidXMLException;
import org.digitalcampus.oppia.exception.UserNotFoundException;
import org.digitalcampus.oppia.model.ActivitySchedule;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.utils.HTTPClientUtils;
import org.digitalcampus.oppia.utils.SearchUtils;
import org.digitalcampus.oppia.utils.storage.FileUtils;
import org.digitalcampus.oppia.utils.storage.Storage;
import org.digitalcampus.oppia.utils.xmlreaders.CourseScheduleXMLReader;
import org.digitalcampus.oppia.utils.xmlreaders.CourseTrackerXMLReader;
import org.digitalcampus.oppia.utils.xmlreaders.CourseXMLReader;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.util.ArrayList;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class CourseIntallerService extends IntentService {

    public static final String TAG = CourseIntallerService.class.getSimpleName();
    public static final String BROADCAST_ACTION = "com.digitalcampus.oppia.COURSEINSTALLERSERVICE";

    public static final String SERVICE_ACTION = "action";
    public static final String SERVICE_URL = "fileurl"; //field for providing file URL
    public static final String SERVICE_SHORTNAME = "shortname"; //field for providing Course shortname
    public static final String SERVICE_VERSIONID = "versionid"; //field for providing file URL
    public static final String SERVICE_MESSAGE = "message";
    public static final String SERVICE_SCHEDULEURL = "scheduleurl";

    public static final String ACTION_CANCEL = "cancel";
    public static final String ACTION_DOWNLOAD = "download";
    public static final String ACTION_UPDATE = "update";
    public static final String ACTION_INSTALL = "install";
    public static final String ACTION_COMPLETE = "complete";
    public static final String ACTION_FAILED = "failed";

    private ArrayList<String> tasksCancelled;
    private ArrayList<String> tasksDownloading;
    private SharedPreferences prefs;

    private static CourseIntallerService currentInstance;
    private static void setInstance(CourseIntallerService instance){
        currentInstance = instance;
    }
    public static ArrayList<String> getTasksDownloading(){
        if (currentInstance != null){
            synchronized (currentInstance){
                return currentInstance.tasksDownloading;
            }
        }
        return null;
    }

    public CourseIntallerService() { super(TAG); }

    @Override
    public void onCreate(){
        super.onCreate();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        CourseIntallerService.setInstance(this);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent.hasExtra(SERVICE_ACTION) && intent.hasExtra(SERVICE_URL)) {
            // Set the canceling flag to that file
            if (intent.getStringExtra(SERVICE_ACTION).equals(ACTION_CANCEL)){
                Log.d(TAG, "CANCEL commmand received");
                addCancelledTask(intent.getStringExtra(SERVICE_URL));
            }
            else if (intent.getStringExtra(SERVICE_ACTION).equals(ACTION_DOWNLOAD) ||
                    intent.getStringExtra(SERVICE_ACTION).equals(ACTION_UPDATE)) {
                addDownloadingTask(intent.getStringExtra(SERVICE_URL));
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent.hasExtra (SERVICE_ACTION)) {
            boolean cancel = intent.getStringExtra(SERVICE_ACTION).equals(ACTION_CANCEL);
            //We have nothing more to do with a 'cancel' action than what is done in onStartCommand()
            if (cancel) { return; }
        }

        if (!intent.hasExtra(SERVICE_URL)){
            Log.d(TAG, "No Course passed to the service. Invalid task");
            return;
        }

        if (intent.getStringExtra(SERVICE_ACTION).equals(ACTION_DOWNLOAD)){
            String fileUrl = intent.getStringExtra(SERVICE_URL);
            String shortname = intent.getStringExtra(SERVICE_SHORTNAME);
            Double versionID = intent.getDoubleExtra(SERVICE_VERSIONID, 0);

            if (isCancelled(fileUrl)) {
                //If it was cancelled before starting, we do nothing
                Log.d(TAG, "Course " + fileUrl + " cancelled before started.");
                removeCancelled(fileUrl);
                removeDownloading(fileUrl);
                return;
            }
            boolean success = downloadCourseFile(fileUrl, shortname, versionID);
            if (success){ installDownloadedCourse(fileUrl, shortname, versionID); }
        }
        else if (intent.getStringExtra(SERVICE_ACTION).equals(ACTION_UPDATE)){
            String scheduleURL = intent.getStringExtra(SERVICE_SCHEDULEURL);
            String shortname = intent.getStringExtra(SERVICE_SHORTNAME);
            updateCourseSchedule(scheduleURL, shortname);
        }

    }

    private void installDownloadedCourse(String fileUrl, String shortname, Double versionID) {
        File tempdir = new File(Storage.getStorageLocationRoot(this) + "temp/");
        String filename = getLocalFilename(shortname, versionID);
        File zipFile = new File(Storage.getDownloadPath(this), filename);
        tempdir.mkdirs();

        long startTime = System.currentTimeMillis();
        sendBroadcast(fileUrl, ACTION_INSTALL, ""+1);
        boolean unzipResult = FileUtils.unzipFiles(Storage.getDownloadPath(this), filename, tempdir.getAbsolutePath());

        if (!unzipResult){
            //then was invalid zip file and should be removed
            FileUtils.cleanUp(tempdir, Storage.getDownloadPath(this) + filename);
            sendBroadcast(fileUrl, ACTION_FAILED, "" + this.getString(R.string.error_installing_course, shortname));
            removeDownloading(fileUrl);
            return;
        }
        String[] courseDirs = tempdir.list(); // use this to get the course name

        sendBroadcast(fileUrl, ACTION_INSTALL, "" + 10);

        String courseXMLPath;
        String courseScheduleXMLPath;
        String courseTrackerXMLPath;
        // check that it's unzipped etc correctly
        try {
            courseXMLPath = tempdir + File.separator + courseDirs[0] + File.separator + MobileLearning.COURSE_XML;
            courseScheduleXMLPath = tempdir + File.separator + courseDirs[0] + File.separator + MobileLearning.COURSE_SCHEDULE_XML;
            courseTrackerXMLPath = tempdir + File.separator + courseDirs[0] + File.separator + MobileLearning.COURSE_TRACKER_XML;
        } catch (ArrayIndexOutOfBoundsException aioobe){
            FileUtils.cleanUp(tempdir, Storage.getDownloadPath(this) + filename);
            logAndNotifyError(fileUrl, aioobe);
            return;
        }

        // check a module.xml file exists and is a readable XML file
        CourseXMLReader cxr;
        CourseScheduleXMLReader csxr;
        CourseTrackerXMLReader ctxr;
        try {
            cxr = new CourseXMLReader(courseXMLPath, 0, this);
            csxr = new CourseScheduleXMLReader(courseScheduleXMLPath);
            File trackerXML = new File(courseTrackerXMLPath);
			ctxr = new CourseTrackerXMLReader(trackerXML);
        } catch (InvalidXMLException e) {
            Mint.logException(e);
            logAndNotifyError(fileUrl, e);
            return;
        }

        Course c = new Course(prefs.getString(PrefsActivity.PREF_STORAGE_LOCATION, ""));
        c.setVersionId(cxr.getVersionId());
        c.setTitles(cxr.getTitles());
        c.setShortname(courseDirs[0]);
        c.setImageFile(cxr.getCourseImage());
        c.setLangs(cxr.getLangs());
        c.setDescriptions(cxr.getDescriptions());
        c.setPriority(cxr.getPriority());
        String sequencingMode = cxr.getCourseSequencingMode();
        if ((sequencingMode!=null) && (sequencingMode.equals(Course.SEQUENCING_MODE_COURSE) ||
                sequencingMode.equals(Course.SEQUENCING_MODE_SECTION) || sequencingMode.equals(Course.SEQUENCING_MODE_NONE))){
            c.setSequencingMode(sequencingMode);
        }

        String title = c.getTitle(prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage()));

        sendBroadcast(fileUrl, ACTION_INSTALL, ""+20);

        boolean success = false;

        DbHelper db = DbHelper.getInstance(this);
        long courseId = db.addOrUpdateCourse(c);
        if (courseId != -1) {
            File src = new File(tempdir + File.separator + courseDirs[0]);
            File dest = new File(Storage.getCoursesPath(this));

            db.insertActivities(cxr.getActivities(courseId));
            sendBroadcast(fileUrl, ACTION_INSTALL, "" + 50);
            
            long userId = db.getUserId(SessionManager.getUsername(this));
            db.resetCourse(courseId, userId);
            db.insertTrackers(ctxr.getTrackers(courseId, userId));
            db.insertQuizAttempts(ctxr.getQuizAttempts(courseId, userId));
            
            sendBroadcast(fileUrl, ACTION_INSTALL, "" + 70);

            // Delete old course
            File oldCourse = new File(Storage.getCoursesPath(this) + courseDirs[0]);
            FileUtils.deleteDir(oldCourse);

            // move from temp to courses dir
            success = src.renameTo(new File(dest, src.getName()));

            if (!success) {
                sendBroadcast(fileUrl, ACTION_FAILED, "" + this.getString(R.string.error_installing_course, title));
                removeDownloading(fileUrl);
                return;
            }
        }  else {
            sendBroadcast(fileUrl, ACTION_FAILED, "" + this.getString(R.string.error_latest_already_installed, title));
            removeDownloading(fileUrl);
        }
        // add schedule
        // put this here so even if the course content isn't updated the schedule will be
        db.insertSchedule(csxr.getSchedule());
        db.updateScheduleVersion(courseId, csxr.getScheduleVersion());

        sendBroadcast(fileUrl, ACTION_INSTALL, "" + 80);
        if (success){ SearchUtils.indexAddCourse(this, c); }

        // delete temp directory
        FileUtils.deleteDir(tempdir);
        sendBroadcast(fileUrl, ACTION_INSTALL, "" + 90);

        // delete zip file from download dir
        deleteFile(zipFile);

        long estimatedTime = System.currentTimeMillis() - startTime;
        Log.d(TAG, "MeasureTime - " + c.getShortname() + ": " + estimatedTime + "ms");

        Log.d(TAG, fileUrl + " succesfully downloaded");
        removeDownloading(fileUrl);
        sendBroadcast(fileUrl, ACTION_COMPLETE, null);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        CourseIntallerService.setInstance(null);
    }

    private void logAndNotifyError(String fileUrl, Exception e){
        e.printStackTrace();
        Log.d(TAG, "Error: " + e.getMessage());
        sendBroadcast(fileUrl, ACTION_FAILED, this.getString(R.string.error_media_download));
        removeDownloading(fileUrl);
    }

    /*
    * Sends a new Broadcast with the results of the action
    */
    private void sendBroadcast(String fileUrl, String result, String message){

        Intent localIntent = new Intent(BROADCAST_ACTION);
        localIntent.putExtra(SERVICE_ACTION, result);
        localIntent.putExtra(SERVICE_URL, fileUrl);
        if (message != null){
            localIntent.putExtra(SERVICE_MESSAGE, message);
        }
        // Broadcasts the Intent to receivers in this app.
        Log.d(TAG, fileUrl + "=" + result + ":" + message);
        sendOrderedBroadcast(localIntent, null);

    }

    private void addCancelledTask(String fileUrl){
        if (tasksCancelled == null){
            tasksCancelled = new ArrayList<>();
        }
        if (!tasksCancelled.contains(fileUrl)){
            tasksCancelled.add(fileUrl);
        }
    }

    private boolean isCancelled(String fileUrl){
        return (tasksCancelled != null) && (tasksCancelled.contains(fileUrl));
    }

    private boolean removeCancelled(String fileUrl) {
        return tasksCancelled != null && tasksCancelled.remove(fileUrl);
    }

    private void addDownloadingTask(String fileUrl){
        if (tasksDownloading == null){
            tasksDownloading = new ArrayList<>();
        }
        if (!tasksDownloading.contains(fileUrl)){
            synchronized (this){
                tasksDownloading.add(fileUrl);
            }
        }
    }

    private boolean removeDownloading(String fileUrl){
        if (tasksDownloading != null){
            synchronized (this){
                return tasksDownloading.remove(fileUrl);
            }
        }
        return false;
    }

    private boolean downloadCourseFile(String fileUrl, String shortname, Double versionID){

        long startTime = System.currentTimeMillis();
        File downloadedFile = null;
        try {
        	DbHelper db = DbHelper.getInstance(this);
        	User u = db.getUser(SessionManager.getUsername(this));

            OkHttpClient client = HTTPClientUtils.getClient(this);
            Request request = new Request.Builder()
                    .url(HTTPClientUtils.getUrlWithCredentials(fileUrl, u.getUsername(), u.getApiKey()))
                    .build();

            Response response = client.newCall(request).execute();

            long fileLength = response.body().contentLength();
            Log.d(TAG, "Content-lenght: " + fileLength);
            long availableStorage = Storage.getAvailableStorageSize(this);

            if (fileLength >= availableStorage){
                sendBroadcast(fileUrl, ACTION_FAILED, this.getString(R.string.error_insufficient_storage_available));
                removeDownloading(fileUrl);
                return false;
            }

            String localFileName = getLocalFilename(shortname, versionID);
            downloadedFile = new File(Storage.getDownloadPath(this),localFileName);
            FileOutputStream f = new FileOutputStream(downloadedFile);
            InputStream in = response.body().byteStream();

            byte[] buffer = new byte[8192];
            int len1;
            long total = 0;
            int previousProgress = 0, progress;
            while ((len1 = in.read(buffer)) > 0) {
                //If received a cancel action while downloading, stop it
                if (isCancelled(fileUrl)) {
                    Log.d(TAG, "Course " + localFileName + " cancelled while downloading. Deleting temp file...");
                    deleteFile(downloadedFile);
                    removeCancelled(fileUrl);
                    removeDownloading(fileUrl);
                    return false;
                }

                total += len1;
                progress = (int)((total*100)/fileLength);
                if ( (progress > 0) && (progress > previousProgress)){
                    sendBroadcast(fileUrl, ACTION_DOWNLOAD, ""+progress);
                    previousProgress = progress;
                }
                f.write(buffer, 0, len1);
            }
            in.close();
            f.flush();
            f.close();

        } catch (MalformedURLException e) {
            logAndNotifyError(fileUrl, e);
            return false;
        } catch (ProtocolException e) {
            this.deleteFile(downloadedFile);
            logAndNotifyError(fileUrl, e);
            return false;
        } catch (IOException e) {
            this.deleteFile(downloadedFile);
            logAndNotifyError(fileUrl, e);
            return false;
        } catch (UserNotFoundException unfe) {
            logAndNotifyError(fileUrl, unfe);
            return false;
		}

        Log.d(TAG, fileUrl + " succesfully downloaded");
        removeDownloading(fileUrl);
        sendBroadcast(fileUrl, ACTION_INSTALL, "0");

        long estimatedTime = System.currentTimeMillis() - startTime;
        Log.d(TAG, "MeasureTime - " + ": " + estimatedTime + "ms");
        return true;
    }

    private boolean updateCourseSchedule(String scheduleUrl, String shortname){
        sendBroadcast(scheduleUrl, ACTION_INSTALL, "" + 0);
        try {
        	
        	DbHelper db = DbHelper.getInstance(this);
        	User u = db.getUser(SessionManager.getUsername(this));

            OkHttpClient client = HTTPClientUtils.getClient(this);
            Request request = new Request.Builder()
                    .url(HTTPClientUtils.getFullURL(this, scheduleUrl))
                    .addHeader(HTTPClientUtils.HEADER_AUTH,
                            HTTPClientUtils.getAuthHeaderValue(u.getUsername(), u.getApiKey()))
                    .build();

            Response response = client.newCall(request).execute();
            if (response.isSuccessful()){
                JSONObject jsonObj = new JSONObject(response.body().string());
                long scheduleVersion = jsonObj.getLong("version");
                JSONArray schedule = jsonObj.getJSONArray("activityschedule");
                ArrayList<ActivitySchedule> activitySchedule = new ArrayList<>();
                int lastProgress = 0;
                for (int i = 0; i < (schedule.length()); i++) {

                    int progress = (i+1)*100/schedule.length();
                    if ((progress - (progress%10) > lastProgress)){
                        sendBroadcast(scheduleUrl, ACTION_INSTALL, ""+progress);
                        lastProgress = progress;
                    }

                    JSONObject acts = (JSONObject) schedule.get(i);
                    ActivitySchedule as = new ActivitySchedule();
                    as.setDigest(acts.getString("digest"));
                    DateTime sdt = MobileLearning.DATETIME_FORMAT.parseDateTime(acts.getString("start_date"));
                    DateTime edt = MobileLearning.DATETIME_FORMAT.parseDateTime(acts.getString("end_date"));
                    as.setStartTime(sdt);
                    as.setEndTime(edt);
                    activitySchedule.add(as);
                }
                int courseId = db.getCourseID(shortname);
                db.resetSchedule(courseId);
                db.insertSchedule(activitySchedule);
                db.updateScheduleVersion(courseId, scheduleVersion);
            }
            else{
                switch (response.code()) {
                    case 400:
                    case 401: // unauthorised
                        sendBroadcast(scheduleUrl, ACTION_FAILED, getString(R.string.error_login));
                        removeDownloading(scheduleUrl);
                        SessionManager.setUserApiKeyValid(this, u, false);
                        return false;

                    default:
                        sendBroadcast(scheduleUrl, ACTION_FAILED, getString(R.string.error_connection));
                        removeDownloading(scheduleUrl);
                        return false;
                }
            }

        } catch (JSONException e) {
            Mint.logException(e);
            e.printStackTrace();
            sendBroadcast(scheduleUrl, ACTION_FAILED, getString(R.string.error_processing_response));
            removeDownloading(scheduleUrl);
        } catch (UserNotFoundException | IOException e) {
            sendBroadcast(scheduleUrl, ACTION_FAILED, getString(R.string.error_connection));
            removeDownloading(scheduleUrl);
        }

        Log.d(TAG, scheduleUrl + " succesfully downloaded");
        removeDownloading(scheduleUrl);
        sendBroadcast(scheduleUrl, ACTION_COMPLETE, null);
        return true;
    }

    private String getLocalFilename(String shortname, Double versionID){
        return shortname+"-"+String.format("%.0f",versionID)+".zip";
    }

    private void deleteFile(File file){
        if ((file != null) && file.exists() && !file.isDirectory()){
            boolean deleted = file.delete();
            Log.d(TAG, file.getName() + (deleted? " deleted succesfully.": " deletion failed!"));
        }
    }
}
