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

import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.exception.CourseNotFoundException;
import org.digitalcampus.oppia.utils.storage.Storage;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Locale;

public class Course implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4412987572522420704L;
	public static final String SEQUENCING_MODE_NONE = "none";
    public static final String SEQUENCING_MODE_SECTION = "section";
    public static final String SEQUENCING_MODE_COURSE = "course";

	public static final String TAG = Course.class.getSimpleName();
	private int courseId;
	private String shortname;
	private Double versionId;
    private MultiLangInfo multiLangInfo = new MultiLangInfo();
	private boolean installed;
	private boolean toUpdate;
	private boolean toUpdateSchedule;
	private String downloadUrl;
	private String imageFile;
	private ArrayList<Media> media = new ArrayList<Media>();
	private ArrayList<CourseMetaPage> metaPages = new ArrayList<CourseMetaPage>();
	private Double scheduleVersionID;
	private String scheduleURI;
	private boolean isDraft = false;
	private int priority = 0;
	private int noActivities = 0;
	private int noActivitiesCompleted = 0;
	private int noActivitiesStarted = 0;
    private String sequencingMode = SEQUENCING_MODE_NONE;

	private String root;
	
	public Course(String root) {
		this.root = root;
	}	
	
	public boolean validate() throws CourseNotFoundException{
		File courseXML = new File(this.getCourseXMLLocation());
		if(!courseXML.exists()){
			throw new CourseNotFoundException();
		} else {
			return true;
		}
	}
	
	public Double getScheduleVersionID() {
		return scheduleVersionID;
	}

	public void setScheduleVersionID(Double scheduleVersionID) {
		this.scheduleVersionID = scheduleVersionID;
	}

	public ArrayList<Media> getMedia() {
		return media;
	}

	public void setMedia(ArrayList<Media> media) {
		this.media = media;
	}

	public String getImageFile() {
		return imageFile;
	}

	public String getImageFileFromRoot() {
		return this.root + File.separator + Storage.APP_COURSES_DIR_NAME + File.separator + this.getShortname() + File.separator + imageFile;
	}
	
	public void setImageFile(String imageFile) {
		this.imageFile = imageFile;
	}
	
	public String getDownloadUrl() {
		return downloadUrl;
	}

	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}
	
	public String getTrackerLogUrl(){
		return String.format(MobileLearning.COURSE_ACTIVITY_PATH, this.getShortname());
	}

	public Double getVersionId() {
		return versionId;
	}

	public void setVersionId(Double versionId) {
		this.versionId = versionId;
	}

	public boolean isInstalled() {
		return installed;
	}

	public void setInstalled(boolean installed) {
		this.installed = installed;
	}

	public boolean isToUpdate() {
		return toUpdate;
	}

	public void setToUpdate(boolean toUpdate) {
		this.toUpdate = toUpdate;
	}

	public float getProgressPercent() {
		// prevent divide by zero errors
		if (this.noActivities != 0){
			return this.noActivitiesCompleted*100/this.noActivities;
		} else {
			return 0;
		}
	}

	public String getShortname() {
		return shortname.toLowerCase(Locale.US);
	}

	public void setShortname(String shortname) {
		this.shortname = shortname.toLowerCase(Locale.US);
	}

	public int getCourseId() {
		return courseId;
	}

	public void setCourseId(int courseId) {
		this.courseId = courseId;
	}

	public String getLocation() {
		return this.root + File.separator + Storage.APP_COURSES_DIR_NAME + File.separator + this.getShortname() + File.separator;
		
	}

	public String getCourseXMLLocation(){
		//String root = prefs.getString(PrefsActivity.PREF_STORAGE_LOCATION, "");
		return this.root + File.separator + Storage.APP_COURSES_DIR_NAME + File.separator + this.getShortname() + File.separator + MobileLearning.COURSE_XML;
	}
	
	public boolean hasMedia(){
		if(media.size() == 0){
			return false;
		} else {
			return true;
		}
	}
	
	public void setMetaPages(ArrayList<CourseMetaPage> ammp){
		this.metaPages = ammp;
	}
	
	public ArrayList<CourseMetaPage> getMetaPages(){
		return this.metaPages;
	}
	
	public CourseMetaPage getMetaPage(int id){
		for(CourseMetaPage mmp: this.metaPages){
			if(id == mmp.getId()){
				return mmp;
			}
		}
		return null;
	}

	public boolean isToUpdateSchedule() {
		return toUpdateSchedule;
	}

	public void setToUpdateSchedule(boolean toUpdateSchedule) {
		this.toUpdateSchedule = toUpdateSchedule;
	}

	public String getScheduleURI() {
		return scheduleURI;
	}

	public void setScheduleURI(String scheduleURI) {
		this.scheduleURI = scheduleURI;
	}

	public boolean isDraft() {
		return isDraft;
	}

	public void setDraft(boolean isDraft) {
		this.isDraft = isDraft;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public int getNoActivities() {
		return noActivities;
	}

	public void setNoActivities(int noActivities) {
		this.noActivities = noActivities;
	}

	public int getNoActivitiesCompleted() {
		return noActivitiesCompleted;
	}

	public void setNoActivitiesCompleted(int noActivitiesCompleted) {
		this.noActivitiesCompleted = noActivitiesCompleted;
	}

	public int getNoActivitiesStarted() {
		return noActivitiesStarted;
	}

	public void setNoActivitiesStarted(int noActivitiesStarted) {
		this.noActivitiesStarted = noActivitiesStarted;
	}
	
	public int getNoActivitiesNotStarted(){
		return this.getNoActivities() - this.getNoActivitiesCompleted() - this.getNoActivitiesStarted();
	}

    public String getSequencingMode() {
        return sequencingMode;
    }

    public void setSequencingMode(String sequencingMode) {
        this.sequencingMode = sequencingMode;
    }

    public MultiLangInfo getMultiLangInfo() { return multiLangInfo; }
    public void setMultiLangInfo(MultiLangInfo multiLangInfo) {
        this.multiLangInfo = multiLangInfo;
    }
}
