package org.digitalcampus.mobile.learning.model;

public class DownloadProgress {

	private String message = "";
	private int progress = 0;
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public int getProgress() {
		return progress;
	}
	public void setProgress(int progress) {
		this.progress = progress;
	}
}
