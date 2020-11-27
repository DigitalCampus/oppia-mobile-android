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

package org.digitalcampus.oppia.model;

import android.content.SharedPreferences;
import android.util.Log;

import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.App;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Media implements Serializable{

	private static final long serialVersionUID = -7381597814535579028L;
	
	public static final String TAG = Media.class.getSimpleName();
	private String filename;
	private String downloadUrl;
	private String digest;
	private int length;
	private double fileSize;
	private ArrayList<Course> courses;

	private int downloaded;

	public Media(){
		courses = new ArrayList<>();
	}

	public Media(String filename, int lenght) {
		this.filename = filename;
		this.length = lenght;
	}

	public String getFilename() {
		return filename;
	}
	
	public int getLength() {
		return length;
	}
	
	public void setLength(int length) {
		this.length = length;
	}
	
	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	public String getDownloadUrl() {
		return downloadUrl;
	}
	
	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}
	
	public String getDigest() {
		return digest;
	}
	
	public void setDigest(String digest) {
		this.digest = digest;
	}

	public double getFileSize() {
		return fileSize;
	}

	public void setFileSize(double fileSize) {
		this.fileSize = fileSize;
	}

	public int getDownloaded() {
		return downloaded;
	}

	public List<Course> getCourses(){ return courses; }

    //ONLY FOR UI PURPOSES
    private boolean downloading;
    private int progress;

    public boolean isDownloading() {
        return downloading;
    }
    public void setDownloading(boolean downloading) {
        this.downloading = downloading;
    }

    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }

    public static boolean shouldScanMedia(SharedPreferences prefs){
		long now = System.currentTimeMillis()/1000;
		long lastScan = prefs.getLong(PrefsActivity.PREF_LAST_MEDIA_SCAN, 0);
		return (lastScan + App.MEDIA_SCAN_TIME_LIMIT <= now);
	}

	public static void resetMediaScan(SharedPreferences prefs){
    	Log.d(TAG, "Resetting last media scan");
		prefs.edit().putLong(PrefsActivity.PREF_LAST_MEDIA_SCAN, 0).apply();
	}

	public static void updateMediaScan(SharedPreferences prefs){
		Log.d(TAG, "Updating last media scan to now");
		long now = System.currentTimeMillis()/1000;
		prefs.edit().putLong(PrefsActivity.PREF_LAST_MEDIA_SCAN, now).apply();
	}
}

