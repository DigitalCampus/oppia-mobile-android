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

package org.digitalcampus.oppia.activity;

import java.util.ArrayList;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.application.ScheduleReminders;

import android.support.v7.app.ActionBarActivity;

public class AppActivity extends ActionBarActivity {
	
	public static final String TAG = AppActivity.class.getSimpleName();
	
	private ScheduleReminders reminders;

	
	/**
	 * @param activities
	 */
	public void drawReminders(ArrayList<org.digitalcampus.oppia.model.Activity> activities){
		try {
			reminders = (ScheduleReminders) findViewById(R.id.schedule_reminders);
			reminders.initSheduleReminders(activities);
		} catch (NullPointerException npe) {
			// do nothing
		}
	}

}
