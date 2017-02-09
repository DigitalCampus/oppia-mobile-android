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

import org.digitalcampus.mobile.quiz.model.QuizQuestion;
import org.digitalcampus.mobile.quiz.model.Response;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by alex on 9.2.2017.
 */

public class DDImageOrText implements Serializable, QuizQuestion {

    /**
     *
     */
    private static final long serialVersionUID = 1531985882092687497L;
    public static final String TAG = "DDImageOrText";
    private int id;
    private HashMap<String,String> title = new HashMap<>();
    private float userscore = 0;
    private List<String> userResponses = new ArrayList<>();
    private HashMap<String,String> props = new HashMap<>();
    private boolean feedbackDisplayed = false;

    @Override
    public void addResponseOption(Response r) {
        // do nothing
    }

    @Override
    public List<Response> getResponseOptions() {
        return null;
    }

    @Override
    public List<String> getUserResponses() {
        return this.userResponses;
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
    public void setResponseOptions(List<Response> responses) {
        // do nothing
    }

    @Override
    public float getUserscore() {
        return this.userscore;
    }

    @Override
    public void setProps(HashMap<String,String> props) {
        this.props = props;
    }

    @Override
    public String getProp(String key) {
        return props.get(key);
    }

    @Override
    public void setUserResponses(List<String> str) {
        if (!str.equals(this.userResponses)){
            this.setFeedbackDisplayed(false);
        }
        this.userResponses = str;
    }

    @Override
    public String getFeedback(String lang) {
        return "";
    }

    @Override
    public int getMaxScore() {
        return Integer.parseInt(this.getProp("maxscore"));
    }

    @Override
    public JSONObject responsesToJSON() {
        JSONObject jo = new JSONObject();

        if(userResponses.size() == 0){
            try {
                jo.put("question_id", this.id);
                jo.put("score",userscore);
                jo.put("text", "");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jo;
        }

        for(String ur: userResponses ){
            try {
                jo.put("question_id", this.id);
                jo.put("score",userscore);
                jo.put("text", ur);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return jo;
    }

    @Override
    public boolean responseExpected() {
        if (this.props.containsKey("required")){
            return Boolean.parseBoolean(this.getProp("required"));
        }
        return true;
    }

    @Override
    public int getScoreAsPercent() {
        if (this.getMaxScore() > 0){
            return (int) (100 * this.getUserscore()) / this.getMaxScore();
        } else {
            return 0;
        }
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
