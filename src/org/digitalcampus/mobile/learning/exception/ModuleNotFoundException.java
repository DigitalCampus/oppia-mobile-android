package org.digitalcampus.mobile.learning.exception;

import org.digitalcampus.mobile.learning.application.DbHelper;

import android.app.Activity;
import android.util.Log;

public class ModuleNotFoundException extends Exception {

	public static final String TAG = ModuleNotFoundException.class.getSimpleName();
	private static final long serialVersionUID = 6941152461497123259L;
	
	public void deleteModule(Activity act, int id){
		Log.d(TAG,"deleting module...");
		DbHelper db = new DbHelper(act);
		db.deleteModule(id);
		db.close();
	}

}
