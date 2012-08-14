package org.digitalcampus.mtrain.task;

public class Payload {
	
	public int taskType;
	public Object[] data;
	public boolean result = false;
	public String resultResponse;
	public Exception exception;

	public Payload(int taskType, Object[] data) {
		this.taskType = taskType;
		this.data = data;
	}
}
