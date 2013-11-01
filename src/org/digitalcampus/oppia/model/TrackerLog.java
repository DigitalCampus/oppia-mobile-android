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

package org.digitalcampus.oppia.model;

import org.digitalcampus.oppia.application.MobileLearning;
import org.joda.time.DateTime;

public class TrackerLog {

	private DateTime datetime;
	private long id;
	private String digest;
	private String content;
	private boolean submitted;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	
	public DateTime getDatetime() {
		return datetime;
	}
	public void setDatetime(DateTime datetime) {
		this.datetime = datetime;
	}
	public String getDigest() {
		return digest;
	}
	public void setDigest(String digest) {
		this.digest = digest;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public boolean isSubmitted() {
		return submitted;
	}
	public void setSubmitted(boolean submitted) {
		this.submitted = submitted;
	}
	
	public String getDateTimeString() {
		return MobileLearning.DATETIME_FORMAT.print(datetime);
	}
	
	
}
