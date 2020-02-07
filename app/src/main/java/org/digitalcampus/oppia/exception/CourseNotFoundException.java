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

package org.digitalcampus.oppia.exception;

import org.digitalcampus.oppia.database.DbHelper;

import android.app.Activity;

public class CourseNotFoundException extends Exception {

	public static final String TAG = CourseNotFoundException.class.getSimpleName();
	private static final long serialVersionUID = 6941152461497123259L;
	
	public void deleteCourse(Activity act, int id){
		DbHelper db = DbHelper.getInstance(act);
		db.deleteCourse(id);
	}

}
