package org.digitalcampus.mquiz.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;


public interface QuizQuestion extends Serializable {
	
	public void addResponseOption(Response r); 
	
	public List<Response> getResponseOptions(); 
	
	public void setUserResponses(List<String> str);
	
	public List<String> getUserResponses();
	
	public void setResponseOptions(List<Response> responses); 
	
	public void mark();
	
	public String getRefid();
	
	public void setRefid(String refid);
	
	public String getQtext();
	
	public void setQtext(String qtext);

	public float getUserscore();

	public String getQhint();

	public void setQhint(String qhint);
	
	public void setProps(HashMap<String,String> props);
	
	public String getProp(String key);
	
	public String getFeedback();
	
	public int getMaxScore();
	
	public JSONObject responsesToJSON();
	
}
