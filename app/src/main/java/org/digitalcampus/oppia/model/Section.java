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
import java.util.List;


public class Section extends MultiLangInfoModel implements Serializable  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6360494638548755423L;
	
	public static final String TAG = Section.class.getSimpleName();
	private int order;
	private ArrayList<Activity> activities;
	private String imageFile;
	
	public String getImageFile() {
		return imageFile;
	}

	public void setImageFile(String imageFile) {
		this.imageFile = imageFile;
	}

	public Section(){
		activities = new ArrayList<>();
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public List<Activity> getActivities() {
		return activities;
	}

	public void addActivity(Activity activity){
		this.activities.add(activity);
	}
	public void setActivities(List<Activity> activities) {
		this.activities = (ArrayList<Activity>) activities;
	}

	public Activity getActivity(String digest){
		for (Activity activity : this.activities){
			if (digest.equals(activity.getDigest()))
				return activity;
		}
		return null;
	}

	public int getCompletedActivities(){
		int completed = 0;
		for (Activity activity : this.activities){
			if (activity.getCompleted())
				completed++;
		}
		return completed;
	}

}
