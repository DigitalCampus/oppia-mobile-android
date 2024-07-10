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

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.exception.InvalidXMLException;
import org.digitalcampus.oppia.gamification.Gamification;
import org.digitalcampus.oppia.model.QuizAttempt;
import org.digitalcampus.oppia.model.TrackerLog;
import org.digitalcampus.oppia.utils.DateUtils;
import org.joda.time.DateTime;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

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
    private static final String NODE_EVENT = "event";
    private static final String NODE_POINTS = "points";
    private static final String NODE_UUID = "uuid";

    private Document document;

    public CourseTrackerXMLReader(File courseXML) throws InvalidXMLException {
        if (courseXML.exists()) {

            try {
                DocumentBuilder builder = XMLSecurityHelper.getNewSecureDocumentBuilder();
                document = builder.parse(courseXML);
            } catch (ParserConfigurationException | SAXException | IOException e) {
                throw new InvalidXMLException(e);
            }
        }
    }

    public CourseTrackerXMLReader(String xmlContent) throws InvalidXMLException {

        try {
            DocumentBuilder builder = XMLSecurityHelper.getNewSecureDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xmlContent));
            document = builder.parse(is);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new InvalidXMLException(e);
        }
    }


    public List<TrackerLog> getTrackers(Context ctx, long courseId, long userId) {
        List<TrackerLog> trackers = new ArrayList<>();
        if (this.document == null) {
            return trackers;
        }
        List<String> uuidList = getExistingTrackersIds(ctx, userId);
        NodeList actTrackers = document.getFirstChild().getChildNodes();
        for (int i = 0; i < actTrackers.getLength(); i++) {
            NamedNodeMap attrs = actTrackers.item(i).getAttributes();
            String uuid = attrs.getNamedItem(NODE_UUID).getTextContent();
            if (uuidList.contains(uuid)) {
                continue;
            }

            String digest = attrs.getNamedItem(NODE_DIGEST).getTextContent();
            String submittedDateString = attrs.getNamedItem(NODE_SUBMITTEDDATE).getTextContent();
            DateTime sdt = DateUtils.DATETIME_FORMAT.parseDateTime(submittedDateString);

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

            String event;
            try {
                event = attrs.getNamedItem(NODE_EVENT).getTextContent();
            } catch (NullPointerException npe) {
                event = Gamification.EVENT_NAME_UNDEFINED;
            }

            int points;
            try {
                points = Integer.parseInt(attrs.getNamedItem(NODE_POINTS).getTextContent());
            } catch (NullPointerException | NumberFormatException npe) {
                points = 0;
            }

            TrackerLog t = new TrackerLog();
            t.setDigest(digest);
            t.setSubmitted(true);
            t.setDatetime(sdt);
            t.setCompleted(completed);
            t.setType(type);
            t.setCourseId(courseId);
            t.setUserId(userId);
            t.setEvent(event);
            t.setPoints(points);
            trackers.add(t);
        }
        return trackers;
    }

    private List<String> getExistingTrackersIds(Context ctx, long userId) {
        DbHelper db = DbHelper.getInstance(ctx);
        List<TrackerLog> userTrackers = db.getTrackers(userId, false);
        List<String> uuidList = userTrackers.stream().map(t -> {
            if (t.getContent() != null) {
                JsonElement data = new Gson().fromJson(t.getContent(), JsonObject.class).get("data");
                if (data != null) {
                    JsonElement uuid = new Gson().fromJson(data.getAsString(), JsonObject.class).get("uuid");
                    if (uuid != null) {
                        return uuid.getAsString();
                    }
                }
            }
            return "";
        }).collect(Collectors.toList());
        return uuidList;
    }

    public List<QuizAttempt> getQuizAttempts(long courseId, long userId) {
        ArrayList<QuizAttempt> quizAttempts = new ArrayList<>();
        if (this.document == null) {
            return quizAttempts;
        }
        NodeList actTrackers = document.getFirstChild().getChildNodes();
        for (int i = 0; i < actTrackers.getLength(); i++) {
            NamedNodeMap attrs = actTrackers.item(i).getAttributes();
            String digest = attrs.getNamedItem(NODE_DIGEST).getTextContent();
            String type;
            try {
                type = attrs.getNamedItem(NODE_TYPE).getTextContent();
            } catch (NullPointerException npe) {
                type = null;
            }

            // if quiz activity then get the results etc
            if (type != null && type.equalsIgnoreCase(NODE_QUIZ)) {
                NodeList quizNodes = actTrackers.item(i).getChildNodes();
                for (int j = 0; j < quizNodes.getLength(); j++) {
                    NamedNodeMap quizAttrs = quizNodes.item(j).getAttributes();
                    float maxscore = Float.parseFloat(quizAttrs.getNamedItem(NODE_MAXSCORE).getTextContent());
                    float score = Float.parseFloat(quizAttrs.getNamedItem(NODE_SCORE).getTextContent());

                    String event = "";
                    try {
                        event = quizAttrs.getNamedItem(NODE_EVENT).getTextContent();
                    } catch (NullPointerException npe) {
                        Log.d(TAG, "Event node not found", npe);
                    }

                    int points = 0;
                    try {
                        points = Integer.parseInt(quizAttrs.getNamedItem(NODE_POINTS).getTextContent());
                    } catch (NumberFormatException nfe) {
                        Log.d(TAG, "Points node not an integer", nfe);
                    } catch (NullPointerException npe) {
                        Log.d(TAG, "Points node not found", npe);
                    }

                    String submittedDateString = quizAttrs.getNamedItem(NODE_SUBMITTEDDATE).getTextContent();
                    DateTime sdt = DateUtils.DATETIME_FORMAT.parseDateTime(submittedDateString);

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
                    qa.setEvent(event);
                    qa.setPoints(points);
                    quizAttempts.add(qa);
                }
            }
        }
        return quizAttempts;
    }
}
