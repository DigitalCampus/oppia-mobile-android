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

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

public interface QuizQuestion extends Serializable {

    void addResponseOption(Response r);
    List<Response> getResponseOptions();
    void setUserResponses(List<String> str);
    List<String> getUserResponses();
    void setResponseOptions(List<Response> responses);
    void mark(String lang);
    int getID();
    void setID(int id);
    String getTitle(String lang);
    void setTitleForLang(String lang, String title);
    float getUserscore();
    void setProps(HashMap<String, String> props);
    String getProp(String key);
    String getFeedback(String lang);
    int getMaxScore();
    JSONObject responsesToJSON();
    boolean responseExpected();
    int getScoreAsPercent();
    void setFeedbackDisplayed(boolean feedbackDisplayed);
    boolean getFeedbackDisplayed();
}