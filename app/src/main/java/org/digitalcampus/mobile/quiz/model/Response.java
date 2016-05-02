package org.digitalcampus.mobile.quiz.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

public class Response implements Serializable{

    private static final long serialVersionUID = 5970350772982572264L;
    public static final String TAG = "Response";
    private HashMap<String,String> title = new HashMap<>();
    private float score;
    private HashMap<String,String> props = new HashMap<>();
    private HashMap<String,String> feedback = new HashMap<>();

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

    public void setTitleForLang(String lang, String title) {
        this.title.put(lang, title);
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public void setProps(HashMap<String,String> props) {
        this.props = props;
    }

    public String getProp(String key) {
        return props.get(key);
    }

    public void setFeedback(String defaultLang){
        if (this.props.containsKey("feedback")) try {
            JSONObject feedbackLangs = new JSONObject(this.getProp("feedback"));
            Iterator<?> keys = feedbackLangs.keys();

            while (keys.hasNext()) {
                String key = (String) keys.next();
                this.setFeedbackForLang(key, feedbackLangs.getString(key));
            }
        } catch (JSONException e) {
            this.setFeedbackForLang(defaultLang, this.getProp("feedback"));
        }
    }

    private void setFeedbackForLang(String lang, String title){
        this.feedback.put(lang, title);
    }

    public String getFeedback(String lang) {
        if(feedback.containsKey(lang)){
            return feedback.get(lang);
        } else {
            for (String key : feedback.keySet()) {
                return feedback.get(key);
            }
            return "";
        }
    }
}