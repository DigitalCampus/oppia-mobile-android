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

package org.digitalcampus.mobile.quiz.model;

import org.digitalcampus.mobile.quiz.Quiz;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Response implements Serializable {

    private static final long serialVersionUID = 5970350772982572264L;
    public static final String TAG = Response.class.getSimpleName();
    private Map<String, String> title = new HashMap<>();
    private float score;
    private Map<String, String> props = new HashMap<>();
    private Map<String, String> feedback = new HashMap<>();

    public String getTitle(String lang) {
        if (title.containsKey(lang)) {
            return title.get(lang);
        } else if (!title.entrySet().isEmpty()) {
            return title.entrySet().iterator().next().getValue();
        } else {
            return "";
        }
    }

    public void setTitleForLang(String lang, String title) {
        this.title.put(lang, title);
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public void setProps(Map<String, String> props) {
        this.props = props;
    }

    public String getProp(String key) {
        return props.get(key);
    }

    public void setFeedback(String defaultLang) {
        if (this.props.containsKey(Quiz.JSON_PROPERTY_FEEDBACK)) try {
            JSONObject feedbackLangs = new JSONObject(this.getProp(Quiz.JSON_PROPERTY_FEEDBACK));
            Iterator<?> keys = feedbackLangs.keys();

            while (keys.hasNext()) {
                String key = (String) keys.next();
                this.setFeedbackForLang(key, feedbackLangs.getString(key));
            }
        } catch (JSONException e) {
            this.setFeedbackForLang(defaultLang, this.getProp(Quiz.JSON_PROPERTY_FEEDBACK));
        }
    }

    private void setFeedbackForLang(String lang, String title) {
        this.feedback.put(lang, title);
    }

    public String getFeedback(String lang) {
        if (feedback.containsKey(lang)) {
            return feedback.get(lang);
        } else if (!feedback.entrySet().isEmpty()) {
            return feedback.entrySet().iterator().next().getValue();
        } else {
            return "";
        }
    }

    public Float getTolerance(){
        Float tolerance = (float) 0.0;
        if(this.getProp(Quiz.JSON_PROPERTY_TOLERANCE) != null){
            tolerance = Float.parseFloat(this.getProp(Quiz.JSON_PROPERTY_TOLERANCE));
        }
        return tolerance;
    }
}