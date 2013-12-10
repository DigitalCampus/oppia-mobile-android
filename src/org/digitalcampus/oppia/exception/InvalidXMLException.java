package org.digitalcampus.oppia.exception;

import org.digitalcampus.oppia.application.MobileLearning;

import com.bugsense.trace.BugSenseHandler;

public class InvalidXMLException extends Exception {

	public static final String TAG = InvalidXMLException.class.getSimpleName();
	private static final long serialVersionUID = -2986632352088699106L;
	
	public InvalidXMLException(Exception e){
		if(!MobileLearning.DEVELOPER_MODE){
			BugSenseHandler.sendException(e);
		} else {
			e.printStackTrace();
		}
	}

}
