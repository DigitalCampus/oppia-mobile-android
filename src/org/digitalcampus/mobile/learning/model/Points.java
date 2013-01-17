package org.digitalcampus.mobile.learning.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Points {

	private String date;
	private String description;
	private int points;
	
	public String getDate() {
		try {
			Date dt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(date);
			return new SimpleDateFormat("dd-MMM-yyyy").format(dt);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return date;
	}
	
	public String getTime() {
		try {
			Date dt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(date);
			return new SimpleDateFormat("H:mm").format(dt);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return date;
	}
	
	public void setDate(String date) {
		this.date = date;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public int getPoints() {
		return points;
	}
	
	public void setPoints(int points) {
		this.points = points;
	}
	
	
	
}
