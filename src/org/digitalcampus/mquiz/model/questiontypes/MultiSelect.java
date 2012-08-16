package org.digitalcampus.mquiz.model.questiontypes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.digitalcampus.mquiz.MQuiz;
import org.digitalcampus.mquiz.model.QuizQuestion;
import org.digitalcampus.mquiz.model.Response;
import org.json.JSONException;
import org.json.JSONObject;

public class MultiSelect implements Serializable, QuizQuestion {

	private static final long serialVersionUID = 936284577467681053L;
	public static final String TAG = "MultiSelect";
	private String refid;
	private String qtext;
	private String qhint;
	private List<Response> responseOptions = new ArrayList<Response>();
	private float userscore = 0;
	private List<String> userResponses = new ArrayList<String>();
	private HashMap<String,String> props = new HashMap<String,String>();
	private String feedback = "";
	
	public void addResponseOption(Response r){
		responseOptions.add(r);
	}
	
	public List<Response> getResponseOptions(){
		return responseOptions;
	}
	
	public void mark(){
		// loop through the responses
		// find whichever are set as selected and add up the responses
		
		float total = 0;
		
		for (Response r : responseOptions){
			for (String ur : userResponses) {
				//Log.d(TAG,"ur: "+ ur);
				//Log.d(TAG,"r.getText(): "+ r.getText());
				if (ur.equals(r.getText())) {
					total += r.getScore();
					if(!r.getProp("feedback").equals("")){
						this.feedback += ur + ": " + r.getProp("feedback") + "\n\n";
					}
					//Log.d(TAG,"match");
				}  else {
					//Log.d(TAG," no match");
				}
			}
			//Log.d(TAG,"-----------");
			// fix marking so that if one of the incorrect scores is selected final mark is 0
			for(String ur: userResponses){
				if (r.getText().equals(ur) && r.getScore() == 0){
					total = 0;
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
	
	public String getQtext() {
		return qtext;
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
		return qhint;
	}

	public void setQhint(String qhint) {
		this.qhint = qhint;
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
		try {
			jo.put("qid", refid);
			jo.put("score",userscore);
			String qrtext = "";
			for(String ur: userResponses ){
				qrtext += ur + MQuiz.RESPONSE_SEPARATOR;
			}
			jo.put("qrtext", qrtext);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jo;
	}

}
