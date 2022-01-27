package org.digitalcampus.oppia.task.result;

public class BasicResult {

    private boolean result;
    private String resultMessage;

    public static BasicResult SUCCESS = new BasicResult(true);
    public static BasicResult ERROR = new BasicResult(false);

    public static BasicResult withSuccessMessage(String message) {
        return new BasicResult(true, message);
    }

    public static BasicResult withErrorMessage(String message) {
        return new BasicResult(false, message);
    }

    public BasicResult() {
    }

    public BasicResult(boolean success) {
        this.result = success;
    }

    public BasicResult(boolean success, String resultMessage) {
        this.result = success;
        this.resultMessage = resultMessage;
    }

    public boolean isSuccess() {
        return result;
    }

    public void setSuccess(boolean success) {
        this.result = success;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }
}
