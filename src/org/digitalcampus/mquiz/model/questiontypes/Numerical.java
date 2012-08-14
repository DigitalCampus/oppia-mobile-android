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

public class Numerical implements Serializable, QuizQuestion {
	
	private static final long serialVersionUID = 808485823168202643L;
	public static final String TAG = "Numerical";
	private String refid;
	private String qtext;
	private String qhint;
	private List<Response> responseOptions = new ArrayList<Response>();
	private float userscore = 0;
	private List<String> userResponses = new ArrayList<String>();
	private HashMap<String,String> props = new HashMap<String,String>();
	private String feedback = "";
	

	public void addResponseOption(Response r) {
		responseOptions.add(r);
	}
	
	public List<Response> getResponseOptions() {
		return responseOptions;
	}
	
	public List<String> getUserResponses() {
		return this.userResponses;
	}
	
	public void mark() {
		Float userAnswer = null;
		this.userscore = 0;
		Iterator<String> itr = this.userResponses.iterator();
		while(itr.hasNext()) {
			String a = itr.next(); 
			try{
				userAnswer = Float.parseFloat(a);
			} catch (NumberFormatException nfe){
				
			}
		}
		float score = 0;
		if(userAnswer != null){
			float currMax = 0;
			// loop through the valid answers and check against these
			for (Response r : responseOptions){
				try{
					Float respNumber = Float.parseFloat(r.getText());
					//TODO check that tolerance is loaded as a property
					Float tolerance = Float.parseFloat(r.getProp("tolerance"));
					if ((respNumber - tolerance <= userAnswer) && (userAnswer <= respNumber + tolerance)){
						if(r.getScore() > currMax){
							score = r.getScore();
							currMax = r.getScore();
							if(!(r.getProp("feedback") == null)){
								this.feedback = r.getProp("feedback");
							}
						} 
					}
				} catch (NumberFormatException nfe){
					
				}
			}
		}
		
		int maxscore = Integer.parseInt(this.getProp("maxscore"));
		if (score > maxscore){
			this.userscore = maxscore;
		} else {
			this.userscore = score;
		}
	}
	
	public String getRefid() {
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
		this.responseOptions = responses;
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
				jo.put("qid", refid);
				jo.put("score",userscore);
				jo.put("qrtext", ur);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return jo;
	}

}
