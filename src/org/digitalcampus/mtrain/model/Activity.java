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
	private HashMap<String, String> data;
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

	public HashMap<String, String> getActivityData() {
		return data;
	}

	public void setActivityData(HashMap<String, String> data) {
		this.data = data;
	}

	
}
