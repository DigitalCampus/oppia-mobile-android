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

package org.digitalcampus.oppia.listener;

import java.io.File;
import java.util.List;

import org.digitalcampus.mobile.learning.R;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class OnResourceClickListener implements OnClickListener{

	private Context ctx;
	private String type;
	
	public OnResourceClickListener(Context ctx, String type){
		this.ctx = ctx;
		this.type = type;
	}

	public void onClick(View v) {
		File file = (File) v.getTag();
		// check the file is on the file system (should be but just in case)
		if(!file.exists()){
			Toast.makeText(this.ctx,this.ctx.getString(R.string.error_resource_not_found,file.getName()), Toast.LENGTH_LONG).show();
			return;
		} 
		Uri targetUri = Uri.fromFile(file);
		
		// check there is actually an app installed to open this filetype
		
		Intent intent = new Intent();
		intent.setAction(android.content.Intent.ACTION_VIEW);
		intent.setDataAndType(targetUri, type);
		
		PackageManager pm = this.ctx.getPackageManager();

		List<ResolveInfo> infos = pm.queryIntentActivities(intent, PackageManager.GET_RESOLVED_FILTER);
		boolean appFound = false;
		for (ResolveInfo info : infos) {
			IntentFilter filter = info.filter;
			if (filter != null && filter.hasAction(Intent.ACTION_VIEW)) {
				// Found an app with the right intent/filter
				appFound = true;
			}
		}

		if(appFound){
			this.ctx.startActivity(intent);
		} else {
			Toast.makeText(this.ctx,this.ctx.getString(R.string.error_resource_app_not_found,file.getName()), Toast.LENGTH_LONG).show();
		}
		return;
	}
	
}