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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class MultiChoice extends QuizQuestion implements Serializable  {

    private static final long serialVersionUID = -6605393327170759582L;
    public static final String TAG = MultiChoice.class.getSimpleName();

    @Override
    public String getFeedback(String lang) {
        // reset feedback back to nothing
        this.feedback = "";
        this.mark(lang);
        return this.feedback;
    }

    @Override
    public JSONObject responsesToJSON() {
        JSONObject jo = new JSONObject();
        if(userResponses.isEmpty()){
            try {
                jo.put(Quiz.JSON_PROPERTY_QUESTION_ID, this.id);
                jo.put(Quiz.JSON_PROPERTY_SCORE,userscore);
                jo.put(Quiz.JSON_PROPERTY_TEXT, "");
            } catch (JSONException jsone) {
                Log.d(TAG,"Error creating json object", jsone);
            }
            return jo;
        }

        for(String ur: userResponses ){
            try {
                jo.put(Quiz.JSON_PROPERTY_QUESTION_ID, this.id);
                jo.put(Quiz.JSON_PROPERTY_SCORE,userscore);
                jo.put(Quiz.JSON_PROPERTY_TEXT, ur);
            } catch (JSONException jsone) {
                Log.d(TAG,"Error creating json object", jsone);
            }
        }
        return jo;
    }
}
