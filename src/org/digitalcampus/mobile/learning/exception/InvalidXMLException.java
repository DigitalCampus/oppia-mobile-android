package org.digitalcampus.mobile.learning.exception;

import com.bugsense.trace.BugSenseHandler;

public class InvalidXMLException extends Exception {

	public static final String TAG = InvalidXMLException.class.getSimpleName();
	private static final long serialVersionUID = -2986632352088699106L;
	
	public InvalidXMLException(Exception e){
		BugSenseHandler.sendException(e);
		e.printStackTrace();
	}

}
