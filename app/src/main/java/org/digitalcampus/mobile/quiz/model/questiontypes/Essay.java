package org.digitalcampus.mobile.quiz.model.questiontypes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.digitalcampus.mobile.quiz.model.QuizQuestion;
import org.digitalcampus.mobile.quiz.model.Response;
import org.json.JSONException;
import org.json.JSONObject;

public class Essay implements Serializable, QuizQuestion {

    /**
     *
     */
    private static final long serialVersionUID = 1531985882092686497L;
    public static final String TAG = "Essay";
    private int id;
    private HashMap<String,String> title = new HashMap<String,String>();
    private float userscore = 0;
    private List<String> userResponses = new ArrayList<String>();
    private HashMap<String,String> props = new HashMap<String,String>();
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
            int pc = Integer.valueOf((int) (100* this.getUserscore()))/this.getMaxScore();
            return pc;
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