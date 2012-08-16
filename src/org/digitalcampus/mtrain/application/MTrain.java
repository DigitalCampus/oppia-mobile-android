package org.digitalcampus.mtrain.application;

import java.io.File;

import org.digitalcampus.mtrain.R;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;

public class MTrain extends Application {

	public static final String TAG = "MTrain";

	// local storage vars
	public static final String MTRAIN_ROOT = Environment
			.getExternalStorageDirectory() + "/mtrain/";
	public static final String MODULES_PATH = MTRAIN_ROOT + "modules/";
	public static final String MEDIA_PATH = MTRAIN_ROOT + "media/";
	public static final String DOWNLOAD_PATH = MTRAIN_ROOT + "download/";
	public static final String MODULE_XML = "module.xml";

	// server path vars
	public static final String SERVER_MODULES_PATH = "/modules/list/";
	public static final String TRACKER_PATH = "/api/?method=tracker";
	public static final String LOGIN_PATH = "/api/?method=login";
	public static final String REGISTER_PATH = "/api/?method=register";
	public static final String MQUIZ_SUBMIT_PATH = "/api/?method=submit";
	
	// general other settings
	public static final int PASSWORD_MIN_LENGTH = 6;
	public static final int PAGE_READ_TIME = 3;
	
	public static void createMTrainDirs() throws RuntimeException {
		String cardstatus = Environment.getExternalStorageState();
		if (cardstatus.equals(Environment.MEDIA_REMOVED)
				|| cardstatus.equals(Environment.MEDIA_UNMOUNTABLE)
				|| cardstatus.equals(Environment.MEDIA_UNMOUNTED)
				|| cardstatus.equals(Environment.MEDIA_MOUNTED_READ_ONLY)
				|| cardstatus.equals(Environment.MEDIA_SHARED)) {
			RuntimeException e = new RuntimeException(
					"mTrain reports :: SDCard error: "
							+ Environment.getExternalStorageState());
			throw e;
		}

		String[] dirs = { MTRAIN_ROOT, MODULES_PATH, MEDIA_PATH, DOWNLOAD_PATH };

		for (String dirName : dirs) {
			File dir = new File(dirName);
			if (!dir.exists()) {
				if (!dir.mkdirs()) {
					RuntimeException e = new RuntimeException(
							"mTrain reports :: Cannot create directory: "
									+ dirName);
					throw e;
				}
			} else {
				if (!dir.isDirectory()) {
					RuntimeException e = new RuntimeException(
							"mTrain reports :: " + dirName
									+ " exists, but is not a directory");
					throw e;
				}
			}
		}
	}
	
	public static void showAlert(Context ctx, int t, int m){
    	MTrain.showAlert(ctx, ctx.getString(t), ctx.getString(m));
    }
	
	public static void showAlert(Context ctx, int R, String msg){
    	MTrain.showAlert(ctx, ctx.getString(R), msg);
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
