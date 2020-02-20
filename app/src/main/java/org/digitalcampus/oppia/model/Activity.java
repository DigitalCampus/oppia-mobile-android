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
import java.util.List;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.exception.GamificationEventNotFound;


public class Activity extends MultiLangInfoModel implements Serializable{
	
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
	private List<Lang> locations = new ArrayList<>();
	private List<Lang> contents = new ArrayList<>();
	private String digest;
	private String imageFile;
	private List<Media> media = new ArrayList<>();
	private boolean completed = false;
	private boolean attempted = false;
	private boolean customImage = false;
	private String mimeType;
	private List<GamificationEvent> gamificationEvents = new ArrayList<>();

	public Activity(){
		// do nothing
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

	public void setImageFile(String imageFile) {
		this.imageFile = imageFile;
		this.customImage = true;
	}
	
	public List<Media> getMedia() {
		return media;
	}

	public Media getMedia(String filename){
		for (Media m : getMedia()) {
			if (m.getFilename().equals(filename)) {
				return m;
			}
		}
		return null;
	}

	public void setMedia(List<Media> media) {
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
			if(l.getLanguage().equals(lang)){
				return l.getContent();
			}
		}
		if(locations.isEmpty()){
			return null;
		} else {
			return locations.get(0).getContent();
		}

	}
	
	public void setLocations(List<Lang> locations) {
		this.locations = locations;
	}
	
	public String getContents(String lang) {
		for(Lang l: contents){
			if(l.getLanguage().equals(lang)){
				return l.getContent();
			}
		}
		if(contents.isEmpty()) {
			return "No content";
		} else {
			return contents.get(0).getContent();
		}

	}
	
	public void setContents(List<Lang> contents) {
		this.contents = contents;
	}

	public boolean hasMedia(){
		return !media.isEmpty();
	}
	
	public void setCompleted(boolean completed){
		this.completed = completed;
	}
	
	public boolean getCompleted(){
		return this.completed;
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

	public void setGamificationEvents(List<GamificationEvent> events){
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
