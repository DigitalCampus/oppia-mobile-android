package org.digitalcampus.oppia.model;

import org.digitalcampus.oppia.application.MobileLearning;
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
		return MobileLearning.DATETIME_FORMAT.print(startTime);
	}
	
	public void setStartTime(DateTime startTime) {
		this.startTime = startTime;
	}
	
	public DateTime getEndTime() {
		return endTime;
	}
	
	public String getEndTimeString () {
		return MobileLearning.DATETIME_FORMAT.print(endTime);
	}
	
	public void setEndTime(DateTime endTime) {
		this.endTime = endTime;
	}
}
