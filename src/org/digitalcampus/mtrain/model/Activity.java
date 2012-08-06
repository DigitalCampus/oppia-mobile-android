package org.digitalcampus.mtrain.model;

import org.w3c.dom.Node;

public class Activity {
	private long modId;
	private int sectionId;
	private int actId;
	private String actType;
	private Node activity;
	
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

	public Node getActivity() {
		return activity;
	}

	public void setActivity(Node activity) {
		this.activity = activity;
	}

	
}
