package org.digitalcampus.mtrain.model;

import java.io.Serializable;
import java.util.ArrayList;

public class Section implements Serializable  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6360494638548755423L;
	
	public static final String TAG = "Section";
	private int sectionId;
	private String title;
	private ArrayList<Activity> activities;
	private float progress = 0;
	
	public float getProgress() {
		return progress;
	}

	public void setProgress(float progress) {
		this.progress = progress;
	}

	public Section(){
		activities = new ArrayList<Activity>();
	}

	public int getSectionId() {
		return sectionId;
	}

	public void setSectionId(int sectionId) {
		this.sectionId = sectionId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
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
