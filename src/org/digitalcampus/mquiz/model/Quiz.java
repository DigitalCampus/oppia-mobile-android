package org.digitalcampus.mquiz.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;


public class Quiz implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2416034891439585524L;
	private static final String TAG = "Quiz";
	private String ref;
	private String title;
	private String url;
	private int maxscore;
	private boolean checked;
	private int currentq = 0;
	private float userscore;
	
	
	public List<QuizQuestion> questions = new ArrayList<QuizQuestion>();
	
	public Quiz(String ref){
		this.setRef(ref);
	}
	
	public void addQuestion(QuizQuestion q){
		questions.add(q);
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
	
	public String getRef() {
		return ref;
	}
	public void setRef(String ref) {
		this.ref = ref;
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
