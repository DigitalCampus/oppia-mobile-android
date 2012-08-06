package org.digitalcampus.mtrain.model;

import java.io.Serializable;

public class Module implements Serializable {
	
	public static final String TAG = "Module";
	private int modId;
	private String location;
	private String title;
	private String shortname;

	public Module() {

	}

	public String getShortname() {
		return shortname;
	}

	public void setShortname(String shortname) {
		this.shortname = shortname;
	}

	public int getModId() {
		return modId;
	}

	public void setModId(int modId) {
		this.modId = modId;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
}
