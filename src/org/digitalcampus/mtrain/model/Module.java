package org.digitalcampus.mtrain.model;

import java.io.Serializable;
import java.util.HashMap;

public class Module implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4412987572522420704L;
	
	public static final String TAG = "Module";
	private int modId;
	private String location;
	private String title;
	private String shortname;
	private float progress = 0;
	private HashMap<String,String> props;

	public HashMap<String, String> getProps() {
		return props;
	}

	public void setProps(HashMap<String, String> props) {
		this.props = props;
	}

	public float getProgress() {
		return progress;
	}

	public void setProgress(float progress) {
		this.progress = progress;
	}

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
