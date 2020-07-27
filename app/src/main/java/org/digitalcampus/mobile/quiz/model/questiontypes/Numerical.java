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

public class Numerical extends QuizQuestion implements Serializable {

    public static final String TAG = Numerical.class.getSimpleName();
    private static final long serialVersionUID = 808485823168202643L;

    @Override
    public void mark(String lang) {
        Float userAnswer = null;
        this.userscore = 0;
        for (String a : this.userResponses) {
            try {
                userAnswer = Float.parseFloat(a);
            } catch (NumberFormatException|NullPointerException e) {
                Log.d(TAG, "Response given is not recognised as a number", e);
            }
        }
        float score = calculateScoreWithTolerance(lang, userAnswer);

        if (score == 0){
            for (Response r : responseOptions){
                if (r.getTitle(lang).equalsIgnoreCase("*") && r.getProp(Quiz.JSON_PROPERTY_FEEDBACK) != null && !(r.getProp(Quiz.JSON_PROPERTY_FEEDBACK).equals(""))){
                    this.feedback = r.getFeedback(lang);
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

    private float calculateScoreWithTolerance(String lang, Float userAnswer){
        float score = 0;
        if (userAnswer != null) {
            float currMax = 0;
            // loop through the valid answers and check against these
            for (Response r : responseOptions) {
                try {
                    Float respNumber = Float.parseFloat(r.getTitle(lang));
                    Float tolerance = r.getTolerance();

                    if ((respNumber - tolerance <= userAnswer) && (userAnswer <= respNumber + tolerance) && (r.getScore() > currMax)) {
                        score = r.getScore();
                        currMax = r.getScore();
                        if(r.getFeedback(lang) != null && !(r.getFeedback(lang).equals(""))) {
                            this.feedback = r.getFeedback(lang);
                        }
                    }
                } catch (NumberFormatException nfe) {
                    Log.d(TAG, "Response option is not recognised as a number", nfe);
                }
            }
        }
        return score;
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
}