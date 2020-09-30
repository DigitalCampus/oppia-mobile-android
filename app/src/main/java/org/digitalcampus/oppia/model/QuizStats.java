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

import android.util.Log;

import java.io.Serializable;

public class QuizStats implements Serializable {

	public static final String TAG = QuizStats.class.getSimpleName();

    private String digest;
    private int numAttempts;
    private boolean attempted;
    private float maxScore = -1;
    private float userScore = -1;
    private float averageScore = -1;
    private boolean passed;

    private String quizTitle;
    private String sectionTitle;

    public String getDigest() { return digest; }
    public void setDigest(String digest) { this.digest = digest; }

    public float getMaxScore() {
        return maxScore;
    }
    public void setMaxScore(float maxScore) {
        this.maxScore = maxScore;
    }

    public float getUserScore() { return userScore; }
    public void setUserScore(float userScore) { this.userScore = userScore; }

    public void setAverageScore(float averageScore) {
        this.averageScore = averageScore;
    }

    public boolean isAttempted(){ return attempted; }
    public void setAttempted(boolean a){
        attempted = a;
    }

    public int getPercent(){
    	Log.d(TAG, "userScore:" + userScore);
    	Log.d(TAG, "maxScore:" + maxScore);
    	int percent =  Math.round(userScore * 100.0f / Math.max(1,maxScore));
    	Log.d(TAG, "percent:" + percent);
        return percent;
    }

    public int getAveragePercent(){
        return  Math.round(averageScore * 100.0f / Math.max(1,maxScore));
    }

    public boolean isPassed(){
        return passed;
    }
    public void setPassed(boolean passed){ this.passed = passed; }

    public int getNumAttempts() { return numAttempts; }
    public void setNumAttempts(int numAttempts) { this.numAttempts = numAttempts; }

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
}
