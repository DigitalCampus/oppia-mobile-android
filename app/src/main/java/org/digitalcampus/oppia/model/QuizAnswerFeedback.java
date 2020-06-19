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

import java.util.List;

public class QuizAnswerFeedback {

	public static final String TAG = QuizAnswerFeedback.class.getSimpleName();
	
	private float score;
	private String questionText;
	private String feedbackText;
	private List<String> userResponse;
	private boolean isSurvey;
	
	public float getScore() {
		return score;
	}
	public void setScore(float score) {
		this.score = score;
	}
	public String getQuestionText() {
		return questionText;
	}
	public void setQuestionText(String questionText) {
		this.questionText = questionText;
	}
	public String getFeedbackText() {
		return feedbackText;
	}
	public void setFeedbackText(String feedbackText) {
		this.feedbackText = feedbackText;
	}
	public List<String> getUserResponse() {
		return userResponse;
	}
	public void setUserResponse(List<String> userResponse) {
		this.userResponse = userResponse;
	}

	public boolean isSurvey() {
		return isSurvey;
	}

	public void setIsSurvey(boolean survey) {
		isSurvey = survey;
	}
}
