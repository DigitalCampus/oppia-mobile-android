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

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.exception.GamificationEventNotFound;
import org.digitalcampus.oppia.utils.ImageUtils;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;

public class Activity implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1548943805902073988L;

	public static final String TAG = Activity.class.getSimpleName();
	
	private long courseId;
	private int sectionId;
	private int actId;
	private int dbId;
	private String actType;
    private MultiLangInfo multiLangInfo = new MultiLangInfo();
	private ArrayList<Lang> locations = new ArrayList<>();
	private ArrayList<Lang> contents = new ArrayList<>();
	private String digest;
	private String imageFile;
	private ArrayList<Media> media = new ArrayList<>();
	private boolean completed = false;
	private boolean attempted = false;
	private boolean customImage = false;
	private DateTime startDate;
	private DateTime endDate;
	private String mimeType;
	private ArrayList<GamificationEvent> gamificationEvents = new ArrayList<>();

	public Activity(){
	}
	
	public boolean hasCustomImage(){
		return this.customImage;
	}

    public String getImageFilePath(String prefix){
        if(!prefix.endsWith(File.separator)){
            prefix += File.separator;
        }
        return prefix + this.imageFile;
    }

    public int getDefaultResourceImage(){
        if(actType.equals("quiz")){
            return R.drawable.default_icon_quiz;
        } else if (actType.equals("page") && this.hasMedia()){
            return R.drawable.default_icon_video;
        }
        return R.drawable.default_icon_activity;
    }

	public BitmapDrawable getImageFile(String prefix, Resources res) {
		return ImageUtils.LoadBMPsdcard(getImageFilePath(prefix), res, getDefaultResourceImage());
	}

	public void setImageFile(String imageFile) {
		this.imageFile = imageFile;
		this.customImage = true;
	}
	
	public ArrayList<Media> getMedia() {
		return media;
	}

	public void setMedia(ArrayList<Media> media) {
		this.media = media;
	}

	public String getDigest() {
		return digest;
	}

	public void setDigest(String digest) {
		this.digest = digest;
	}
	
	public long getCourseId() {
		return courseId;
	}

	public void setCourseId(long courseId) {
		this.courseId = courseId;
	}

	public int getSectionId() {
		return sectionId;
	}

	public void setSectionId(int sectionId) {
		this.sectionId = sectionId;
	}

	public int getActId() {
		return actId;
	}

	public void setActId(int actId) {
		this.actId = actId;
	}

	public String getActType() {
		return actType;
	}

	public void setActType(String actType) {
		this.actType = actType;
	}

	public String getLocation(String lang) {
		for(Lang l: locations){
			if(l.getLang().equals(lang)){
				return l.getContent();
			}
		}
		if(locations.size() > 0){
			return locations.get(0).getContent();
		}
		return null;
	}
	
	public void setLocations(ArrayList<Lang> locations) {
		this.locations = locations;
	}
	
	public String getContents(String lang) {
		for(Lang l: contents){
			if(l.getLang().equals(lang)){
				return l.getContent();
			}
		}
		if(contents.size() > 0){
			return contents.get(0).getContent();
		}
		return "No content";
	}
	
	public void setContents(ArrayList<Lang> contents) {
		this.contents = contents;
	}
	
	public void setContentFromJSONString(String json){
		try {
			JSONArray contentsArray = new JSONArray(json);
			for(int i=0; i<contentsArray.length(); i++){
				JSONObject contentObj = contentsArray.getJSONObject(i);
				@SuppressWarnings("unchecked")
				Iterator<String> iter = (Iterator<String>) contentObj.keys();
				while(iter.hasNext()){
					String key = iter.next().toString();
					String content = contentObj.getString(key);
					Lang l = new Lang(key,content);
					this.contents.add(l);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	public boolean hasMedia(){
		if(media.size() == 0){
			return false;
		} else {
			return true;
		}
	}
	
	public void setCompleted(boolean completed){
		this.completed = completed;
	}
	
	public boolean getCompleted(){
		return this.completed;
	}
	
	public DateTime getStartDate() {
		return startDate;
	}

	public void setStartDate(DateTime startDate) {
		this.startDate = startDate;
	}

	public DateTime getEndDate() {
		return endDate;
	}

	public void setEndDate(DateTime endDate) {
		this.endDate = endDate;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public boolean isAttempted() {
		return attempted;
	}

	public void setAttempted(boolean attempted) {
		this.attempted = attempted;
	}

	public int getDbId() {
		return dbId;
	}

	public void setDbId(int dbId) {
		this.dbId = dbId;
	}

    public MultiLangInfo getMultiLangInfo() { return multiLangInfo; }

    public void setMultiLangInfo(MultiLangInfo multiLangInfo) {
        this.multiLangInfo = multiLangInfo;
    }

    public void addGamificationEvent(GamificationEvent event){
        gamificationEvents.add(event);
    }

	public void setGamificationEvents(ArrayList<GamificationEvent> events){
		gamificationEvents = events;
	}

    public GamificationEvent findGamificationEvent(String event) throws GamificationEventNotFound {
        for(GamificationEvent ge: gamificationEvents){
            if(ge.getEvent().equals(event)){
                return ge;
            }
        }
        throw new GamificationEventNotFound(event);
    }
}
