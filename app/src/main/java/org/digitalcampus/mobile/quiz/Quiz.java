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

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.digitalcampus.mobile.quiz.model.QuizQuestion;
import org.digitalcampus.mobile.quiz.model.Response;
import org.digitalcampus.mobile.quiz.model.questiontypes.Description;
import org.digitalcampus.mobile.quiz.model.questiontypes.DragAndDrop;
import org.digitalcampus.mobile.quiz.model.questiontypes.Essay;
import org.digitalcampus.mobile.quiz.model.questiontypes.Matching;
import org.digitalcampus.mobile.quiz.model.questiontypes.MultiChoice;
import org.digitalcampus.mobile.quiz.model.questiontypes.MultiSelect;
import org.digitalcampus.mobile.quiz.model.questiontypes.Numerical;
import org.digitalcampus.mobile.quiz.model.questiontypes.ShortAnswer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Quiz implements Serializable {

    public static final String TAG = "Quiz";

    public static final String RESPONSE_SEPARATOR = "||";
    public static final String MATCHING_SEPARATOR = "|";
    public static final String MATCHING_REGEX = "\\|";

    public static final int AVAILABILITY_ALWAYS = 0;
    public static final int AVAILABILITY_SECTION = 1;
    public static final int AVAILABILITY_COURSE = 2;

    public static final int SHOW_FEEDBACK_ALWAYS = 1;
    public static final int SHOW_FEEDBACK_NEVER = 0;
    public static final int SHOW_FEEDBACK_ATEND = 2;

    public static final int QUIZ_DEFAULT_PASS_THRESHOLD = 99; // use 99 rather than 100 in case of rounding
    public static final int QUIZ_QUESTION_PASS_THRESHOLD = 99; // use 99 rather than 100 in case of rounding

    private static final long serialVersionUID = -2416034891439585524L;
    private int id;
    private HashMap<String,String> title = new HashMap<String,String>();
    private String url;
    private float maxscore;
    private boolean checked;
    private int currentq = 0;
    private int maxattempts;
    private float userscore;
    private List<QuizQuestion> questions = new ArrayList<QuizQuestion>();
    private String instanceID;
    private String propsSerialized;
    private String defaultLang;

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
            this.id = json.getInt("id");

            if (json.get("title") instanceof JSONObject){
                JSONObject titleLangs = json.getJSONObject("title");
                Iterator<?> keys = titleLangs.keys();

                while( keys.hasNext() ){
                    String key = (String) keys.next();
                    this.setTitleForLang(key, titleLangs.getString(key));
                }
            } else if (json.get("title") instanceof String) {
                this.setTitleForLang(this.defaultLang, json.getString("title"));
            } else {
                //fallback
                this.setTitleForLang(this.defaultLang, "unknown");
            }
            this.propsSerialized = json.get("props").toString();
            this.maxscore = propsSerializedGetInt("maxscore",0);
            this.maxattempts = propsSerializedGetInt("maxattempts", -1);
            int randomSelect = propsSerializedGetInt("randomselect",0);

            // add questions
            JSONArray questions = (JSONArray) json.get("questions");
            if (randomSelect > 0){
                this.generateQuestionSet(questions, randomSelect);
            } else {
                for (int i = 0; i < questions.length(); i++) {
                    this.addQuestion((JSONObject) questions.get(i));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void generateQuestionSet(JSONArray questionChoices, int randomSelect){
        Random generator = new Random();
        while(this.questions.size() < randomSelect){
            int randomNum = generator.nextInt(questionChoices.length());
            boolean found = false;
            JSONObject quizquestion;
            try {
                quizquestion = (JSONObject) questionChoices.get(randomNum);
                JSONObject q = quizquestion.getJSONObject("question");
                int qid = q.getInt("id");
                for(int i=0; i < this.questions.size(); i++){
                    if (qid == this.questions.get(i).getID()){
                        found = true;
                    }
                }
                if(!found){
                    this.addQuestion(quizquestion);
                }
            } catch (JSONException e) {

            }
        }

        // now set the new maxscore
        float newMax = 0;
        for(int i=0; i<this.questions.size(); i++){
            newMax += this.questions.get(i).getMaxScore();
        }
        this.maxscore = newMax;
    }

    private boolean addQuestion(JSONObject qObj) {

        // determine question type
        QuizQuestion question;
        String qtype;
        try {
            JSONObject q = qObj.getJSONObject("question");
            qtype = (String) q.get("type");
            if (qtype.toLowerCase().equals(Essay.TAG.toLowerCase())) {
                question = new Essay();
            } else if (qtype.toLowerCase().equals(MultiChoice.TAG.toLowerCase())) {
                question = new MultiChoice();
            } else if (qtype.toLowerCase().equals(Numerical.TAG.toLowerCase())) {
                question = new Numerical();
            } else if (qtype.toLowerCase().equals(Matching.TAG.toLowerCase())) {
                question = new Matching();
            } else if (qtype.toLowerCase().equals(ShortAnswer.TAG.toLowerCase())) {
                question = new ShortAnswer();
            } else if (qtype.toLowerCase().equals(MultiSelect.TAG.toLowerCase())) {
                question = new MultiSelect();
            } else if (qtype.toLowerCase().equals(Description.TAG.toLowerCase())) {
                question = new Description();
            } else if (qtype.toLowerCase().equals(DragAndDrop.TAG.toLowerCase())) {
            question = new DragAndDrop();
            } else {
                Log.d(TAG, "Question type " + qtype + " is not yet supported");
                return false;
            }

            question.setID(q.getInt("id"));

            if (q.get("title") instanceof JSONObject){
                JSONObject titleLangs = q.getJSONObject("title");
                Iterator<?> keys = titleLangs.keys();

                while( keys.hasNext() ){
                    String key = (String) keys.next();
                    question.setTitleForLang(key, titleLangs.getString(key));
                }
            } else if (q.get("title") instanceof Integer){
                question.setTitleForLang(this.defaultLang, String.valueOf(q.getInt("title")));
            } else {
                question.setTitleForLang(this.defaultLang, q.getString("title"));
            }

            JSONObject questionProps = (JSONObject) q.get("props");

            HashMap<String, String> qProps = new HashMap<String, String>();
            if (questionProps.names() != null){
                for (int k = 0; k < questionProps.names().length(); k++) {
                    qProps.put(questionProps.names().getString(k),
                            questionProps.getString(questionProps.names().getString(k)));
                }
                question.setProps(qProps);
            }
            this.questions.add(question);

            // now add response options for this question
            JSONArray responses = (JSONArray) q.get("responses");
            for (int j = 0; j < responses.length(); j++) {
                JSONObject r = (JSONObject) responses.get(j);
                Response responseOption = new Response();

                if (r.get("title") instanceof JSONObject){
                    JSONObject titleLangs = r.getJSONObject("title");
                    Iterator<?> keys = titleLangs.keys();

                    while( keys.hasNext() ){
                        String key = (String) keys.next();
                        if (titleLangs.get(key) instanceof Integer){
                            responseOption.setTitleForLang(key, String.valueOf(titleLangs.getInt(key)));
                        } else {
                            responseOption.setTitleForLang(key, titleLangs.getString(key));
                        }
                    }
                } else if (r.get("title") instanceof Integer){
                    responseOption.setTitleForLang(this.defaultLang, String.valueOf(r.getInt("title")));
                } else {
                    responseOption.setTitleForLang(this.defaultLang, r.getString("title"));
                }

                responseOption.setScore(Float.parseFloat((String) r.get("score")));
                JSONObject responseProps = (JSONObject) r.get("props");
                HashMap<String, String> rProps = new HashMap<String, String>();
                if (responseProps.names() != null) {
                    for (int m = 0; m < responseProps.names().length(); m++) {
                        rProps.put(responseProps.names().getString(m),
                                responseProps.getString(responseProps.names().getString(m)));
                    }
                }
                responseOption.setProps(rProps);
                responseOption.setFeedback(this.defaultLang);
                question.addResponseOption(responseOption);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        return true;
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

    public void setTitleForLang(String lang, String title) {
        this.title.put(lang, title);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
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

    public JSONObject getResultObject() {
        JSONObject json = new JSONObject();
        try {
            json.put("quiz_id", this.getID());
            Date now = new Date();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            json.put("attempt_date", simpleDateFormat.format(now));
            json.put("score", this.getUserscore());
            json.put("maxscore", this.getMaxscore());
            json.put("instance_id",this.getInstanceID());
            JSONArray responses = new JSONArray();
            for(QuizQuestion q: questions){
                if(!(q instanceof Description)){
                    JSONObject r = q.responsesToJSON();
                    responses.put(r);
                }
            }
            json.put("responses", responses);
        } catch (JSONException e) {
            e.printStackTrace();
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

    public boolean isAllowTryAgain(){
        return propsSerializedGetBoolean("allowtryagain",true);
    }

    private int propsSerializedGetInt(String key, int defaultValue){
        try {
            JSONObject json = new JSONObject(propsSerialized);
            return json.getInt(key);
        } catch (JSONException e) {
            // do nothing
        }
        return defaultValue;
    }

    private boolean propsSerializedGetBoolean(String key, boolean defaultValue){
        try {
            JSONObject json = new JSONObject(propsSerialized);
            return json.getBoolean(key);
        } catch (JSONException e) {
            // do nothing
        }
        return defaultValue;
    }

    public  int getMaxAttempts() { return maxattempts; }
    public boolean limitAttempts(){ return maxattempts > 0; }
    public void setMaxAttempts(int maxAttempts) { this.maxattempts = maxAttempts; }
}