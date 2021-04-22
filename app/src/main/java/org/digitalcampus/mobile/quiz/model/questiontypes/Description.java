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
import org.digitalcampus.oppia.analytics.Analytics;
import org.json.JSONException;
import org.json.JSONObject;

public class Description extends QuizQuestion {

    private static final long serialVersionUID = 809312927290284785L;
    public static final String TAG = Description.class.getSimpleName();

    @Override
    public JSONObject responsesToJSON() {
        JSONObject jo = new JSONObject();
        try {
            jo.put(Quiz.JSON_PROPERTY_QUESTION_ID, super.id);
            jo.put(Quiz.JSON_PROPERTY_SCORE,0);
            jo.put(Quiz.JSON_PROPERTY_TEXT, null);
        } catch (JSONException jsone) {
            Log.d(TAG,"Error creating json object", jsone);
            Analytics.logException(jsone);
        }
        return jo;
    }

    @Override
    public boolean responseExpected(){
        return false;
    }

}