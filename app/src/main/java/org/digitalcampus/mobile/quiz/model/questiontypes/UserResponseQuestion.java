package org.digitalcampus.mobile.quiz.model.questiontypes;

import android.util.Log;

import com.splunk.mint.Mint;

import org.digitalcampus.mobile.quiz.Quiz;
import org.digitalcampus.mobile.quiz.model.QuizQuestion;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public abstract class UserResponseQuestion extends QuizQuestion implements Serializable {

    @Override
    public JSONObject responsesToJSON() {
        JSONObject jo = new JSONObject();
        if(userResponses.isEmpty()){
            try {
                jo.put(Quiz.JSON_PROPERTY_QUESTION_ID, this.id);
                jo.put(Quiz.JSON_PROPERTY_SCORE, userscore);
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
}
