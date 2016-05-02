package org.digitalcampus.mobile.quiz.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

public interface QuizQuestion extends Serializable {

    void addResponseOption(Response r);
    List<Response> getResponseOptions();
    void setUserResponses(List<String> str);
    List<String> getUserResponses();
    void setResponseOptions(List<Response> responses);
    void mark(String lang);
    int getID();
    void setID(int id);
    String getTitle(String lang);
    void setTitleForLang(String lang, String title);
    float getUserscore();
    void setProps(HashMap<String, String> props);
    String getProp(String key);
    String getFeedback(String lang);
    int getMaxScore();
    JSONObject responsesToJSON();
    boolean responseExpected();
    int getScoreAsPercent();
    void setFeedbackDisplayed(boolean feedbackDisplayed);
    boolean getFeedbackDisplayed();
}