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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.digitalcampus.mobile.quiz.Quiz;
import org.digitalcampus.mobile.quiz.model.QuizQuestion;
import org.digitalcampus.mobile.quiz.model.Response;
import org.json.JSONException;
import org.json.JSONObject;

public class Matching implements Serializable, QuizQuestion {

    private static final long serialVersionUID = -7500128521011492086L;
    public static final String TAG = "Matching";
    private int id;
    private HashMap<String,String> title = new HashMap<>();
    private List<Response> responseOptions = new ArrayList<>();
    private float userscore = 0;
    private List<String> userResponses = new ArrayList<>();
    private HashMap<String, String> props = new HashMap<>();
    private String feedback = "";
    private boolean feedbackDisplayed = false;

    public void addResponseOption(Response r) {
        responseOptions.add(r);
    }

    public List<Response> getResponseOptions() {
        return responseOptions;
    }

    public void mark(String lang) {
        // loop through the responses
        // find whichever are set as selected and add up the responses

        float total = 0;

        for (Response r : responseOptions) {
            for (String ur : userResponses) {
                if (ur.equals(r.getTitle(lang))) {
                    total += r.getScore();
                }
            }

            // fix marking so that if one of the incorrect scores is selected
            // final mark is 0
            for (String ur : userResponses) {
                if (r.getTitle(lang).equals(ur) && r.getScore() == 0) {
                    total = 0;
                }
            }
        }
        int maxscore = Integer.parseInt(this.getProp("maxscore"));
        if (total > maxscore) {
            userscore = maxscore;
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

    public void setResponseOptions(List<Response> responses) {
        this.responseOptions = responses;
    }

    public float getUserscore() {
        return this.userscore;
    }

    public void setUserResponses(List<String> str) {
        if (!str.equals(this.userResponses)){
            this.setFeedbackDisplayed(false);
        }
        this.userResponses = str;
    }

    public void setProps(HashMap<String, String> props) {
        this.props = props;
    }

    public String getProp(String key) {
        return props.get(key);
    }

    public List<String> getUserResponses() {
        return this.userResponses;
    }

    public String getFeedback(String lang) {
        this.feedback = "";
        this.mark(lang);
        if(this.getScoreAsPercent() >= Quiz.QUIZ_QUESTION_PASS_THRESHOLD
                && this.getProp("correctfeedback") != null
                && !this.getProp("correctfeedback").equals("")){
            return this.getFeedback(lang,"correctfeedback");
        } else if(this.getScoreAsPercent() == 0
                && this.getProp("incorrectfeedback") != null
                && !this.getProp("incorrectfeedback").equals("")){
            return this.getFeedback(lang,"incorrectfeedback");
        } else if (this.getProp("partiallycorrectfeedback") != null
                && !this.getProp("partiallycorrectfeedback").equals("")){
            return this.getFeedback(lang,"partiallycorrectfeedback");
        } else {
            return this.feedback;
        }
    }

    private String getFeedback(String lang, String fbKey){
        try {
            JSONObject feedbackLangs = new JSONObject(this.getProp(fbKey));
            if(feedbackLangs.has(lang)){
                return feedbackLangs.getString(lang);
            } else {
                Iterator<?> keys = feedbackLangs.keys();
                while( keys.hasNext() ){
                    String key = (String) keys.next();
                    return feedbackLangs.getString(key);
                }
                return "";
            }
        } catch (JSONException e) {
            return this.getProp(fbKey);
        }
    }

    public int getMaxScore() {
        return Integer.parseInt(this.getProp("maxscore"));
    }

    public JSONObject responsesToJSON() {
        JSONObject jo = new JSONObject();
        try {
            jo.put("question_id", this.id);
            jo.put("score", this.getUserscore());
            String qrtext = "";
            for (String ur : userResponses) {
                qrtext += ur + Quiz.RESPONSE_SEPARATOR;
            }
            jo.put("text", qrtext);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jo;
    }

    @Override
    public boolean responseExpected() {
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