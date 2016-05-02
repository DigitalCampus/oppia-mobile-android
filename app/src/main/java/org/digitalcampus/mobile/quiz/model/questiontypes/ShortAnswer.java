package org.digitalcampus.mobile.quiz.model.questiontypes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.digitalcampus.mobile.quiz.model.QuizQuestion;
import org.digitalcampus.mobile.quiz.model.Response;
import org.json.JSONException;
import org.json.JSONObject;

public class ShortAnswer implements Serializable, QuizQuestion {

    private static final long serialVersionUID = 3539362553016059321L;
    public static final String TAG = "ShortAnswer";
    private int id;
    private HashMap<String,String> title = new HashMap<String,String>();
    private List<Response> responseOptions = new ArrayList<Response>();
    private float userscore = 0;
    private List<String> userResponses = new ArrayList<String>();
    private HashMap<String,String> props = new HashMap<String,String>();
    private String feedback = "";
    private boolean feedbackDisplayed = false;

    @Override
    public void addResponseOption(Response r){
        responseOptions.add(r);
    }

    @Override
    public void mark(String lang){
        // loop through the responses
        // find whichever are set as selected and add up the responses

        float total = 0;
        for (Response r : responseOptions){
            Iterator<String> itr = this.userResponses.iterator();
            while(itr.hasNext()) {
                String a = itr.next();
                if (r.getTitle(lang).toLowerCase().equals(a.toLowerCase())){
                    total += r.getScore();
                    if(r.getFeedback(lang) != null && !(r.getFeedback(lang).equals(""))){
                        this.feedback = r.getFeedback(lang);
                    }
                }
            }
        }
        if (total == 0){
            for (Response r : responseOptions){
                if (r.getTitle(lang).toLowerCase().equals("*")){
                    if(r.getFeedback(lang) != null && !(r.getFeedback(lang).equals(""))){
                        this.feedback = r.getFeedback(lang);
                    }
                }
            }
        }
        int maxscore = Integer.parseInt(this.getProp("maxscore"));
        if (total > maxscore){
            userscore = maxscore;
        } else {
            userscore = total;
        }
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
        this.responseOptions = responses;
    }

    @Override
    public float getUserscore() {
        return this.userscore;
    }

    @Override
    public void setUserResponses(List<String> str) {
        if (!str.equals(this.userResponses)){
            this.setFeedbackDisplayed(false);
        }
        this.userResponses = str;
    }

    @Override
    public List<String> getUserResponses() {
        return this.userResponses;
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
    public List<Response> getResponseOptions() {
        return this.responseOptions;
    }

    @Override
    public String getFeedback(String lang) {
        // reset feedback back to nothing
        this.feedback = "";
        this.mark(lang);

        return this.feedback;
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