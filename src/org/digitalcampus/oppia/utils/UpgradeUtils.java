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

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.concurrent.Callable;

import org.digitalcampus.mobile.learning.R;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.text.Html;

public class UpgradeUtils {

	public final static String TAG = UpgradeUtils.class.getSimpleName();
	public final static String UPGRADE_PREFIX = "Upgrade";
	private Context ctx;

	public UpgradeUtils(Context ctx) {
		this.ctx = ctx;
	}

	public void show() {
        PackageInfo versionInfo = getPackageInfo();
 
        // the eulaKey changes every time you increment the version number in the AndroidManifest.xml
        final String upgradeKey = UPGRADE_PREFIX + versionInfo.versionCode;
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);

        if(!prefs.getBoolean(upgradeKey, false)){
 
            String title = ctx.getString(R.string.app_name) + " v" + versionInfo.versionName;
 
            //Includes the updates as well so users know what changed.
            String messagePath = FileUtils.getLocalizedFilePath((Activity) ctx,prefs.getString(ctx.getString(R.string.prefs_language), Locale.getDefault().getLanguage()) , "upgrade.txt");
            
            String message = "";
			try {
				InputStream stream = ((Activity) ctx).getAssets().open(messagePath);
				message = FileUtils.readFile(stream);
			} catch (IOException e) {
				e.printStackTrace();
			}
            
            UIUtils.showAlert(ctx, title, Html.fromHtml(message), new Callable<Boolean>() {
				
				public Boolean call() throws Exception {
					SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean(upgradeKey, true);
                    editor.commit();
					return true;
				}
			});
        }
    }
	private PackageInfo getPackageInfo() {
		PackageInfo pi = null;
		try {
			pi = ctx.getPackageManager()
					.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		return pi;
	}
}
