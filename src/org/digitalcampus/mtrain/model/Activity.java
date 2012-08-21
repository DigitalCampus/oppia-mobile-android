package org.digitalcampus.mtrain.model;

import java.io.Serializable;
import java.util.ArrayList;

public class Activity implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1548943805902073988L;

	public static final String TAG = "Activity";
	
	private long modId;
	private int sectionId;
	private int actId;
	private String actType;
	private ArrayList<Lang> titles = new ArrayList<Lang>();
	private ArrayList<Lang> locations = new ArrayList<Lang>();
	private ArrayList<Lang> contents = new ArrayList<Lang>();
	private String digest;
	
	public String getDigest() {
		return digest;
	}

	public void setDigest(String digest) {
		this.digest = digest;
	}

	public Activity(){
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
}
