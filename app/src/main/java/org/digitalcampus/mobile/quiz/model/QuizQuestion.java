package org.digitalcampus.mobile.quiz.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;


public interface QuizQuestion extends Serializable {


    public abstract void addResponseOption(Response r);

    public abstract List<Response> getResponseOptions();

    public abstract void setUserResponses(List<String> str);

    public abstract List<String> getUserResponses();

    public abstract void setResponseOptions(List<Response> responses);

    public abstract void mark(String lang);

    public abstract int getID();

    public abstract void setID(int id);

    public abstract String getTitle(String lang);

    public abstract void setTitleForLang(String lang, String title);

    public abstract float getUserscore();

    public abstract void setProps(HashMap<String,String> props);

    public abstract String getProp(String key);

    public abstract String getFeedback(String lang);

    public abstract int getMaxScore();

    public abstract JSONObject responsesToJSON();

    public abstract boolean responseExpected();

    public abstract int getScoreAsPercent();

    public abstract void setFeedbackDisplayed(boolean feedbackDisplayed);

    public abstract boolean getFeedbackDisplayed();


}