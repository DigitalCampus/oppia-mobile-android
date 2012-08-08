package org.digitalcampus.mquiz.model;

import java.io.Serializable;
import java.util.HashMap;

public class Response implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5970350772982572264L;
	private static final String TAG = "Response";
	private String text;
	private float score;
	private HashMap<String,String> props = new HashMap<String,String>();
	
	
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public float getScore() {
		return score;
	}
	
	public void setScore(float score) {
		this.score = score;
	}

	public void setProps(HashMap<String,String> props) {
		this.props = props;
	}
	
	public String getProp(String key) {
		return props.get(key);
	}
	
	
}
