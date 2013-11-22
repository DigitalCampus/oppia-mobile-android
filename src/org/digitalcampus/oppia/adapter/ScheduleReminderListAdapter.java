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

package org.digitalcampus.oppia.adapter;

import java.util.ArrayList;
import java.util.Locale;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.model.Course;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ScheduleReminderListAdapter extends ArrayAdapter<org.digitalcampus.oppia.model.Activity> {

	public static final String TAG = ScheduleReminderListAdapter.class.getSimpleName();

	private final Context ctx;
	private final ArrayList<org.digitalcampus.oppia.model.Activity> activityList;
	private SharedPreferences prefs;
	
	public ScheduleReminderListAdapter(Context context, ArrayList<org.digitalcampus.oppia.model.Activity> activityList) {
		super(context, R.layout.schedule_reminder_list_row, activityList);
		this.ctx = context;
		this.activityList = activityList;
		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    View rowView = inflater.inflate(R.layout.schedule_reminder_list_row, parent, false);
	    org.digitalcampus.oppia.model.Activity a = activityList.get(position);
	    DbHelper db = new DbHelper(ctx);
		Course m = db.getCourse(a.getModId());
		db.close();
	    
		String lang = prefs.getString(ctx.getString(R.string.prefs_language), Locale.getDefault().getLanguage());
		
		TextView scheduleTitle = (TextView) rowView.findViewById(R.id.schedule_title);
		scheduleTitle.setText(m.getTitle(lang) + ": " + a.getTitle(lang));
		rowView.setTag(R.id.TAG_COURSE_ID,m);
		rowView.setTag(R.id.TAG_ACTIVITY_DIGEST,a.getDigest());

	    return rowView;
	}

}
