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

public class Numerical implements Serializable, QuizQuestion {

	private static final long serialVersionUID = 808485823168202643L;
	public static final String TAG = "Numerical";
	private String title;
	private int id;
	private List<Response> responseOptions = new ArrayList<Response>();
	private float userscore = 0;
	private List<String> userResponses = new ArrayList<String>();
	private HashMap<String, String> props = new HashMap<String, String>();
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
		while (itr.hasNext()) {
			String a = itr.next();
			try {
				userAnswer = Float.parseFloat(a);
			} catch (NumberFormatException nfe) {
				BugSenseHandler.log(TAG, nfe);
			}
		}
		float score = 0;
		if (userAnswer != null) {
			float currMax = 0;
			// loop through the valid answers and check against these
			for (Response r : responseOptions) {
				try {
					Float respNumber = Float.parseFloat(r.getTitle());
					Float tolerance = (float) 0.0;
					if(r.getProp("tolerance") != null){
						tolerance = Float.parseFloat(r.getProp("tolerance"));
					}
					 
					if ((respNumber - tolerance <= userAnswer) && (userAnswer <= respNumber + tolerance)) {
						if (r.getScore() > currMax) {
							score = r.getScore();
							currMax = r.getScore();
							if (r.getProp("feedback") != null && !r.getProp("feedback").equals("")){
								this.feedback = r.getProp("feedback");
							}
						}
					}
				} catch (NumberFormatException nfe) {
					BugSenseHandler.log(TAG, nfe);
				}
			}
		}

		int maxscore = Integer.parseInt(this.getProp("maxscore"));
		if (score > maxscore) {
			this.userscore = maxscore;
		} else {
			this.userscore = score;
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

	public void setProps(HashMap<String, String> props) {
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
			jo.put("question_id", this.id);
			jo.put("score", userscore);
			for (String ur : userResponses) {
				jo.put("text", ur);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			BugSenseHandler.log(TAG, e);
		}
		return jo;
	}

}
