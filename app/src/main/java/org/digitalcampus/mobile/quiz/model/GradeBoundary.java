package org.digitalcampus.mobile.quiz.model;

import java.util.Comparator;

public class GradeBoundary {
    private int grade;
    private String message;

    public GradeBoundary(int grade, String message) {
        this.grade = grade;
        this.message = message;
    }

    public int getGrade() {
        return grade;
    }

    public String getMessage() {
        return message;
    }

    public static Comparator<GradeBoundary> sorByGradeDescending = new Comparator<GradeBoundary>() {
        @Override
        public int compare(GradeBoundary gradeBoundary1, GradeBoundary gradeBoundary2) {
            return gradeBoundary2.grade - gradeBoundary1.grade;
        }
    };
}
