package org.digitalcampus.mobile.learning.task;

import java.util.ArrayList;

public class Payload {
	
	public int taskType;
	public ArrayList<Object> data;
	public boolean result = false;
	public String resultResponse;
	public ArrayList<Object> responseData = new ArrayList<Object>();
	public Exception exception;

	public Payload(int taskType, ArrayList<Object> data) {
		this.taskType = taskType;
		this.data = data;
	}
}
