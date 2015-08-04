package org.digitalcampus.oppia.model;


public class QuizStats {

    public boolean attempted;
    private int maxScore;
    private int userScore;
    private int passThreshold;

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
        return (userScore * 100) / maxScore ;
    }

    public boolean isPassed(){
        return (attempted && (getPercent() >= passThreshold));
    }
}
