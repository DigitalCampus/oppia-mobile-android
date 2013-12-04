/* 
 * This file is part of OppiaMobile - http://oppia-mobile.org/
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import org.digitalcampus.mobile.learning.R;
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
	
	private long modId;
	private int sectionId;
	private int actId;
	private String actType;
	private ArrayList<Lang> titles = new ArrayList<Lang>();
	private ArrayList<Lang> locations = new ArrayList<Lang>();
	private ArrayList<Lang> contents = new ArrayList<Lang>();
	private String digest;
	private String imageFile;
	private ArrayList<Media> media = new ArrayList<Media>();
	private boolean completed = false;
	private boolean attempted = false;
	private boolean customImage = false;
	private DateTime startDate;
	private DateTime endDate;
	private String mimeType;
	private ArrayList<Lang> descriptions = new ArrayList<Lang>();

	public Activity(){
	}
	
	public boolean hasCustomImage(){
		return this.customImage;
	}
	
	public BitmapDrawable getImageFile(String prefix, Resources res) {
		int defaultImage = R.drawable.default_icon_activity;
		if(actType.equals("quiz")){
			defaultImage = R.drawable.default_icon_quiz;
		} else if (actType.equals("page") && this.hasMedia()){
			defaultImage = R.drawable.default_icon_video;
		}
		if(!prefix.endsWith("/")){
			prefix += "/";
		}
		return ImageUtils.LoadBMPsdcard(prefix + this.imageFile, res, defaultImage);
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
	
	public long getModId() {
		return modId;
	}

	public void setModId(long modId) {
		this.modId = modId;
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

	public String getTitleJSONString(){
		JSONArray array = new JSONArray();
		for(Lang l: titles){
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
	
	public void setTitlesFromJSONString(String json){
		try {
			JSONArray titlesArray = new JSONArray(json);
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
	
	public void setTitles(ArrayList<Lang> titles) {
		this.titles = titles;
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
		return "No location set";
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
		return "No content set";
	}
	
	public void setContents(ArrayList<Lang> contents) {
		this.contents = contents;
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
	
	public String getDescription(String lang) {
		for(Lang l: descriptions){
			if(l.getLang().equals(lang)){
				return l.getContent();
			}
		}
		if(descriptions.size() > 0){
			return descriptions.get(0).getContent();
		}
		return "No description set";
	}
	
	public void setDescriptions(ArrayList<Lang> descriptions) {
		this.descriptions = descriptions;
	}

	public boolean isAttempted() {
		return attempted;
	}

	public void setAttempted(boolean attempted) {
		this.attempted = attempted;
	}
}
