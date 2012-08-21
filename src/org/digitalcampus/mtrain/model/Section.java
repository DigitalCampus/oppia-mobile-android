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
	private ArrayList<Lang> titles = new ArrayList<Lang>();
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

	public void addTitle(String lang, String title) {
		// TODO check that this lang isn't already in list
		this.titles.add(new Lang(lang,title));
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
