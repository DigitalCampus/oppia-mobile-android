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

package org.digitalcampus.oppia.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.exception.InvalidXMLException;
import org.digitalcampus.oppia.model.ActivitySchedule;
import org.joda.time.DateTime;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class CourseScheduleXMLReader {

	public static final String TAG = CourseScheduleXMLReader.class.getSimpleName();
	private Document document;
	
	public CourseScheduleXMLReader(String filename) throws InvalidXMLException {
		File courseXML = new File(filename);
		if (courseXML.exists()) {

			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder;
			try {
				builder = factory.newDocumentBuilder();
				document = builder.parse(courseXML);
			} catch (ParserConfigurationException e) {
				throw new InvalidXMLException(e);
			} catch (SAXException e) {
				throw new InvalidXMLException(e);
			} catch (IOException e) {
				throw new InvalidXMLException(e);
			}
		}
	}
	
	public long getScheduleVersion(){
		if (this.document == null){
			return 0;
		}
		Node schedule = document.getDocumentElement();
		NamedNodeMap attrs = schedule.getAttributes();
		long version = Long.parseLong(attrs.getNamedItem("version").getTextContent());
		return version;
	}
	
	public ArrayList<ActivitySchedule> getSchedule(){
		ArrayList<ActivitySchedule> schedule = new ArrayList<ActivitySchedule>();
		if (this.document == null){
			return schedule;
		}
		NodeList actscheds = document.getFirstChild().getChildNodes();
		for (int i=0; i<actscheds.getLength(); i++) {
			NamedNodeMap attrs = actscheds.item(i).getAttributes();
			String digest = attrs.getNamedItem("digest").getTextContent();
			String startDateString = attrs.getNamedItem("startdate").getTextContent();
			String endDateString = attrs.getNamedItem("enddate").getTextContent();
		
			DateTime sdt = MobileLearning.DATETIME_FORMAT.parseDateTime(startDateString);
			DateTime edt = MobileLearning.DATETIME_FORMAT.parseDateTime(endDateString);
			
			ActivitySchedule as = new ActivitySchedule();
			as.setDigest(digest);
			as.setStartTime(sdt);
			as.setEndTime(edt);
			schedule.add(as);
		}
		return schedule;
	}
}
