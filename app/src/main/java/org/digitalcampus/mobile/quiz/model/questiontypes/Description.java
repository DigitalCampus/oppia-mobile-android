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

package org.digitalcampus.mobile.quiz.model.questiontypes;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.digitalcampus.mobile.quiz.Quiz;
import org.digitalcampus.mobile.quiz.model.QuizQuestion;
import org.digitalcampus.mobile.quiz.model.Response;
import org.json.JSONException;
import org.json.JSONObject;

public class Description implements QuizQuestion {

    private static final long serialVersionUID = 809312927290284785L;
    public static final String TAG = Description.class.getSimpleName();

    private int id;
    private float userscore = 0;
    private HashMap<String,String> title = new HashMap<>();
    private HashMap<String,String> props = new HashMap<>();
    private boolean feedbackDisplayed = false;

    @Override
    public void addResponseOption(Response r) {
        //do nothing
    }

    @Override
    public List<Response> getResponseOptions() {
        // nothing
        return null;
    }

    @Override
    public void setUserResponses(List<String> str) {
        // nothing
    }

    @Override
    public List<String> getUserResponses() {
        // nothing
        return null;
    }

    @Override
    public void setResponseOptions(List<Response> responses) {
        // nothing

    }

    @Override
    public void mark(String lang) {
        this.userscore = 0;
    }

    @Override
    public int getID() {
        return this.id;
    }

    @Override
    public void setID(int id) {
        this.id = id;
    }

    @Override
    public String getTitle(String lang) {
        if(title.containsKey(lang)){
            return title.get(lang);
        } else if (!title.isEmpty()){
            return title.entrySet().iterator().next().getValue();
        }
        else{
            return "";
        }
    }

    @Override
    public void setTitleForLang(String lang, String title) {
        this.title.put(lang, title);
    }

    @Override
    public float getUserscore() {
        return this.userscore;
    }

    @Override
    public void setProps(HashMap<String, String> props) {
        this.props = props;
    }

    @Override
    public String getProp(String key) {
        return props.get(key);
    }

    @Override
    public String getFeedback(String lang) {
        return "";
    }

    @Override
    public int getMaxScore() {
        return 0;
    }

    @Override
    public JSONObject responsesToJSON() {
        JSONObject jo = new JSONObject();
        try {
            jo.put(Quiz.JSON_PROPERTY_QUESTION_ID, this.id);
            jo.put(Quiz.JSON_PROPERTY_SCORE,0);
            jo.put(Quiz.JSON_PROPERTY_TEXT, null);
        } catch (JSONException jsone) {
            Log.d(TAG,"Error creating json object", jsone);
        }
        return jo;
    }

    @Override
    public boolean responseExpected() {
        return false;
    }

    @Override
    public int getScoreAsPercent() {
        return 0;
    }

    @Override
    public void setFeedbackDisplayed(boolean feedbackDisplayed) {
        this.feedbackDisplayed = feedbackDisplayed;

    }

    @Override
    public boolean getFeedbackDisplayed() {
        return feedbackDisplayed;
    }

}