package org.digitalcampus.mobile.learning.model;

import org.digitalcampus.mobile.learning.application.MobileLearning;
import org.joda.time.DateTime;

public class ActivitySchedule {
	
	private String digest;
	private DateTime startTime;
	private DateTime endTime;
	
	public String getDigest() {
		return digest;
	}
	
	public void setDigest(String digest) {
		this.digest = digest;
	}
	
	public DateTime getStartTime() {
		return startTime;
	}
	
	public String getStartTimeString() {
		return MobileLearning.DATE_FORMAT.print(startTime);
	}
	
	public void setStartTime(DateTime startTime) {
		this.startTime = startTime;
	}
	
	public DateTime getEndTime() {
		return endTime;
	}
	
	public String getEndTimeString () {
		return MobileLearning.DATE_FORMAT.print(endTime);
	}
	
	public void setEndTime(DateTime endTime) {
		this.endTime = endTime;
	}
}
