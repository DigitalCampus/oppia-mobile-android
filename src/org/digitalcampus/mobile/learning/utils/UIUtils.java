/* 
 * This file is part of OppiaMobile - http://oppia-mobile.org/
 * 
 * OppiaMobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * OppiaMobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with OppiaMobile. If not, see <http://www.gnu.org/licenses/>.
 */

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
import android.view.View;
import android.widget.TextView;

public class UIUtils {

	public final static String TAG = UIUtils.class.getSimpleName();

	public static void showUserData(final Activity act) {
		// TextView username = (TextView) act.findViewById(R.id.username);
		TextView points = (TextView) act.findViewById(R.id.userpoints);
		TextView badges = (TextView) act.findViewById(R.id.userbadges);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(act.getBaseContext());
		if (MobileLearning.isLoggedIn(act)) {
			points.setVisibility(View.VISIBLE);
			badges.setVisibility(View.VISIBLE);
			// username.setText(prefs.getString("prefDisplayName", uname));
			points.setText(String.valueOf(prefs.getInt("prefPoints", 100)));
			badges.setText(String.valueOf(prefs.getInt("prefBadges", 0)));

			if (!(act instanceof ScoreActivity)) {
				points.setOnClickListener(new View.OnClickListener() {

					public void onClick(View v) {
						act.startActivity(new Intent(act, ScoreActivity.class));
					}
				});
			}
		}
	}

	public static AlertDialog showAlert(Context ctx, int title, int msg) {
		return UIUtils.showAlert(ctx, ctx.getString(title), ctx.getString(msg));
	}

	public static AlertDialog showAlert(Context ctx, int R, String msg) {
		return UIUtils.showAlert(ctx, ctx.getString(R), msg);
	}

	public static AlertDialog showAlert(Context ctx, String title, String msg) {
		AlertDialog alertDialog = new AlertDialog.Builder(ctx).create();
		alertDialog.setTitle(title);
		alertDialog.setMessage(msg);
		alertDialog.setButton(ctx.getString(R.string.close), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		alertDialog.show();
		return alertDialog;
	}

	public static AlertDialog showAlert(Context ctx, int title, int msg, Callable<Boolean> funct) {
		return UIUtils.showAlert(ctx, ctx.getString(title), ctx.getString(msg), funct);
	}

	public static AlertDialog showAlert(Context ctx, int R, String msg, Callable<Boolean> funct) {
		return UIUtils.showAlert(ctx, ctx.getString(R), msg, funct);
	}

	public static AlertDialog showAlert(Context ctx, String title, String msg, final Callable<Boolean> funct) {
		AlertDialog alertDialog = new AlertDialog.Builder(ctx).create();
		alertDialog.setTitle(title);
		alertDialog.setMessage(msg);
		alertDialog.setCancelable(true);
		alertDialog.setButton(ctx.getString(R.string.close), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			public void onCancel(DialogInterface dialog) {
				try {
					funct.call();
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});
		alertDialog.show();
		return alertDialog;
	}
	
	

}
