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

import org.digitalcampus.oppia.application.MobileLearning;
import org.joda.time.DateTime;

public class Badges {

	private DateTime datetime;
	private String description;
	private String icon;
	
	public String getDateAsString() {
		return MobileLearning.DATE_FORMAT.print(datetime);
	}
	
	public String getTimeAsString() {
		return MobileLearning.TIME_FORMAT.print(datetime);
	}
	
	public void setDateTime(String date) {
		this.datetime = MobileLearning.DATETIME_FORMAT.parseDateTime(date);;
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
	
	public void setPoints(String icon) {
		this.icon = icon;
	}
}
