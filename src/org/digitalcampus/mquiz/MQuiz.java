package org.digitalcampus.mquiz;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.digitalcampus.mquiz.model.QuizQuestion;
import org.digitalcampus.mquiz.model.Response;
import org.digitalcampus.mquiz.model.questiontypes.Essay;
import org.digitalcampus.mquiz.model.questiontypes.Matching;
import org.digitalcampus.mquiz.model.questiontypes.MultiChoice;
import org.digitalcampus.mquiz.model.questiontypes.Numerical;
import org.digitalcampus.mquiz.model.questiontypes.ShortAnswer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class MQuiz implements Serializable{

	private static final long serialVersionUID = -2416034891439585524L;
	private static final String TAG = "MQuiz";
	private String qref;
	private String title;
	private String url;
	private int maxscore;
	private boolean checked;
	private int currentq = 0;
	private float userscore;
	private List<QuizQuestion> questions = new ArrayList<QuizQuestion>();
	
	public MQuiz(){
		
	}
	
	public boolean load(String quiz){
		try {
			JSONObject json = new JSONObject(quiz);
			qref = (String) json.get("qref");
			title = (String) json.get("quiztitle");
			maxscore = Integer.parseInt((String) json.get("maxscore"));
			// add questions
			JSONArray questions = (JSONArray) json.get("q");
			for (int i=0; i<questions.length(); i++){
				this.addQuestion((JSONObject) questions.get(i));
			}
		} catch (JSONException e){
			Log.d(TAG,"quiz not loaded");
			e.printStackTrace();
			return false;
		}
		Log.d(TAG,"quiz loaded");
		return true;
	}
	
	public boolean addQuestion(JSONObject q) {
		//determine question type
		QuizQuestion question;
		String qtype;
		try {
			qtype = (String) q.get("type");
			if(qtype.toLowerCase().equals("essay")){
				question = new Essay();
			} else if(qtype.toLowerCase().equals("multichoice")){
				question = new MultiChoice();
			// TODO add other question types
			} else if(qtype.toLowerCase().equals("numerical")){
				question = new Numerical();
			} else if(qtype.toLowerCase().equals("matching")){
				question = new Matching();
			} else if(qtype.toLowerCase().equals("shortanswer")){
				question = new ShortAnswer();
			} else {
				Log.d(TAG,"Question type "+qtype+" is not yet supported");
				return false;
			}
			
			question.setRefid((String) q.get("refid"));
			question.setQtext((String) q.get("text"));
			question.setQhint((String) q.optString("hint"));
			JSONObject questionProps = (JSONObject) q.get("props");
			
			HashMap<String,String> qProps = new HashMap<String,String>();
			for (int k = 0; k < questionProps.names().length(); k++) {
				qProps.put(questionProps.names().getString(k), questionProps.getString(questionProps.names().getString(k)));
				Log.d(TAG,"Adding question prop: "+ questionProps.names().getString(k) +" : "+questionProps.getString(questionProps.names().getString(k)));
			}
			question.setProps(qProps);
			
			this.questions.add(question);
			Log.d(TAG,"question added:"+question.getQtext());
			
			// now add response options for this question
			JSONArray responses = (JSONArray) q.get("r");
			for (int j=0; j<responses.length(); j++){
				JSONObject r = (JSONObject) responses.get(j);
				Response responseOption = new Response();
				responseOption.setText((String) r.get("text"));
				responseOption.setScore(Float.parseFloat((String) r.get("score")));
				JSONObject responseProps = (JSONObject) r.get("props");
				HashMap<String,String> rProps = new HashMap<String,String>();
				if(responseProps.names() != null){
					for (int m = 0; m < responseProps.names().length(); m++) {
						rProps.put(responseProps.names().getString(m), responseProps.getString(responseProps.names().getString(m)));
						Log.d(TAG,"Adding response prop: "+ responseProps.names().getString(m) +" : "+responseProps.getString(responseProps.names().getString(m)));
					}
				}
				question.addResponseOption(responseOption);
			}
			
			
		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public boolean hasNext(){
		if (this.currentq+1 < questions.size()){
			return true;
		}
		return false;
	}
	
	public boolean hasPrevious(){
		if (this.currentq > 0){
			return true;
		}
		return false;
	}
	
	public void moveNext(){
		if (currentq+1 < questions.size()){
			currentq++;
		}
	}

	public void movePrevious(){
		if (currentq > 0){
			currentq--;
		}
	}
	
	public void mark(){
		float total = 0;
		for (QuizQuestion q : questions){
			q.mark();
			total += q.getUserscore();
		}
		if (total > maxscore){
			userscore = maxscore;
		} else {
			userscore = total;
		}
		//Log.d(TAG,"Total score: " + String.valueOf(userscore) + " out of "+ String.valueOf(maxscore));
	}
	
	public String getQRef() {
		return qref;
	}
	public void setQRef(String qref) {
		this.qref = qref;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String t) {
		this.title = t;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public boolean isChecked() {
		return checked;
	}
	public void setChecked(boolean checked) {
		this.checked = checked;
	}
	
	public int getCurrentq() {
		return currentq;
	}

	public void setCurrentq(int currentq) {
		this.currentq = currentq;
	}

	public float getUserscore() {
		return this.userscore;
	}

	public int getMaxscore() {
		return maxscore;
	}

	public void setMaxscore(int maxscore) {
		this.maxscore = maxscore;
	}
	
	
}
