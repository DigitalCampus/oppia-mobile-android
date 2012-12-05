package org.digitalcampus.mobile.learning.application;

import java.io.File;

import org.digitalcampus.mobile.learning.R;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;

public class MobileLearning extends Application {

	public static final String TAG = "MobileLearning";

	// local storage vars
	public static final String MLEARN_ROOT = Environment
			.getExternalStorageDirectory() + "/digitalcampus/";
	public static final String MODULES_PATH = MLEARN_ROOT + "modules/";
	public static final String MEDIA_PATH = MLEARN_ROOT + "media/";
	public static final String DOWNLOAD_PATH = MLEARN_ROOT + "download/";
	public static final String MODULE_XML = "module.xml";

	// server path vars
	// TODO check that pref server already had trailing '/'
	
	public static final String LOGIN_PATH = "api/v1/user/";
	public static final String REGISTER_PATH = "api/v1/register/";
	public static final String MQUIZ_SUBMIT_PATH = "api/v1/quizattempt/";
	
	public static final String SERVER_MODULES_PATH = "modules/api/v1/module/";
	public static final String TRACKER_PATH = "modules/api/v1/tracker/";
	
	// general other settings
	public static final String BUGSENSE_API_KEY = "84d61fd0";
	public static final int PASSWORD_MIN_LENGTH = 6;
	public static final int PAGE_READ_TIME = 3;
	
	// only used in case a module doesn't have any lang specified
	public static final String DEFAULT_LANG = "en";
	
	public static boolean createDirs() {
		String cardstatus = Environment.getExternalStorageState();
		if (cardstatus.equals(Environment.MEDIA_REMOVED)
				|| cardstatus.equals(Environment.MEDIA_UNMOUNTABLE)
				|| cardstatus.equals(Environment.MEDIA_UNMOUNTED)
				|| cardstatus.equals(Environment.MEDIA_MOUNTED_READ_ONLY)
				|| cardstatus.equals(Environment.MEDIA_SHARED)) {
			return false;
		}

		String[] dirs = { MLEARN_ROOT, MODULES_PATH, MEDIA_PATH, DOWNLOAD_PATH };

		for (String dirName : dirs) {
			File dir = new File(dirName);
			if (!dir.exists()) {
				if (!dir.mkdirs()) {
					return false;
				}
			} else {
				if (!dir.isDirectory()) {
					return false;
				}
			}
		}
		return true;
	}
	
	public static void showAlert(Context ctx, int t, int m){
    	MobileLearning.showAlert(ctx, ctx.getString(t), ctx.getString(m));
    }
	
	public static void showAlert(Context ctx, int R, String msg){
    	MobileLearning.showAlert(ctx, ctx.getString(R), msg);
    }
	
	public static void showAlert(Context ctx, String title, String msg){
    	AlertDialog alertDialog = new AlertDialog.Builder(ctx).create();
		alertDialog.setTitle(title);
		alertDialog.setMessage(msg);
		alertDialog.setButton(ctx.getString(R.string.close), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}});
		alertDialog.show();
    }

}
