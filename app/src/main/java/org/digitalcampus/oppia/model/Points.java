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

public class Points {

	private DateTime datetime;
	private String event;
    private String description;
	private int pointsAwarded;

	public String getDateDayMonth() {
		return DateUtils.DATE_FORMAT_DAY_MONTH.print(datetime);
	}


	public String getTimeHoursMinutes() {
		return DateUtils.TIME_FORMAT_HOURS_MINUTES.print(datetime);
	}
	
	public void setDateTime(String date) {
		this.datetime = DateUtils.DATETIME_FORMAT.parseDateTime(date);
	}

	public DateTime getDateTime() {
		return datetime;
	}

	public String getEvent() {
		return event;
	}
	
	public void setEvent(String event) {
		this.event = event;
	}

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
	
	public int getPointsAwarded() {
		return pointsAwarded;
	}
	
	public void setPointsAwarded(int points) {
		this.pointsAwarded = points;
	}
}
