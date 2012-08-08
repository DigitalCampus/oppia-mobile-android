package org.digitalcampus.mquiz.model.questiontypes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.digitalcampus.mquiz.model.QuizQuestion;
import org.digitalcampus.mquiz.model.Response;

public class ShortAnswer implements Serializable, QuizQuestion {

	private static final long serialVersionUID = 3539362553016059321L;
	public static final String TAG = "ShortAnswer";
	private int dbid;
	private String refid;
	private String quizrefid;
	private int orderno;
	private String qtext;
	private String qhint;
	private List<Response> responseOptions = new ArrayList<Response>();
	private float userscore = 0;
	private List<String> userResponses = new ArrayList<String>();
	private HashMap<String,String> props = new HashMap<String,String>();
	
	public void addResponseOption(Response r){
		responseOptions.add(r);
	}

	
	public void mark(){
		// loop through the responses
		// find whichever are set as selected and add up the responses
		
		float total = 0;
		for (Response r : responseOptions){
			Iterator<String> itr = this.userResponses.iterator();
			while(itr.hasNext()) {
				String a = itr.next(); 
				if (r.getText().toLowerCase().equals(a.toLowerCase())){
					total += r.getScore();
				}
			}
		}
		int maxscore = Integer.parseInt(this.getProp("maxscore"));
		if (total > maxscore){
			userscore = maxscore;
		} else {
			userscore = total;
		}
	}
	
	public String getRefid() {
		return refid;
	}
	
	public void setRefid(String refid) {
		this.refid = refid;
	}
	
	public String getQuizRefid() {
		return quizrefid;
	}
	
	public void setQuizRefid(String quizrefid) {
		this.quizrefid = quizrefid;
	}
	
	public int getOrderno() {
		return orderno;
	}
	
	public void setOrderno(int orderno) {
		this.orderno = orderno;
	}
	
	public String getQtext() {
		return qtext;
	}
	
	public void setQtext(String qtext) {
		this.qtext = qtext;
	}

	public int getDbid() {
		return dbid;
	}

	public void setDbid(int dbid) {
		this.dbid = dbid;
	}

	public void setResponseOptions(List<Response> responses) {
		this.responseOptions = responses;
	}

	public float getUserscore() {
		return this.userscore;
	}

	public String getQhint() {
		return qhint;
	}

	public void setQhint(String qhint) {
		this.qhint = qhint;
	}

	public void setUserResponses(List<String> str) {
		this.userResponses= str;
		
	}

	public List<String> getUserResponses() {
		return this.userResponses;
	}

	public void setProps(HashMap<String,String> props) {
		this.props = props;
	}
	
	public String getProp(String key) {
		return props.get(key);
	}

	public List<Response> getResponseOptions() {
		return this.responseOptions;
	}
	
	public String getFeedback() {
		// TODO return feedback
		return "";
	}

}
