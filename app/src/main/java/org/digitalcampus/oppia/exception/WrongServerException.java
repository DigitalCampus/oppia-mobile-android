package org.digitalcampus.oppia.exception;

public class WrongServerException extends Exception{
    public WrongServerException(String message){ super(message); }
    public WrongServerException(String message, Throwable cause) {
        super(message, cause);
    }
    public WrongServerException(Throwable cause) {
        super(cause);
    }
}