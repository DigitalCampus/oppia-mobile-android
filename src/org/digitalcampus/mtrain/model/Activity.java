package org.digitalcampus.mtrain.model;

import java.io.Serializable;
import java.util.HashMap;

import org.w3c.dom.Node;

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
	private HashMap<String, String> activity;
	private String md5 = "";
	
	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
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

	public HashMap<String, String> getActivity() {
		return activity;
	}

	public void setActivity(HashMap<String, String> activity) {
		this.activity = activity;
	}

	
}
