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

package org.digitalcampus.oppia.model;

import org.digitalcampus.mobile.quiz.Quiz;
import org.json.JSONException;
import org.json.JSONObject;

public class QuizStats {

    public static final String fromQuizResultRegex = "\\{\\s*\"score\"\\s*:\\s*(.+?)\\s*,\\s*\"maxscore\":\\s*(.+?)\\s*,\\s*\"quiz_id\"\\s*:\\s*(.+?)\\s*,\\s*(.+?)*\\}";
    public static final String fromCourseXMLRegex = "\\{.+?\\s*\"id\"\\s*:\\s*(.+?)\\s*,[A-z\"{0-9\\\\\\/,:\\_\\-]+?,\"passthreshold\":\"(.+?)\"\\s*.+?\\}";
    public static final String fromCourseXMLRegexSimple = "\"id\"\\s*:\\s*(.+?)\\s*,\"";

    public static final String JSONPROP_QUIZID = "quiz_id";
    public static final String JSONPROP_SCORE = "score";
    public static final String JSONPROP_MAXSCORE = "maxscore";

    public static final int COURSEXML_MATCHER_QUIZID = 1;
    public static final int COURSEXML_MATCHER_THRESHOLD = 2;

    private int quizId;
    public boolean attempted;
    private int maxScore;
    private int userScore;
    private int passThreshold = Quiz.QUIZ_DEFAULT_PASS_THRESHOLD;

    public QuizStats(int quizId){ this.quizId = quizId; }
    public QuizStats(){ }

    public int getQuizId() { return quizId; }
    public void setQuizId(int quizId) { this.quizId = quizId; }

    public int getMaxScore() {
        return maxScore;
    }
    public void setMaxScore(int maxScore) {
        this.maxScore = maxScore;
    }

    public int getUserScore() {
        return userScore;
    }
    public void setUserScore(int userScore) {
        this.userScore = userScore;
    }

    public int getPassThreshold() {
        return passThreshold;
    }
    public void setPassThreshold(int passThreshold) {
        this.passThreshold = passThreshold;
    }

    public boolean isAttempted(){
        return attempted;
    }
    public void setAttempted(boolean a){
        attempted = a;
    }

    public int getPercent(){
        return (userScore * 100) / Math.max(1,maxScore) ;
    }

    public boolean isPassed(){
        return (attempted && (getPercent() >= passThreshold));
    }

    //Class to parse the QuizResult JSON saved in the database
    public static class QuizStatsJsonParser {

        private String jsonString;
        private JSONObject quizJson;

        private double score;
        private int maxScore;
        private int quizID;

        public QuizStatsJsonParser(String jsonString) {
            this.jsonString = jsonString;
        }

        public double getScore() { return score; }
        public int getMaxScore() { return maxScore; }
        public int getQuizID() { return quizID; }

        public boolean parse(){
            try {
                quizJson = new JSONObject(jsonString);
                if (quizJson.has(JSONPROP_QUIZID)) quizID = quizJson.getInt(JSONPROP_QUIZID);
                if (quizJson.has(JSONPROP_SCORE)) score = quizJson.getDouble(JSONPROP_SCORE);
                if (quizJson.has(JSONPROP_MAXSCORE)) maxScore = quizJson.getInt(JSONPROP_MAXSCORE);

            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
    }

}
