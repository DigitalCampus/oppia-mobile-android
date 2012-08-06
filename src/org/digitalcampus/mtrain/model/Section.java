package org.digitalcampus.mtrain.model;

import java.util.ArrayList;

public class Section {

	private int sectionId;
	private String title;
	private ArrayList<Activity> activities;
	
	public Section(){
		
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
