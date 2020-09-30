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

package org.digitalcampus.oppia.model;

import org.digitalcampus.oppia.utils.DateUtils;
import org.joda.time.DateTime;

public class Badge {

	private DateTime datetime;
	private String description;
	private String icon;

	public Badge(){

	}

	public Badge(DateTime datetime, String description) {
		this.datetime = datetime;
		this.description = description;
	}

	public String getDateAsString() {
		return DateUtils.DATE_FORMAT.print(datetime);
	}
	
	public void setDateTime(String date) {
		this.datetime = DateUtils.DATETIME_FORMAT.parseDateTime(date);
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getIcon() {
		return icon;
	}
	
	public void setIcon(String icon) {
		this.icon = icon;
	}
}
