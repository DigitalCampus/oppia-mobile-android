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

import com.bugsense.trace.BugSenseHandler;

public class Matching implements Serializable, QuizQuestion {

	private static final long serialVersionUID = -7500128521011492086L;
	public static final String TAG = "Matching";
	private int id;
	private String title;
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

	public void mark() {
		// loop through the responses
		// find whichever are set as selected and add up the responses

		float total = 0;

		for (Response r : responseOptions) {
			for (String ur : userResponses) {
				if (ur.equals(r.getTitle())) {
					total += r.getScore();
				} else {
				}
			}

			// fix marking so that if one of the incorrect scores is selected
			// final mark is 0
			for (String ur : userResponses) {
				if (r.getTitle().equals(ur) && r.getScore() == 0) {
					total = 0;
				}
			}
			
			// TODO add feedback
		}
		int maxscore = Integer.parseInt(this.getProp("maxscore"));
		if (total > maxscore) {
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
		this.userResponses = str;
	}

	public void setProps(HashMap<String, String> props) {
		this.props = props;
	}

	public String getProp(String key) {
		return props.get(key);
	}

	public List<String> getUserResponses() {
		return this.userResponses;
	}

	public String getFeedback() {
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
			jo.put("score", this.getUserscore());
			String qrtext = "";
			for (String ur : userResponses) {
				qrtext += ur + MQuiz.RESPONSE_SEPARATOR;
			}
			jo.put("text", qrtext);
		} catch (JSONException e) {
			e.printStackTrace();
			BugSenseHandler.log(TAG, e);
		}
		return jo;
	}

}
