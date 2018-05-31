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

import android.util.Log;

import org.digitalcampus.mobile.quiz.Quiz;
import org.digitalcampus.mobile.quiz.model.QuizQuestion;
import org.digitalcampus.mobile.quiz.model.Response;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Numerical implements Serializable, QuizQuestion {

    private static final long serialVersionUID = 808485823168202643L;
    public static final String TAG = Numerical.class.getSimpleName();
    private HashMap<String,String> title = new HashMap<>();
    private int id;
    private List<Response> responseOptions = new ArrayList<>();
    private float userscore = 0;
    private List<String> userResponses = new ArrayList<>();
    private HashMap<String, String> props = new HashMap<>();
    private String feedback = "";
    private boolean feedbackDisplayed = false;

    @Override
    public void addResponseOption(Response r) {
        responseOptions.add(r);
    }

    @Override
    public List<Response> getResponseOptions() {
        return responseOptions;
    }

    @Override
    public List<String> getUserResponses() {
        return this.userResponses;
    }

    @Override
    public void mark(String lang) {
        Float userAnswer = null;
        this.userscore = 0;
        for (String a : this.userResponses) {
            try {
                userAnswer = Float.parseFloat(a);
            } catch (NumberFormatException nfe) {
            }
        }
        float score = 0;
        if (userAnswer != null) {
            float currMax = 0;
            // loop through the valid answers and check against these
            for (Response r : responseOptions) {
                try {
                    Float respNumber = Float.parseFloat(r.getTitle(lang));
                    Float tolerance = (float) 0.0;
                    if(r.getProp("tolerance") != null){
                        tolerance = Float.parseFloat(r.getProp("tolerance"));
                    }

                    if ((respNumber - tolerance <= userAnswer) && (userAnswer <= respNumber + tolerance)) {
                        if (r.getScore() > currMax) {
                            score = r.getScore();
                            currMax = r.getScore();
                            if(r.getFeedback(lang) != null && !(r.getFeedback(lang).equals(""))){
                                this.feedback = r.getFeedback(lang);
                            }
                        }
                    }
                } catch (NumberFormatException nfe) {
                    // do nothing - just skip over this particular response option
                }
            }
        }

        if (score == 0){
            for (Response r : responseOptions){
                if (r.getTitle(lang).toLowerCase().equals("*")){
                    if(r.getProp(Quiz.JSON_PROPERTY_FEEDBACK) != null && !(r.getProp(Quiz.JSON_PROPERTY_FEEDBACK).equals(""))){
                        this.feedback = r.getProp(Quiz.JSON_PROPERTY_FEEDBACK);
                    }
                }
            }
        }

        int maxscore = Integer.parseInt(this.getProp(Quiz.JSON_PROPERTY_MAXSCORE));
        if (score > maxscore) {
            this.userscore = maxscore;
        } else {
            this.userscore = score;
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
        this.responseOptions = responses;
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
        return Integer.parseInt(this.getProp(Quiz.JSON_PROPERTY_MAXSCORE));
    }

    @Override
    public JSONObject responsesToJSON() {
        JSONObject jo = new JSONObject();
        try {
            jo.put(Quiz.JSON_PROPERTY_QUESTION_ID, this.id);
            jo.put(Quiz.JSON_PROPERTY_SCORE, userscore);
            for (String ur : userResponses) {
                jo.put(Quiz.JSON_PROPERTY_TEXT, ur);
            }
        } catch (JSONException jsone) {
            Log.d(TAG,"Error creating json object", jsone);
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