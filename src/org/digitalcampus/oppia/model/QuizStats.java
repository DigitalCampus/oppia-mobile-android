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

public class QuizStats {

    public static final String fromQuizResultRegex = "\\{\\s*\"score\"\\s*:\\s*(.+?)\\s*,\\s*\"maxscore\":\\s*(.+?)\\s*,\\s*\"quiz_id\"\\s*:\\s*(.+?)\\s*,\\s*(.+?)*\\}";
    public static final String fromCourseXMLRegex = "\\{.+?\\s*\"id\"\\s*:\\s*(.+?)\\s*,[A-z\"{0-9\\\\\\/,:\\_\\-]+?,\"passthreshold\":\"(.+?)\"\\s*.+?\\}";

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
}
