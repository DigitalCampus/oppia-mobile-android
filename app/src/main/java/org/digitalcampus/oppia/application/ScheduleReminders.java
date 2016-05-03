/*
 * This file is part of OppiaMobile - https://digital-campus.org/
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

package org.digitalcampus.oppia.application;

import java.util.ArrayList;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.CourseIndexActivity;
import org.digitalcampus.oppia.adapter.ScheduleReminderListAdapter;
import org.digitalcampus.oppia.model.Course;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;

public class ScheduleReminders extends LinearLayout {

	public static final String TAG = ScheduleReminders.class.getSimpleName();
	private Context ctx;
	
	public ScheduleReminders(Context context) {
		super(context);
		this.ctx = context;
	}

	public ScheduleReminders(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.ctx = context;
	}

	public void initSheduleReminders(ArrayList<org.digitalcampus.oppia.model.Activity> activities) {
		setOrientation(VERTICAL);
		LinearLayout ll = (LinearLayout) findViewById(R.id.schedule_reminders);
		ll.removeAllViews();
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.schedule_reminder, this);
		if (activities.size() > 0){
			ScheduleReminders.this.setVisibility(VISIBLE);
		} else {
			ScheduleReminders.this.setVisibility(GONE);
		}
		ScheduleReminderListAdapter srla = new ScheduleReminderListAdapter(ctx, activities);
		ListView listView = (ListView) findViewById(R.id.schedule_reminder_list);
		listView.setAdapter(srla);

		listView.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Course course = (Course) view.getTag(R.id.TAG_COURSE);
				String digest = (String) view.getTag(R.id.TAG_ACTIVITY_DIGEST);
				Intent i = new Intent(getContext(), CourseIndexActivity.class);
				Bundle tb = new Bundle();
				tb.putSerializable(Course.TAG, course);
				tb.putSerializable("JumpTo", digest);
				i.putExtras(tb);
				getContext().startActivity(i);
			}
		});

	}

	
}
