package org.digitalcampus.mobile.learning.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.digitalcampus.mobile.learning.application.MobileLearning;
import org.digitalcampus.mobile.learning.model.ActivitySchedule;
import org.joda.time.DateTime;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.bugsense.trace.BugSenseHandler;

public class ModuleScheduleXMLReader {

	public static final String TAG = ModuleScheduleXMLReader.class.getSimpleName();
	private Document document;
	
	public ModuleScheduleXMLReader(String filename) {
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
		
			DateTime sdt = MobileLearning.DATE_FORMAT.parseDateTime(startDateString);
			DateTime edt = MobileLearning.DATE_FORMAT.parseDateTime(endDateString);
			
			ActivitySchedule as = new ActivitySchedule();
			as.setDigest(digest);
			as.setStartTime(sdt);
			as.setEndTime(edt);
			schedule.add(as);
		}
		return schedule;
	}
}
