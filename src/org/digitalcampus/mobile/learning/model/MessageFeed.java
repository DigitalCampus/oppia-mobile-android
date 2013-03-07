package org.digitalcampus.mobile.learning.model;

import java.util.ArrayList;

public class MessageFeed {

	private ArrayList<String> messages = new ArrayList<String>();
	private int currentMessage = 0;
	
	public MessageFeed(){
		
	}
	
	public int count(){
		return messages.size();
	}
	
	public void addMessage(String m){
		messages.add(m);
	}
	
	public String getNextMessage() {
		String ret = "";
		if (messages.size() <= 0) {
			return ret;
		}
		ret = messages.get(currentMessage);
		currentMessage++;
		if (currentMessage >= messages.size()) {
			currentMessage = 0;
		}

		return ret;
	}
}
