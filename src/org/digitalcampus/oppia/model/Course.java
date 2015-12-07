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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

public class Course implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4412987572522420704L;
	
	public static final String TAG = Course.class.getSimpleName();
	private int courseId;
	private ArrayList<Lang> titles = new ArrayList<Lang>();
	private ArrayList<Lang> descriptions = new ArrayList<Lang>();
	private String shortname;
	private Double versionId;
	private boolean installed;
	private boolean toUpdate;
	private boolean toUpdateSchedule;
	private String downloadUrl;
	private ArrayList<Lang> langs = new ArrayList<Lang>();
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
	
	public ArrayList<Lang> getLangs() {
		return langs;
	}

	public String getLangsJSONString(){
		JSONArray array = new JSONArray();
		for(Lang l: langs){
			JSONObject obj = new JSONObject();
			try {
				obj.put(l.getLang(), true);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			array.put(obj);
		}
		return array.toString();
	}
	
	public void setLangs(ArrayList<Lang> langs) {
		this.langs = langs;
	}
	
	public void setLangsFromJSONString(String jsonStr) {
		try {
			JSONArray langsArray = new JSONArray(jsonStr);
			for(int i=0; i<langsArray.length(); i++){
				JSONObject titleObj = langsArray.getJSONObject(i);
				@SuppressWarnings("unchecked")
				Iterator<String> iter = (Iterator<String>) titleObj.keys();
				while(iter.hasNext()){
					Lang l = new Lang(iter.next().toString(),"");
					this.langs.add(l);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (NullPointerException npe){
			npe.printStackTrace();
		}
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
	

	public String getTitle(String lang) {
		for(Lang l: titles){
			if(l.getLang().equals(lang)){
				return l.getContent();
			}
		}
		if(titles.size() > 0){
			return titles.get(0).getContent();
		}
		return "No title set";
	}
	
	public void setTitles(ArrayList<Lang> titles) {
		this.titles = titles;
	}
	
	public void setTitlesFromJSONString(String jsonStr) {
		try {
			JSONArray titlesArray = new JSONArray(jsonStr);
			for(int i=0; i<titlesArray.length(); i++){
				JSONObject titleObj = titlesArray.getJSONObject(i);
				@SuppressWarnings("unchecked")
				Iterator<String> iter = (Iterator<String>) titleObj.keys();
				while(iter.hasNext()){
					String key = iter.next().toString();
					String title = titleObj.getString(key);
					Lang l = new Lang(key,title);
					this.titles.add(l);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public String getTitleJSONString(){
		JSONArray array = new JSONArray();
		for(Lang l: this.titles){
			JSONObject obj = new JSONObject();
			try {
				obj.put(l.getLang(), l.getContent());
			} catch (JSONException e) {
				e.printStackTrace();
			}
			array.put(obj);
		}
		return array.toString();
	}
	
	public String getDescription(String lang) {
		for(Lang l: descriptions){
			if(l.getLang().equals(lang)){
				return l.getContent();
			}
		}
		if(descriptions.size() > 0){
			return descriptions.get(0).getContent();
		}
		return null;
	}
	
	public void setDescriptions(ArrayList<Lang> descriptions) {
		this.descriptions = descriptions;
	}
	
	public void setDescriptionsFromJSONString(String jsonStr) {
		try {
			JSONArray descriptionsArray = new JSONArray(jsonStr);
			for(int i=0; i<descriptionsArray.length(); i++){
				JSONObject descriptionObj = descriptionsArray.getJSONObject(i);
				@SuppressWarnings("unchecked")
				Iterator<String> iter = (Iterator<String>) descriptionObj.keys();
				while(iter.hasNext()){
					String key = iter.next().toString();
					String description = descriptionObj.getString(key);
					Lang l = new Lang(key,description);
					this.descriptions.add(l);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public String getDescriptionJSONString(){
		JSONArray array = new JSONArray();
		for(Lang l: this.descriptions){
			JSONObject obj = new JSONObject();
			try {
				obj.put(l.getLang(), l.getContent());
			} catch (JSONException e) {
				e.printStackTrace();
			}
			array.put(obj);
		}
		return array.toString();
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
	
	
}
