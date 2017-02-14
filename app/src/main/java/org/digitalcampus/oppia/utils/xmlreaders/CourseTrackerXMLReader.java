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

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.exception.InvalidXMLException;
import org.digitalcampus.oppia.model.QuizAttempt;
import org.digitalcampus.oppia.model.TrackerLog;
import org.joda.time.DateTime;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class CourseTrackerXMLReader {
	
	public static final String TAG = CourseTrackerXMLReader.class.getSimpleName();
	
	private static final String NODE_TYPE = "type";
	private static final String NODE_QUIZ = "quiz";
    private static final String NODE_DIGEST = "digest";
    private static final String NODE_SUBMITTEDDATE = "submitteddate";
    private static final String NODE_COMPLETED = "completed";
    private static final String NODE_SCORE = "score";
    private static final String NODE_MAXSCORE = "maxscore";
    private static final String NODE_PASSED = "passed";
    
	private Document document;
	
	public CourseTrackerXMLReader(String filename) throws InvalidXMLException {
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

	
	public ArrayList<TrackerLog> getTrackers(long courseId, long userId){
		ArrayList<TrackerLog> trackers = new ArrayList<TrackerLog>();
		if (this.document == null){
			return trackers;
		}
		NodeList actTrackers = document.getFirstChild().getChildNodes();
		for (int i=0; i<actTrackers.getLength(); i++) {
			NamedNodeMap attrs = actTrackers.item(i).getAttributes();
			String digest = attrs.getNamedItem(NODE_DIGEST).getTextContent();
			String submittedDateString = attrs.getNamedItem(NODE_SUBMITTEDDATE).getTextContent();
			DateTime sdt = MobileLearning.DATETIME_FORMAT.parseDateTime(submittedDateString);
			
			boolean completed;
			try {
				completed = Boolean.parseBoolean(attrs.getNamedItem(NODE_COMPLETED).getTextContent());
			} catch (NullPointerException npe) {
				completed = true;
			}
			
			String type;
			try {
				type = attrs.getNamedItem(NODE_TYPE).getTextContent();
			} catch (NullPointerException npe) {
				type = null;
			}
			
			TrackerLog t = new TrackerLog();
			t.setDigest(digest);
			t.setSubmitted(true);
			t.setDatetime(sdt);
			t.setCompleted(completed);
			t.setType(type);
			t.setCourseId(courseId);
			t.setUserId(userId);
			trackers.add(t);
		}
		return trackers;
	}
	
	public ArrayList<QuizAttempt> getQuizAttempts(long courseId, long userId){
		ArrayList<QuizAttempt> quizAttempts = new ArrayList<QuizAttempt>();
		if (this.document == null){
			return quizAttempts;
		}
		NodeList actTrackers = document.getFirstChild().getChildNodes();
		for (int i=0; i<actTrackers.getLength(); i++) {
			NamedNodeMap attrs = actTrackers.item(i).getAttributes();
			String digest = attrs.getNamedItem(NODE_DIGEST).getTextContent();
			String type;
			try {
				type = attrs.getNamedItem(NODE_TYPE).getTextContent();
			} catch (NullPointerException npe) {
				type = null;
			}
			
			// if quiz activity then get the results etc
			if (type != null && type.equalsIgnoreCase(NODE_QUIZ)){
				NodeList quizNodes = actTrackers.item(i).getChildNodes();
				for (int j=0; j<quizNodes.getLength(); j++) {
					NamedNodeMap quizAttrs = quizNodes.item(j).getAttributes();
					float maxscore = Float.parseFloat(quizAttrs.getNamedItem(NODE_MAXSCORE).getTextContent());
					float score = Float.parseFloat(quizAttrs.getNamedItem(NODE_SCORE).getTextContent());
					String submittedDateString = quizAttrs.getNamedItem(NODE_SUBMITTEDDATE).getTextContent();
					DateTime sdt = MobileLearning.DATETIME_FORMAT.parseDateTime(submittedDateString);
					
					boolean passed;
					try {
						passed = Boolean.parseBoolean(quizAttrs.getNamedItem(NODE_PASSED).getTextContent());
					} catch (NullPointerException npe) {
						passed = true;
					}
					
					QuizAttempt qa = new QuizAttempt();
					qa.setCourseId(courseId);
					qa.setUserId(userId);
					qa.setActivityDigest(digest);
					qa.setScore(score);
					qa.setMaxscore(maxscore);
					qa.setPassed(passed);
					qa.setSent(true);
					qa.setDatetime(sdt);
					quizAttempts.add(qa);
				}
			}
		}
		return quizAttempts;
	}
}
