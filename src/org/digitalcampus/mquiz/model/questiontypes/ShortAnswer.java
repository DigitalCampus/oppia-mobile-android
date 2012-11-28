package org.digitalcampus.mquiz.model.questiontypes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.digitalcampus.mquiz.model.QuizQuestion;
import org.digitalcampus.mquiz.model.Response;
import org.json.JSONException;
import org.json.JSONObject;

import com.bugsense.trace.BugSenseHandler;

public class ShortAnswer implements Serializable, QuizQuestion {

	private static final long serialVersionUID = 3539362553016059321L;
	public static final String TAG = "ShortAnswer";
	private int id;
	private String title;
	private List<Response> responseOptions = new ArrayList<Response>();
	private float userscore = 0;
	private List<String> userResponses = new ArrayList<String>();
	private HashMap<String,String> props = new HashMap<String,String>();
	private String feedback = "";
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
				if (r.getTitle().toLowerCase().equals(a.toLowerCase())){
					total += r.getScore();
					// TODO return feedback
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
	
	public int getID() {
		return this.id;
	}
	
	public void setID(int id) {
		this.id = id;	
	}
	
	public String getTitle() {
		return this.title;
	}
	
	public void setTitle(String title) {
		this.title = title;	
	}

	public void setResponseOptions(List<Response> responses) {
		this.responseOptions = responses;
	}

	public float getUserscore() {
		return this.userscore;
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
		// reset feedback back to nothing
		this.feedback = "";
		this.mark();
		
		return this.feedback;
	}
	
	public int getMaxScore() {
		return Integer.parseInt(this.getProp("maxscore"));
	}
	
	public JSONObject responsesToJSON() {
		JSONObject jo = new JSONObject();
		for(String ur: userResponses ){
			try {
				jo.put("question_id", this.id);
				jo.put("score",userscore);
				jo.put("text", ur);
			} catch (JSONException e) {
				e.printStackTrace();
				BugSenseHandler.log(TAG, e);
			}
		}
		return jo;
	}

}
