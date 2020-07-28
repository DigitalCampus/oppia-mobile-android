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

package org.digitalcampus.mobile.quiz.model;

import android.util.Log;

import com.splunk.mint.Mint;

import org.digitalcampus.mobile.quiz.Quiz;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuizQuestion implements Serializable {

    public static final String TAG = QuizQuestion.class.getSimpleName();
    private static final long serialVersionUID = 852385823168202643L;

    protected int id;
    protected float userscore = 0;
    private Map<String, String> title = new HashMap<>();
    private Map<String, String> props = new HashMap<>();
    private boolean feedbackDisplayed = false;
    protected List<Response> responseOptions = new ArrayList<>();
    protected List<String> userResponses = new ArrayList<>();
    protected String feedback = "";

    public void addResponseOption(Response r) {
        responseOptions.add(r);
    }

    public List<Response> getResponseOptions() {
        return responseOptions;
    }

    public void setUserResponses(List<String> str) {
        if (!str.equals(this.userResponses)) {
            this.setFeedbackDisplayed(false);
        }
        this.userResponses = str;
    }

    public List<String> getUserResponses() {
        return this.userResponses;
    }

    public void mark(String lang) {
        // loop through the responses
        // find whichever are set as selected and add up the responses
        float total = 0;
        for (Response r : responseOptions) {
            for (String a : userResponses) {
                if (r.getTitle(lang).equals(a)) {
                    total += r.getScore();
                    if (r.getFeedback(lang) != null && !(r.getFeedback(lang).equals(""))) {
                        feedback = r.getFeedback(lang);
                    }
                }
            }
        }
        this.calculateUserscore(total);
    }

    public void calculateUserscore(float total){
        if (this.getProp(Quiz.JSON_PROPERTY_MAXSCORE) != null) {
            int maxscore = Integer.parseInt(this.getProp(Quiz.JSON_PROPERTY_MAXSCORE));
            if (total > maxscore) {
                userscore = maxscore;
            } else {
                userscore = total;
            }
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

    public String getTitle(String lang) {
        if (title.containsKey(lang)) {
            return title.get(lang);
        } else if (!title.entrySet().isEmpty()) {
            return title.entrySet().iterator().next().getValue();
        } else {
            return "";
        }
    }

    public void setTitleForLang(String lang, String title) {
        this.title.put(lang, title);
    }

    public float getUserscore() {
        return this.userscore;
    }

    public void setProps(Map<String, String> props) {
        this.props = props;
    }

    public Map<String, String> getProps() {
        return this.props;
    }

    public String getProp(String key) {
        return props.get(key);
    }

    public String getFeedback(String lang) {
        // reset feedback back to nothing
        this.feedback = "";
        this.mark(lang);
        return this.feedback;
    }

    public int getMaxScore() {
        return Integer.parseInt(this.getProp(Quiz.JSON_PROPERTY_MAXSCORE));
    }

    public JSONObject responsesToJSON() {
        JSONObject jo = new JSONObject();
        try {
            jo.put(Quiz.JSON_PROPERTY_QUESTION_ID, this.id);
            jo.put(Quiz.JSON_PROPERTY_SCORE, 0);
            jo.put(Quiz.JSON_PROPERTY_TEXT, null);
        } catch (JSONException jsone) {
            Log.d(TAG, "Error creating json object", jsone);
            Mint.logException(jsone);
        }
        return jo;
    }

    public boolean responseExpected() {
        if (props.containsKey(Quiz.JSON_PROPERTY_REQUIRED)) {
            return Boolean.parseBoolean(this.getProp(Quiz.JSON_PROPERTY_REQUIRED));
        }
        return true;
    }

    public int getScoreAsPercent() {
        if (this.getMaxScore() > 0) {
            return (int) (100 * this.getUserscore()) / this.getMaxScore();
        } else {
            return 0;
        }
    }

    public void setFeedbackDisplayed(boolean feedbackDisplayed) {
        this.feedbackDisplayed = feedbackDisplayed;

    }

    public boolean getFeedbackDisplayed() {
        return feedbackDisplayed;
    }

}