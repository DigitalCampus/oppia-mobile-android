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

import org.digitalcampus.oppia.analytics.Analytics;

public class InvalidXMLException extends Exception {

	public static final String TAG = InvalidXMLException.class.getSimpleName();
	private static final long serialVersionUID = -2986632352088699106L;

	public InvalidXMLException(String message){
		super(message);
	}

	public InvalidXMLException(Exception e, String message){
		this(message);

		Analytics.logException(e);
		e.printStackTrace();
	}

	public InvalidXMLException(Exception e){
		this(e, e.getMessage());
	}
	


}
