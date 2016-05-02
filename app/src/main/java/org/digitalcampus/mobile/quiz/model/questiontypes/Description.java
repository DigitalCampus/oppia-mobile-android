package org.digitalcampus.mobile.quiz.model.questiontypes;

import java.util.HashMap;
import java.util.List;

import org.digitalcampus.mobile.quiz.model.QuizQuestion;
import org.digitalcampus.mobile.quiz.model.Response;
import org.json.JSONException;
import org.json.JSONObject;

public class Description implements QuizQuestion {

    /**
     *
     */
    private static final long serialVersionUID = 809312927290284785L;
    public static final String TAG = "Description";

    private int id;
    private float userscore = 0;
    private HashMap<String,String> title = new HashMap<String,String>();
    private HashMap<String,String> props = new HashMap<String,String>();
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
        } else {
            for (String key : title.keySet()) {
                return title.get(key);
            }
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
            jo.put("question_id", this.id);
            jo.put("score",0);
            jo.put("text", null);
        } catch (JSONException e) {
            e.printStackTrace();
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