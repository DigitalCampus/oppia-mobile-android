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

package org.digitalcampus.oppia.widgets;

import java.util.HashMap;

import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;

import android.content.SharedPreferences;
import android.support.v4.app.Fragment;

public abstract class WidgetFactory extends Fragment {
	
	public final static String TAG = WidgetFactory.class.getSimpleName();
	protected Activity activity = null;
	protected Course course = null;
	protected SharedPreferences prefs;
	protected boolean isBaseline = false;
	protected long startTime = System.currentTimeMillis()/1000;
	protected boolean readAloud = false;
	
	protected abstract boolean getActivityCompleted();
	public abstract void saveTracker();
	
	public abstract String getContentToRead();
	public abstract HashMap<String,Object> getWidgetConfig();
	public abstract void setWidgetConfig(HashMap<String,Object> config);
	
	public void setReadAloud(boolean readAloud){
		this.readAloud = true;
	}
	
	protected String getDigest() {
		return activity.getDigest();
	}
	
	public void setIsBaseline(boolean isBaseline) {
		this.isBaseline = isBaseline;
	}
	
	public void setStartTime(long startTime){
		this.startTime = System.currentTimeMillis()/1000;
	}
	
	public long getStartTime(){
		return this.startTime;
	}
}
