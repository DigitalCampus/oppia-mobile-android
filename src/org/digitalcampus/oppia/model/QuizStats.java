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
