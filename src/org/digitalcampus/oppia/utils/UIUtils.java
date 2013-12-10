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

package org.digitalcampus.oppia.utils;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.Callable;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.model.Lang;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class UIUtils {

	public final static String TAG = UIUtils.class.getSimpleName();
	private ArrayList<String> langStringList;
	private ArrayList<Lang> langList;
	private SharedPreferences prefs;
	private Context ctx;

	
	
	 /**
     * Displays the users points and badges scores in the app header
     * @param act
     */
	public static void showUserData(Menu menu, Context ctx) {
		MenuItem pointsItem = menu.findItem(R.id.points);

		if(pointsItem == null){
			return;
		}
		
		TextView points = (TextView) pointsItem.getActionView().findViewById(R.id.userpoints);
		TextView badges = (TextView) pointsItem.getActionView().findViewById(R.id.userbadges);

		if(points == null || badges == null){
			return;
		}
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		boolean scoringEnabled = prefs.getBoolean(ctx.getString(R.string.prefs_scoring_enabled), true);
		if (scoringEnabled) {
			points.setVisibility(View.VISIBLE);
			badges.setVisibility(View.VISIBLE);
			points.setText(String.valueOf(prefs.getInt(ctx.getString(R.string.prefs_points), 0)));
			badges.setText(String.valueOf(prefs.getInt(ctx.getString(R.string.prefs_badges), 0)));
		} else {
			points.setVisibility(View.GONE);
			badges.setVisibility(View.GONE);
		}
	}
	/**
	 * @param ctx
	 * @param title
	 * @param msg
	 * @return
	 */
	public static AlertDialog showAlert(Context ctx, int title, int msg) {
		return UIUtils.showAlert(ctx, ctx.getString(title), ctx.getString(msg), ctx.getString(R.string.close));
	}

	/**
	 * @param ctx
	 * @param title
	 * @param msg
	 * @return
	 */
	public static AlertDialog showAlert(Context ctx, int title, int msg, int btnText) {
		return UIUtils.showAlert(ctx, ctx.getString(title), ctx.getString(msg), ctx.getString(btnText));
	}
	
	/**
	 * @param ctx
	 * @param Res
	 * @param msg
	 * @return
	 */
	public static AlertDialog showAlert(Context ctx, int Res, String msg) {
		return UIUtils.showAlert(ctx, ctx.getString(Res), msg, ctx.getString(R.string.close));
	}

	public static AlertDialog showAlert(Context ctx, String title, String msg) {
		return UIUtils.showAlert(ctx, title, msg, ctx.getString(R.string.close));
	}
	
	/**
	 * @param ctx
	 * @param title
	 * @param msg
	 * @return
	 */
	public static AlertDialog showAlert(Context ctx, String title, String msg, String btnText) {
		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		builder.setTitle(title);
		builder.setMessage(msg);
		builder.setNeutralButton(btnText, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		AlertDialog alert = builder.create();
        alert.show();
		return alert;
	}

	/**
	 * @param ctx
	 * @param title
	 * @param msg
	 * @param funct
	 * @return
	 */
	public static AlertDialog showAlert(Context ctx, int title, int msg, Callable<Boolean> funct) {
		return UIUtils.showAlert(ctx, ctx.getString(title), ctx.getString(msg), funct);
	}

	public static AlertDialog showAlert(Context ctx, int title, int msg, int btnText, Callable<Boolean> funct) {
		return UIUtils.showAlert(ctx, ctx.getString(title), ctx.getString(msg),ctx.getString(btnText), funct);
	}
	/**
	 * @param ctx
	 * @param R
	 * @param msg
	 * @param funct
	 * @return
	 */
	public static AlertDialog showAlert(Context ctx, int R, CharSequence msg, Callable<Boolean> funct) {
		return UIUtils.showAlert(ctx, ctx.getString(R), msg, funct);
	}

	public static AlertDialog showAlert(Context ctx, String title, CharSequence msg, final Callable<Boolean> funct) {
		return UIUtils.showAlert(ctx, title, msg, ctx.getString(R.string.close),funct);
	}
	/**
	 * @param ctx
	 * @param title
	 * @param msg
	 * @param funct
	 * @return
	 */
	public static AlertDialog showAlert(Context ctx, String title, CharSequence msg, String btnText, final Callable<Boolean> funct) {
		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		builder.setTitle(title);
		builder.setMessage(msg);
		builder.setCancelable(true);
		builder.setNeutralButton(btnText, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
			public void onCancel(DialogInterface dialog) {
				try {
					funct.call();
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});
		AlertDialog alert = builder.create();
        alert.show();
		return alert;
	}
	
	
	/**
	 * 
	 * @param ctx
	 * @param langs
	 * @param prefs
	 * @param funct
	 */
	public void createLanguageDialog(Context ctx, ArrayList<Lang> langs, SharedPreferences prefs, final Callable<Boolean> funct) {
		this.langStringList = new ArrayList<String>();
		this.langList = new ArrayList<Lang>();
		this.prefs = prefs;
		this.ctx = ctx;
		
		// make sure there aren't any duplicates
		for(Lang l: langs){
			boolean found = false;
			for(Lang ln: langList){
				if(ln.getLang().equals(l.getLang())){
					found = true;
				}
			}
			if(!found){
				langList.add(l);
			}
		}
		
		int selected = -1;
		int i = 0;
		for(Lang l: langList){
			Locale loc = new Locale(l.getLang());
			String langDisp = loc.getDisplayLanguage(loc);
			langStringList.add(langDisp);
			if (l.getLang().equals(prefs.getString(ctx.getString(R.string.prefs_language), Locale.getDefault().getLanguage()))) {
				selected = i;
			}
			i++;
		}
		
		// only show if at least one language
		if (i > 0) {
			ArrayAdapter<String> arr = new ArrayAdapter<String>(ctx, android.R.layout.select_dialog_singlechoice,langStringList);

			AlertDialog mAlertDialog = new AlertDialog.Builder(ctx)
					.setSingleChoiceItems(arr, selected, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							String newLang = langList.get(whichButton).getLang();
							Editor editor = UIUtils.this.prefs.edit();
							editor.putString(UIUtils.this.ctx.getString(R.string.prefs_language), newLang);
							editor.commit();
							dialog.dismiss();
							try {
								funct.call();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}).setTitle(ctx.getString(R.string.change_language))
					.setNegativeButton(ctx.getString(R.string.cancel), new DialogInterface.OnClickListener() {
	
						public void onClick(DialogInterface dialog, int which) {
							// do nothing
						}
	
					}).create();
			mAlertDialog.show();
		}
	}
	

}
