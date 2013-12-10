package org.digitalcampus.oppia.model;

import java.io.Serializable;
import java.util.ArrayList;

public class Tag implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5144400570961137778L;
	
	public final static String TAG = Tag.class.getSimpleName();
	private String name;
	private int count;
	private int id;
	private ArrayList<Course> courses = new ArrayList<Course>();
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public ArrayList<Course> getCourses() {
		return courses;
	}
	public void setCourses(ArrayList<Course> courses) {
		this.courses = courses;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	
}
