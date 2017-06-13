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

import org.digitalcampus.mobile.quiz.Quiz;
import org.digitalcampus.mobile.quiz.model.QuizQuestion;
import org.digitalcampus.mobile.quiz.model.Response;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DragAndDrop implements Serializable, QuizQuestion {

    private static final long serialVersionUID = 8250801428572869330L;
    public static final String TAG = "DragAndDrop";
    private int id;
    private HashMap<String,String> title = new HashMap<String,String>();
    private List<Response> responseOptions = new ArrayList<Response>();
    private float userscore = 0;
    private List<String> userResponses = new ArrayList<String>();
    private HashMap<String,String> props = new HashMap<String,String>();
    private String feedback = "";
    private boolean feedbackDisplayed = false;

    public static final String TYPE_DROPZONE = "dropzone";
    public static final String TYPE_DRAGGABLE = "drag";

    @Override
    public void addResponseOption(Response r){
        responseOptions.add(r);
    }

    @Override
    public List<Response> getResponseOptions(){
        return responseOptions;
    }

    @Override
    public void mark(String lang){

        int dropzones = 0;
        for (Response r : responseOptions){
            if (TYPE_DROPZONE.equals(r.getProp("type"))){
                dropzones++;
            }
        }
        // if not all the drops where filled, mark as failed
        if (userResponses.size() < dropzones){
            userscore = 0;
            return;
        }

        // loop through the responses
        // find whichever are set as selected and add up the responses
        boolean passed = true;
        for (Response r : this.responseOptions){
            if (r.getProp("xleft") == null || r.getProp("ytop") == null){
                //only a draggable, no need to check
                continue;
            }

            for (String a : this.userResponses){
                String[] temp = a.split(Quiz.MATCHING_REGEX,-1);
                if (temp.length < 2) continue;
                String dropzone = temp[0].trim();
                String draggable = temp[1].trim();

                if (dropzone.equals(r.getProp("choice"))){
                    passed &= dropzone.equals(draggable);
                }
            }
        }

        if (!passed){
            userscore = 0;
        }
        else if(this.getProp("maxscore") != null){
            userscore = Integer.parseInt(this.getProp("maxscore"));
        }
        else{
            userscore = 1;
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
    public void setUserResponses(List<String> str) {
        if (!str.equals(this.userResponses)){
            this.setFeedbackDisplayed(false);
        }
        this.userResponses = str;
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
