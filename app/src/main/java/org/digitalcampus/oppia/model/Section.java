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

import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Section implements Serializable  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6360494638548755423L;
	
	public static final String TAG = Section.class.getSimpleName();
	private int order;
	private ArrayList<Lang> titles = new ArrayList<Lang>();
	private ArrayList<Activity> activities;
	private String imageFile;
	
	public String getImageFile() {
		return imageFile;
	}

	public void setImageFile(String imageFile) {
		this.imageFile = imageFile;
	}

	public Section(){
		activities = new ArrayList<Activity>();
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
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
		return null;
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
	
	public void setTitles(ArrayList<Lang> titles) {
		this.titles = titles;
	}

	public ArrayList<Activity> getActivities() {
		return activities;
	}

	public void addActivity(Activity activity){
		this.activities.add(activity);
	}
	public void setActivities(ArrayList<Activity> activities) {
		this.activities = activities;
	}
}
