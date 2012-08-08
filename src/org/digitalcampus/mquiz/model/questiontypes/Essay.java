package org.digitalcampus.mquiz.model.questiontypes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.digitalcampus.mquiz.model.QuizQuestion;
import org.digitalcampus.mquiz.model.Response;

public class Essay implements Serializable, QuizQuestion {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1531985882092686497L;
	public static final String TAG = "Essay";
	private String refid;
	private String qtext;
	private String qhint;
	private float userscore = 0;
	private List<String> userResponses = new ArrayList<String>();
	private HashMap<String,String> props = new HashMap<String,String>();
	
	public void addResponseOption(Response r) {
		// do nothing
	}
	public List<Response> getResponseOptions() {
		return null;
	}
	
	public List<String> getUserResponses() {
		return this.userResponses;
	}
	
	public void mark() {
		this.userscore = 0;
	}
	
	public String getRefid() {
		// TODO Auto-generated method stub
		return this.refid;
	}
	
	public void setRefid(String refid) {
		this.refid = refid;	
	}

	public String getQtext() {
		return this.qtext;
	}
	
	public void setQtext(String qtext) {
		this.qtext = qtext;	
	}
	
	public void setResponseOptions(List<Response> responses) {
		// do nothing
	}
	
	public float getUserscore() {
		return this.userscore;
	}
	
	public String getQhint() {
		return this.qhint;
	}
	
	public void setQhint(String qhint) {
		this.qhint = qhint;
	}
	
	public void setProps(HashMap<String,String> props) {
		this.props = props;
	}
	
	public String getProp(String key) {
		return props.get(key);
	}
	
	public void setUserResponses(List<String> str) {
		this.userResponses = str;
	}
	
	public String getFeedback() {
		return "";
	}
	
	public int getMaxScore() {
		return Integer.parseInt(this.getProp("maxscore"));
	}

}
