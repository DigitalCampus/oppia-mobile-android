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

package org.digitalcampus.mobile.learning.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.digitalcampus.mobile.learning.application.MobileLearning;
import org.digitalcampus.mobile.learning.model.TrackerLog;
import org.joda.time.DateTime;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.bugsense.trace.BugSenseHandler;

public class ModuleTrackerXMLReader {
	public static final String TAG = ModuleTrackerXMLReader.class.getSimpleName();
	private Document document;
	
	public ModuleTrackerXMLReader(String filename) {
		// TODO check that it's a valid module xml file else throw error
		File moduleXML = new File(filename);
		if (moduleXML.exists()) {

			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder;
			try {
				builder = factory.newDocumentBuilder();
				document = builder.parse(moduleXML);

			} catch (ParserConfigurationException e) {
				BugSenseHandler.sendException(e);
				e.printStackTrace();
			} catch (SAXException e) {
				BugSenseHandler.sendException(e);
				e.printStackTrace();
			} catch (IOException e) {
				BugSenseHandler.sendException(e);
				e.printStackTrace();
			}
		}
	}
	
	
	public ArrayList<TrackerLog> getTrackers(){
		ArrayList<TrackerLog> trackers = new ArrayList<TrackerLog>();
		if (this.document == null){
			return trackers;
		}
		NodeList actscheds = document.getFirstChild().getChildNodes();
		for (int i=0; i<actscheds.getLength(); i++) {
			NamedNodeMap attrs = actscheds.item(i).getAttributes();
			String digest = attrs.getNamedItem("digest").getTextContent();
			String submittedDateString = attrs.getNamedItem("submitteddate").getTextContent();
		
			DateTime sdt = MobileLearning.DATE_FORMAT.parseDateTime(submittedDateString);
			
			TrackerLog t = new TrackerLog();
			t.setDigest(digest);
			t.setSubmitted(true);
			t.setDatetime(sdt);
			trackers.add(t);
		}
		return trackers;
	}
}
