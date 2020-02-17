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

import com.splunk.mint.Mint;

import org.digitalcampus.mobile.quiz.Quiz;
import org.digitalcampus.mobile.quiz.model.QuizQuestion;
import org.digitalcampus.mobile.quiz.model.Response;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class DragAndDrop extends QuizQuestion implements Serializable {

    private static final long serialVersionUID = 8250801428572869330L;
    public static final String TAG = DragAndDrop.class.getSimpleName();

    public static final String TYPE_DROPZONE = "dropzone";
    public static final String TYPE_DRAGGABLE = "drag";

    @Override
    public void mark(String lang){

        int dropzones = 0;
        for (Response r : responseOptions){
            if (TYPE_DROPZONE.equals(r.getProp("type"))){
                dropzones++;
            }
        }
        // if not all the drops where filled, mark as failed
        if (userResponses.size() < dropzones) {
            userscore = 0;
            return;
        }

        if (!determineIfPassed()){
            userscore = 0;
        }
        else if(this.getProp(Quiz.JSON_PROPERTY_MAXSCORE) != null){
            userscore = Integer.parseInt(this.getProp(Quiz.JSON_PROPERTY_MAXSCORE));
        }
        else{
            userscore = 1;
        }
    }

    // loop through the responses
    // find whichever are set as selected and add up the responses
    private boolean determineIfPassed(){
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
        return passed;
    }

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
                Mint.logException(jsone);
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
                Mint.logException(jsone);
            }
        }
        return jo;
    }

    @Override
    public boolean responseExpected() {
        if (this.getProps().containsKey(Quiz.JSON_PROPERTY_REQUIRED)){
            return Boolean.parseBoolean(this.getProp(Quiz.JSON_PROPERTY_REQUIRED));
        }
        return true;
    }
}
