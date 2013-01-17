package org.digitalcampus.mobile.learning.utils;

import java.util.concurrent.Callable;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.activity.ScoreActivity;
import org.digitalcampus.mobile.learning.application.MobileLearning;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class UIUtils {
	
	public final static String TAG = UIUtils.class.getSimpleName();
	
	public static void showUserData(final Activity act){
		//TextView username = (TextView) act.findViewById(R.id.username);
		TextView points = (TextView) act.findViewById(R.id.userpoints);
		TextView badges = (TextView) act.findViewById(R.id.userbadges);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(act.getBaseContext());
		if(MobileLearning.isLoggedIn(act)){
			points.setVisibility(View.VISIBLE);
			badges.setVisibility(View.VISIBLE);
			//username.setText(prefs.getString("prefDisplayName", uname));
			points.setText(String.valueOf(prefs.getInt("prefPoints", 100)));
			badges.setText(String.valueOf(prefs.getInt("prefBadges", 0)));
			
			points.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) {
					act.startActivity(new Intent(act, ScoreActivity.class));
					
				}
			});
		}
	}
	
	public static void showAlert(Context ctx, int title, int msg){
		UIUtils.showAlert(ctx, ctx.getString(title), ctx.getString(msg));
    }
	
	public static void showAlert(Context ctx, int R, String msg){
		UIUtils.showAlert(ctx, ctx.getString(R), msg);
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
	
	public static void showAlert(Context ctx, int title, int msg, Callable<Boolean> funct){
		UIUtils.showAlert(ctx, ctx.getString(title), ctx.getString(msg), funct);
    }
	
	public static void showAlert(Context ctx, int R, String msg, Callable<Boolean> funct){
		UIUtils.showAlert(ctx, ctx.getString(R), msg, funct);
    }
	
	public static void showAlert(Context ctx, String title, String msg, final Callable<Boolean> funct){
    	AlertDialog alertDialog = new AlertDialog.Builder(ctx).create();
		alertDialog.setTitle(title);
		alertDialog.setMessage(msg);
		alertDialog.setCancelable(true);
		alertDialog.setButton(ctx.getString(R.string.close), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
		}});
		alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			public void onCancel(DialogInterface dialog) {
				try {
					funct.call();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});
		alertDialog.show();
    }

}
