package org.digitalcampus.mquiz.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;


public interface QuizQuestion extends Serializable {
	
	public void addResponseOption(Response r); //was addResponse
	
	public List<Response> getResponseOptions(); // was getRespones
	
	public void setUserResponses(List<String> str); // was setResponse
	
	public List<String> getUserResponses(); //was getResponse
	
	public void setResponseOptions(List<Response> responses); // setResponses
	
	public void mark();
	
	public String getRefid();
	
	public void setRefid(String refid);
	
	public String getQuizRefid();
	
	public void setQuizRefid(String quizrefid);
	
	public int getOrderno();
	
	public void setOrderno(int orderno);
	
	public String getQtext();
	
	public void setQtext(String qtext);

	public int getDbid();

	public void setDbid(int dbid);

	public float getUserscore();

	public String getQhint();

	public void setQhint(String qhint);
	
	public void setProps(HashMap<String,String> props);
	
	public String getProp(String key);
	
	public String getFeedback();
	
}
