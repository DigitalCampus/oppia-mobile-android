/* 
 * This file is part of OppiaMobile - http://oppia-mobile.org/
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

package org.digitalcampus.mobile.quiz;

import android.util.Log;

import com.splunk.mint.Mint;

import org.digitalcampus.mobile.quiz.model.QuizQuestion;
import org.digitalcampus.mobile.quiz.model.Response;
import org.digitalcampus.mobile.quiz.model.questiontypes.Description;
import org.digitalcampus.mobile.quiz.model.questiontypes.Essay;
import org.digitalcampus.mobile.quiz.model.questiontypes.Matching;
import org.digitalcampus.mobile.quiz.model.questiontypes.MultiChoice;
import org.digitalcampus.mobile.quiz.model.questiontypes.MultiSelect;
import org.digitalcampus.mobile.quiz.model.questiontypes.Numerical;
import org.digitalcampus.mobile.quiz.model.questiontypes.ShortAnswer;
import org.digitalcampus.oppia.model.GamificationEvent;
import org.digitalcampus.oppia.utils.DateUtils;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class Quiz implements Serializable {

    public static final String TAG = Quiz.class.getSimpleName();

    public static final String RESPONSE_SEPARATOR = "||";
    public static final String MATCHING_SEPARATOR = "|";
    public static final String MATCHING_REGEX = "\\|";

    public static final int AVAILABILITY_ALWAYS = 0;
    public static final int AVAILABILITY_SECTION = 1;
    public static final int AVAILABILITY_COURSE = 2;

    public static final int SHOW_FEEDBACK_ALWAYS = 1;
    public static final int SHOW_FEEDBACK_NEVER = 0;
    public static final int SHOW_FEEDBACK_AT_END = 2;

    public static final int QUIZ_DEFAULT_PASS_THRESHOLD = 99; // use 99 rather than 100 in case of rounding
    public static final int QUIZ_QUESTION_PASS_THRESHOLD = 99; // use 99 rather than 100 in case of rounding

    public static final String JSON_PROPERTY_TITLE = "title";
    public static final String JSON_PROPERTY_ID = "id";
    public static final String JSON_PROPERTY_PROPS = "props";
    public static final String JSON_PROPERTY_QUESTIONS = "questions";
    public static final String JSON_PROPERTY_QUESTION = "question";
    public static final String JSON_PROPERTY_RESPONSE = "response";
    public static final String JSON_PROPERTY_RESPONSES = "responses";
    public static final String JSON_PROPERTY_TYPE = "type";
    public static final String JSON_PROPERTY_QUESTION_ID = "question_id";
    public static final String JSON_PROPERTY_SCORE = "score";
    public static final String JSON_PROPERTY_MAXSCORE = "maxscore";
    public static final String JSON_PROPERTY_TEXT = "text";
    public static final String JSON_PROPERTY_TOLERANCE = "tolerance";
    public static final String JSON_PROPERTY_FEEDBACK = "feedback";
    public static final String JSON_PROPERTY_CORRECTFEEDBACK = "correctfeedback";
    public static final String JSON_PROPERTY_INCORRECTFEEDBACK = "incorrectfeedback";
    public static final String JSON_PROPERTY_PARTIALLYCORRECTFEEDBACK = "partiallycorrectfeedback";
    public static final String JSON_PROPERTY_REQUIRED = "required";
    public static final String JSON_PROPERTY_MAXATTEMPTS = "maxattempts";
    public static final String JSON_PROPERTY_RANDOMSELECT = "randomselect";

    private static final long serialVersionUID = -2416034891439585524L;
    private int id;
    private HashMap<String,String> title = new HashMap<>();
    private String url;
    private float maxscore;
    private int currentq = 0;
    private int maxattempts;
    private float userscore;
    private List<QuizQuestion> questions = new ArrayList<>();
    private String instanceID;
    private String propsSerialized;
    private String defaultLang;

    Random generator = new Random();

    public Quiz() {
        this.setInstanceID();
    }

    public String getInstanceID() {
        return instanceID;
    }

    public void setInstanceID() {
        UUID guid = java.util.UUID.randomUUID();
        this.instanceID = guid.toString();
    }

    public boolean load(String quiz, String defaultLang) {
        this.defaultLang = defaultLang;
        try {
            JSONObject json = new JSONObject(quiz);
            this.setID(json.getInt(JSON_PROPERTY_ID));

            if (json.get(JSON_PROPERTY_TITLE) instanceof JSONObject){
                JSONObject titleLangs = json.getJSONObject(JSON_PROPERTY_TITLE);
                Iterator<?> keys = titleLangs.keys();

                while( keys.hasNext() ){
                    String key = (String) keys.next();
                    this.setTitleForLang(key, titleLangs.getString(key));
                }
            } else if (json.get(JSON_PROPERTY_TITLE) instanceof String) {
                this.setTitleForLang(this.defaultLang, json.getString(JSON_PROPERTY_TITLE));
            } else {
                //fallback
                this.setTitleForLang(this.defaultLang, "unknown");
            }
            this.propsSerialized = json.get(JSON_PROPERTY_PROPS).toString();
            this.maxscore = propsSerializedGetInt(JSON_PROPERTY_MAXSCORE,0);
            this.setMaxAttempts(propsSerializedGetInt(JSON_PROPERTY_MAXATTEMPTS, -1));
            int randomSelect = propsSerializedGetInt(JSON_PROPERTY_RANDOMSELECT,0);

            // add questions
            JSONArray questionsJSON = (JSONArray) json.get(JSON_PROPERTY_QUESTIONS);
            if (randomSelect > 0){
                this.generateQuestionSet(questionsJSON, randomSelect);
            } else {
                for (int i = 0; i < questionsJSON.length(); i++) {
                    this.addQuestion((JSONObject) questionsJSON.get(i));
                }
            }
        } catch (JSONException jsone) {
            Log.d(TAG,"Error loading quiz",jsone);
            Mint.logException(jsone);
            return false;
        }
        return true;
    }

    private void generateQuestionSet(JSONArray questionChoices, int randomSelect) throws JSONException {
        while(this.questions.size() < randomSelect){
            int randomNum = generator.nextInt(questionChoices.length());
            boolean found = false;
            JSONObject quizquestion;

            quizquestion = (JSONObject) questionChoices.get(randomNum);
            JSONObject q = quizquestion.getJSONObject(JSON_PROPERTY_QUESTION);
            int qid = q.getInt(JSON_PROPERTY_ID);
            for(int i=0; i < this.questions.size(); i++){
                if (qid == this.questions.get(i).getID()){
                    found = true;
                }
            }
            if(!found) {
                this.addQuestion(quizquestion);
            }
        }

        // now set the new maxscore
        float newMax = 0;
        for(int i=0; i<this.questions.size(); i++){
            newMax += this.questions.get(i).getMaxScore();
        }
        this.maxscore = newMax;
    }

    private void addQuestion(JSONObject qObj) throws JSONException{

        // determine question type
        QuizQuestion question;
        String qtype;
        JSONObject q = qObj.getJSONObject(JSON_PROPERTY_QUESTION);
        qtype = (String) q.get(JSON_PROPERTY_TYPE);

        try {
            question = this.setQuestionType(qtype);
        } catch (UnsupportedQuestionType uqt){
            return;
        }
        question.setID(q.getInt(JSON_PROPERTY_ID));

        if (q.get(JSON_PROPERTY_TITLE) instanceof JSONObject){
            JSONObject titleLangs = q.getJSONObject(JSON_PROPERTY_TITLE);
            Iterator<?> keys = titleLangs.keys();

            while( keys.hasNext() ){
                String key = (String) keys.next();
                question.setTitleForLang(key, titleLangs.getString(key));
            }
        } else if (q.get(JSON_PROPERTY_TITLE) instanceof Integer){
            question.setTitleForLang(this.defaultLang, String.valueOf(q.getInt(JSON_PROPERTY_TITLE)));
        } else {
            question.setTitleForLang(this.defaultLang, q.getString(JSON_PROPERTY_TITLE));
        }

        JSONObject questionProps = (JSONObject) q.get(JSON_PROPERTY_PROPS);

        HashMap<String, String> qProps = new HashMap<>();
        if (questionProps.names() != null){
            for (int k = 0; k < questionProps.names().length(); k++) {
                qProps.put(questionProps.names().getString(k),
                        questionProps.getString(questionProps.names().getString(k)));
            }
            question.setProps(qProps);
        }
        this.questions.add(question);

        // now add response options for this question
        JSONArray responses = (JSONArray) q.get(JSON_PROPERTY_RESPONSES);
        for (int j = 0; j < responses.length(); j++) {
            JSONObject r = (JSONObject) responses.get(j);
            Response responseOption = this.setResponseOptions(r);
            question.addResponseOption(responseOption);
        }
    }

    private QuizQuestion setQuestionType(String qtype) throws UnsupportedQuestionType{
        QuizQuestion question;
        if (qtype.equalsIgnoreCase(Essay.TAG)) {
            question = new Essay();
        } else if (qtype.equalsIgnoreCase(MultiChoice.TAG)) {
            question = new MultiChoice();
        } else if (qtype.equalsIgnoreCase(Numerical.TAG)) {
            question = new Numerical();
        } else if (qtype.equalsIgnoreCase(Matching.TAG)) {
            question = new Matching();
        } else if (qtype.equalsIgnoreCase(ShortAnswer.TAG)) {
            question = new ShortAnswer();
        } else if (qtype.equalsIgnoreCase(MultiSelect.TAG)) {
            question = new MultiSelect();
        } else if (qtype.equalsIgnoreCase(Description.TAG)) {
            question = new Description();
        } else {
            Log.d(TAG, "Question type " + qtype + " is not yet supported");
            throw new UnsupportedQuestionType();
        }
        return question;
    }
    private Response setResponseOptions(JSONObject r) throws JSONException{
        Response responseOption = new Response();

        if (r.get(JSON_PROPERTY_TITLE) instanceof JSONObject){
            JSONObject titleLangs = r.getJSONObject(JSON_PROPERTY_TITLE);
            Iterator<?> keys = titleLangs.keys();

            while( keys.hasNext() ){
                String key = (String) keys.next();
                if (titleLangs.get(key) instanceof Integer){
                    responseOption.setTitleForLang(key, String.valueOf(titleLangs.getInt(key)));
                } else {
                    responseOption.setTitleForLang(key, titleLangs.getString(key));
                }
            }
        } else if (r.get(JSON_PROPERTY_TITLE) instanceof Integer){
            responseOption.setTitleForLang(this.defaultLang, String.valueOf(r.getInt(JSON_PROPERTY_TITLE)));
        } else {
            responseOption.setTitleForLang(this.defaultLang, r.getString(JSON_PROPERTY_TITLE));
        }

        responseOption.setScore(Float.parseFloat((String) r.get(Quiz.JSON_PROPERTY_SCORE)));
        JSONObject responseProps = (JSONObject) r.get(JSON_PROPERTY_PROPS);
        HashMap<String, String> rProps = new HashMap<>();
        if (responseProps.names() != null) {
            for (int m = 0; m < responseProps.names().length(); m++) {
                rProps.put(responseProps.names().getString(m),
                        responseProps.getString(responseProps.names().getString(m)));
            }
        }
        responseOption.setProps(rProps);
        responseOption.setFeedback(this.defaultLang);
        return responseOption;
    }

    public boolean hasNext() {
        return this.currentq + 1 < questions.size();
    }

    public boolean hasPrevious() {
        return this.currentq > 0;
    }

    public void moveNext() {
        if (currentq + 1 < questions.size()) {
            currentq++;
        }
    }

    public void movePrevious() {
        if (currentq > 0) {
            currentq--;
        }
    }

    public void mark(String lang) {
        float total = 0;
        for (QuizQuestion q : questions) {
            q.mark(lang);
            total += q.getUserscore();
        }
        userscore = Math.min(total, maxscore);
    }

    public int getID() {
        return this.id;
    }

    private void setID(int id) {
        this.id = id;
    }

    public String getTitle(String lang) {
        if(title.containsKey(lang)){
            return title.get(lang);
        } else if (!title.entrySet().isEmpty()){
            return title.entrySet().iterator().next().getValue();
        } else {
            return "";
        }
    }

    public void setTitleForLang(String lang, String title) {
        this.title.put(lang, title);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getCurrentQuestionNo() {
        int retNo = 0;
        for(int i = 0; i < this.currentq + 1 ; i++){
            if (!(questions.get(i) instanceof Description)){
                retNo ++;
            }
        }
        return retNo;
    }

    public QuizQuestion getCurrentQuestion() throws InvalidQuizException {
        try {
            return questions.get(this.currentq);
        } catch (IndexOutOfBoundsException e ){
            Mint.logException(e);
            throw new InvalidQuizException(e);
        }
    }

    public float getUserscore() {
        return this.userscore;
    }

    public float getMaxscore() {
        return maxscore;
    }

    public void setMaxscore(float maxscore) {
        this.maxscore = maxscore;
    }

    public int getTotalNoQuestions() {
        int noQs = 0;
        for (QuizQuestion q: questions){
            if (! (q instanceof Description)){
                noQs++;
            }
        }
        return noQs;
    }

    public JSONObject getResultObject(GamificationEvent ge) {
        JSONObject json = new JSONObject();
        try {
            json.put("quiz_id", this.getID());
            DateTime now = new DateTime();
            json.put("attempt_date", DateUtils.DATETIME_FORMAT.print(now));
            json.put(JSON_PROPERTY_SCORE, this.getUserscore());
            json.put(JSON_PROPERTY_MAXSCORE, this.getMaxscore());
            json.put("instance_id",this.getInstanceID());
            json.put("points",ge.getPoints());
            json.put("event",ge.getEvent());
            JSONArray responses = new JSONArray();
            for(QuizQuestion q: questions){
                if(!(q instanceof Description)){
                    JSONObject r = q.responsesToJSON();
                    responses.put(r);
                }
            }
            json.put(JSON_PROPERTY_RESPONSES, responses);
        } catch (JSONException jsone) {
            Log.d(TAG,"Error creating json result object",jsone);
            Mint.logException(jsone);
        }
        return json;
    }

    public List<QuizQuestion> getQuestions(){
        return questions;
    }

    public int getPassThreshold(){
        return propsSerializedGetInt("passthreshold",0);
    }

    public int getShowFeedback(){
        return propsSerializedGetInt("showfeedback",SHOW_FEEDBACK_ALWAYS);
    }

    public int getAvailability(){
        return propsSerializedGetInt("availability",AVAILABILITY_ALWAYS);
    }

    private int propsSerializedGetInt(String key, int defaultValue){
        try {
            JSONObject json = new JSONObject(propsSerialized);
            return json.getInt(key);
        } catch (JSONException jsone) {
            Log.d(TAG, "Error getting int from propsSerialized", jsone);
        }
        return defaultValue;
    }

    public int getMaxAttempts() { return maxattempts; }
    public boolean limitAttempts(){ return maxattempts > 0; }
    private void setMaxAttempts(int maxAttempts) { this.maxattempts = maxAttempts; }
}