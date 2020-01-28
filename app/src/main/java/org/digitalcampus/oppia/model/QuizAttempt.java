/* 
 * This file is part of OppiaMobile - https://digital-campus.org/
 * 
 * OppiaMobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * OppiaMobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with OppiaMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package org.digitalcampus.oppia.model;

import android.content.Context;
import android.text.TextUtils;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.application.MobileLearning;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

public class QuizAttempt implements Serializable {

	public static final String TAG = QuizAttempt.class.getSimpleName();
	
	private DateTime datetime;
	private long id;
	private String data;
	private String activityDigest;
	private boolean sent;
	private long courseId;
	private long userId;
	private float score;
	private float maxscore;
	private boolean passed;
	private User user;
	private String event;
    private int points;
    private int timetaken;

    private String courseTitle;
    private String quizTitle;
    private String sectionTitle;

	public DateTime getDatetime() {
		return datetime;
	}
	
	public void setDatetime(DateTime datetime) {
		this.datetime = datetime;
	}

	public void setDateTime(String date) {
		this.datetime = MobileLearning.DATETIME_FORMAT.parseDateTime(date);
	}

	public String getDateTimeString() {
		return MobileLearning.DATETIME_FORMAT.print(datetime);
	}
	
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public String getData() {
		return data;
	}
	
	public void setData(String data) {
		this.data = data;
	}
	
	public String getActivityDigest() {
		return activityDigest;
	}

	public void setActivityDigest(String activityDigest) {
		this.activityDigest = activityDigest;
	}

	public boolean isSent() {
		return sent;
	}
	
	public void setSent(boolean sent) {
		this.sent = sent;
	}
	
	public long getCourseId() {
		return courseId;
	}
	
	public void setCourseId(long courseId) {
		this.courseId = courseId;
	}
	
	public long getUserId() {
		return userId;
	}
	
	public void setUserId(long userId) {
		this.userId = userId;
	}
	
	public float getScore() {
		return score;
	}
	
	public float getScoreAsPercent(){
		return this.score*100/this.maxscore;
	}

	public String getScorePercentLabel(){
		return Math.round(getScoreAsPercent()) + "%";
	}
	
	public void setScore(float score) {
		this.score = score;
	}
	
	public float getMaxscore() {
		return maxscore;
	}
	
	public void setMaxscore(float maxscore) {
		this.maxscore = maxscore;
	}
	
	public boolean isPassed() {
		return passed;
	}
	
	public void setPassed(boolean passed) {
		this.passed = passed;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public static String asJSONCollectionString(Collection<QuizAttempt> quizAttempts){

		ArrayList<String> jsonQuizAttempts = new ArrayList<>();
		for (QuizAttempt qa : quizAttempts){
			jsonQuizAttempts.add(qa.getData());
		}
		return "[" +  TextUtils.join(",", jsonQuizAttempts) + "]";

	}

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

	public int getTimetaken() {
		return timetaken;
	}

	public void setTimetaken(int timetaken) {
		this.timetaken = timetaken;
	}

	public String getHumanTimetaken(){
		return String.format("%d min %ds",
				TimeUnit.MILLISECONDS.toSeconds(timetaken)/60,
				TimeUnit.MILLISECONDS.toSeconds(timetaken) % 60 );
	}

	public String getCourseTitle() {
		return courseTitle;
	}

	public void setCourseTitle(String courseTitle) {
		this.courseTitle = courseTitle;
	}

	public String getQuizTitle() {
		return quizTitle;
	}

	public void setQuizTitle(String quizTitle) {
		this.quizTitle = quizTitle;
	}

	public String getSectionTitle() {
		return sectionTitle;
	}

	public void setSectionTitle(String sectionTitle) {
		this.sectionTitle = sectionTitle;
	}

	public String getDisplayTitle(Context ctx){
	    if (sectionTitle == null || quizTitle == null){
	        return ctx.getString(R.string.quiz_attempts_unkwnown_quiz);
        }
	    return sectionTitle + " > " + quizTitle;
    }
}
