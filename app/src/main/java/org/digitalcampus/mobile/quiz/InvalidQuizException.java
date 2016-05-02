package org.digitalcampus.mobile.quiz;


public class InvalidQuizException extends Exception{
    public static final String TAG = InvalidQuizException.class.getSimpleName();
    private static final long serialVersionUID = -2986632352088699106L;

    public InvalidQuizException(Exception e){
        e.printStackTrace();
    }
}