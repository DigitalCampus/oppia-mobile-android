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

package org.digitalcampus.oppia.utils.xmlreaders;

import org.digitalcampus.oppia.exception.InvalidXMLException;
import org.digitalcampus.oppia.model.ActivitySchedule;
import org.digitalcampus.oppia.utils.DateUtils;
import org.joda.time.DateTime;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

public class CourseScheduleXMLReader {

	public static final String TAG = CourseScheduleXMLReader.class.getSimpleName();
	private Document document;
	
	public CourseScheduleXMLReader(File courseXML) throws InvalidXMLException {
		if (courseXML.exists()) {

			try {
				DocumentBuilder builder = XMLSecurityHelper.getNewSecureDocumentBuilder();
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

	public CourseScheduleXMLReader(String xmlContent) throws InvalidXMLException {


		try {
			DocumentBuilder builder = XMLSecurityHelper.getNewSecureDocumentBuilder();
			InputSource is = new InputSource(new StringReader(xmlContent));
			document = builder.parse(is);
		} catch (ParserConfigurationException e) {
			throw new InvalidXMLException(e);
		} catch (SAXException e) {
			throw new InvalidXMLException(e);
		} catch (IOException e) {
			throw new InvalidXMLException(e);
		}

	}
	
	public long getScheduleVersion(){
		if (this.document == null){
			return 0;
		}
		Node schedule = document.getDocumentElement();
		NamedNodeMap attrs = schedule.getAttributes();
		return Long.parseLong(attrs.getNamedItem("version").getTextContent());
	}
	
	public ArrayList<ActivitySchedule> getSchedule(){
		ArrayList<ActivitySchedule> schedule = new ArrayList<>();
		if (this.document == null){
			return schedule;
		}
		NodeList actscheds = document.getFirstChild().getChildNodes();
		for (int i=0; i<actscheds.getLength(); i++) {
			NamedNodeMap attrs = actscheds.item(i).getAttributes();
			String digest = attrs.getNamedItem("digest").getTextContent();
			String startDateString = attrs.getNamedItem("startdate").getTextContent();
			String endDateString = attrs.getNamedItem("enddate").getTextContent();
		
			DateTime sdt = DateUtils.DATETIME_FORMAT.parseDateTime(startDateString);
			DateTime edt = DateUtils.DATETIME_FORMAT.parseDateTime(endDateString);
			
			ActivitySchedule as = new ActivitySchedule();
			as.setDigest(digest);
			as.setStartTime(sdt);
			as.setEndTime(edt);
			schedule.add(as);
		}
		return schedule;
	}
}
