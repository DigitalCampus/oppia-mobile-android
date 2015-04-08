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

package org.digitalcampus.oppia.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.DatabaseManager;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.exception.InvalidXMLException;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.model.Media;
import org.digitalcampus.oppia.model.CourseMetaPage;
import org.digitalcampus.oppia.model.Section;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.xml.sax.*;
import org.xml.sax.ext.*;
import org.xml.sax.helpers.*;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class CourseXMLReader {

	public static final String TAG = CourseXMLReader.class.getSimpleName();
	private Document document;
	private Context ctx;
	private SharedPreferences prefs;
	
	private static final String NODE_LANG = "lang";
	private static final String NODE_TITLE = "title";
	private static final String NODE_PRIORITY = "priority";
	private static final String NODE_ID = "id";
	private static final String NODE_DESCRIPTION = "description";
	private static final String NODE_VERSIONID = "versionid";
	private static final String NODE_LANGS = "langs";
	private static final String NODE_PAGE = "page";
	private static final String NODE_LOCATION = "location";
	private static final String NODE_ORDER = "order";
	private static final String NODE_TYPE = "type";
	private static final String NODE_DIGEST = "digest";
	private static final String NODE_IMAGE = "image";
	private static final String NODE_FILENAME = "filename";
	private static final String NODE_LENGTH = "length";
	private static final String NODE_ACTIVITY = "activity";
	private static final String NODE_CONTENT = "content";
	private static final String NODE_MEDIA = "media";
	private static final String NODE_DOWNLOAD_URL = "download_url";
	private static final String NODE_FILE = "file";
	private static final String NODE_ACTIVITIES = "activities";
	private static final String NODE_FILESIZE = "filesize";
    private static final String NODE_SECTION = "section";
    private static final String NODE_META = "meta";

    private XMLReader reader;
    private CourseHandler handler;

    public CourseXMLReader(String filename, long courseId, Context ctx) throws InvalidXMLException{
        this.ctx = ctx;
        prefs = PreferenceManager.getDefaultSharedPreferences(this.ctx);
        File courseXML = new File(filename);

        if (courseXML.exists()) {
            try {
                SAXParserFactory parserFactory  = SAXParserFactory.newInstance();
                SAXParser parser = parserFactory.newSAXParser();
                reader = parser.getXMLReader();
                DbHelper db = new DbHelper(ctx);
                long userId = db.getUserId(prefs.getString(PrefsActivity.PREF_USER_NAME, ""));
                handler = new CourseHandler(courseId, userId, db);

                reader.setContentHandler(handler);
                reader.setProperty("http://xml.org/sax/properties/lexical-handler", handler);
                InputStream in = new BufferedInputStream(new FileInputStream(courseXML));
                reader.parse(new InputSource(in));

            } catch (ParserConfigurationException e) {
                throw new InvalidXMLException(e);
            } catch (SAXException e) {
                throw new InvalidXMLException(e);
            } catch (IOException e) {
                throw new InvalidXMLException(e);
            }
        } else {
            Log.d(TAG, "course XML not found at: " + filename);
            throw new InvalidXMLException("Course XML not found at: " + filename);
        }
    }

    private static class CourseHandler extends DefaultLexicalHandler {

        private long courseId;
        private long userId;
        private DbHelper db;

        private double courseVersionId;
        private String courseIcon;
        private int coursePriority;
        private ArrayList<Lang> courseDescriptions = new ArrayList<Lang>();
        private ArrayList<Lang> courseTitles = new ArrayList<Lang>();
        private ArrayList<Lang> courseLangs = new ArrayList<Lang>();
        private ArrayList<Activity> courseBaseline = new ArrayList<Activity>();
        private ArrayList<Media> courseMedia = new ArrayList<Media>();
        private ArrayList<Section> sections = new ArrayList<Section>();
        private ArrayList<CourseMetaPage> courseMetaPages = new ArrayList<CourseMetaPage>();

        //Vars for traversing the tree
        private Stack<String> parentElements = new Stack<String>();

        //Temporary vars
        private Section currentSection;
        private String currentLang;
        private Activity currentActivity;
        private ArrayList<Lang> sectTitles;

        private CourseMetaPage currentPage;
        private ArrayList<Lang> pageTitles;
        private ArrayList<Lang> pageLocations;

        private ArrayList<Lang> actTitles;
        private ArrayList<Lang> actLocations;
        private ArrayList<Lang> actContents;
        private ArrayList<Lang> actDescriptions;
        private ArrayList<Media> actMedia;

        public CourseHandler(long courseId, long userId, DbHelper db){
            this.courseId = courseId;
            this.userId = userId;
            this.db = db;
        }

        @Override
        public void startElement(String aUri, String aLocalName,String aQName, Attributes aAttributes) throws SAXException {
            chars.setLength(0);

            if (NODE_SECTION.equals(aQName)) {
                currentSection = new Section();
                sectTitles = new ArrayList<Lang>();
                currentSection.setOrder(Integer.parseInt(aAttributes.getValue(NODE_ORDER)));
                parentElements.push(NODE_SECTION);
            }
            else if (NODE_ACTIVITY.equals(aQName)){
                currentActivity = new Activity();
                currentActivity.setCourseId(courseId);
                currentActivity.setDigest(aAttributes.getValue(NODE_DIGEST));
                currentActivity.setActType(aAttributes.getValue(NODE_TYPE));
                currentActivity.setActId(Integer.parseInt(aAttributes.getValue(NODE_ORDER)));
                actTitles = new ArrayList<Lang>();
                actLocations = new ArrayList<Lang>();
                actContents = new ArrayList<Lang>();
                actDescriptions = new ArrayList<Lang>();
                actMedia = new ArrayList<Media>();
                parentElements.push(NODE_ACTIVITY);

            }
            else if (NODE_TITLE.equals(aQName)) {
                currentLang = aAttributes.getValue(NODE_LANG);
            }
            else if (NODE_LOCATION.equals(aQName)) {
                currentLang = aAttributes.getValue(NODE_LANG);
                String mimeType = aAttributes.getValue(NODE_TYPE);
                if ((mimeType != null) && (NODE_ACTIVITY.equals(parentElements.peek()))){
                    currentActivity.setMimeType(mimeType);
                }
            }
            else if (NODE_CONTENT.equals(aQName)) {
                currentLang = aAttributes.getValue(NODE_LANG);
            }
            else if (NODE_DESCRIPTION.equals(aQName)) {
                currentLang = aAttributes.getValue(NODE_LANG);
            }
            else if (NODE_IMAGE.equals(aQName)) {
                if (NODE_ACTIVITY.equals(parentElements.peek())){
                    currentActivity.setImageFile(aAttributes.getValue(NODE_FILENAME));
                }
                else if (NODE_META.equals(parentElements.peek())){
                    courseIcon = aAttributes.getValue(NODE_FILENAME);
                }
                //else if (NODE_PAGE.equals(parentElements.peek())){
                    //Add metapage icon (there is no method?)
                //}
            }
            else if (NODE_FILE.equals(aQName)) {
                Media mediaObject = new Media();
                mediaObject.setFilename(aAttributes.getValue(NODE_FILENAME));
                mediaObject.setDownloadUrl(aAttributes.getValue(NODE_DOWNLOAD_URL));
                String mediaLength = aAttributes.getValue(NODE_LENGTH);
                mediaObject.setLength(mediaLength==null?0:Integer.parseInt(mediaLength));

                if (NODE_ACTIVITY.equals(parentElements.peek())){
                    actMedia.add(mediaObject);
                }
                else if (NODE_MEDIA.equals(parentElements.peek())){
                    courseMedia.add(mediaObject);
                }
            }
            else if (NODE_META.equals(aQName)){
                parentElements.push(NODE_META);
            }
            else if (NODE_MEDIA.equals(aQName)){
                parentElements.push(NODE_MEDIA);
            }
            else if (NODE_PAGE.equals(aQName)){
                currentPage = new CourseMetaPage();
                pageTitles = new ArrayList<Lang>();
                pageLocations = new ArrayList<Lang>();
                currentPage.setId(Integer.parseInt(aAttributes.getValue(NODE_ID)));
                parentElements.push(NODE_PAGE);
            }
        }

        @Override
        public void endElement(String aUri, String aLocalName, String aQName) throws SAXException {

            if (NODE_SECTION.equals(aQName)){
                currentSection.setTitles(sectTitles);
                sections.add(currentSection);
                parentElements.pop();
            }
            else if (NODE_TITLE.equals(aQName)){
                if (chars.length() <= 0) return;

                if (NODE_SECTION.equals(parentElements.peek())){
                    sectTitles.add(new Lang(currentLang, chars.toString()));
                }
                else if (NODE_ACTIVITY.equals(parentElements.peek())){
                    actTitles.add(new Lang(currentLang, chars.toString()));
                }
                else if (NODE_META.equals(parentElements.peek())){
                    courseTitles.add(new Lang(currentLang==null?MobileLearning.DEFAULT_LANG:currentLang, chars.toString()));
                }
                else if (NODE_PAGE.equals(parentElements.peek())){
                    pageTitles.add(new Lang(currentLang==null?MobileLearning.DEFAULT_LANG:currentLang, chars.toString()));
                }
            }
            else if (NODE_LOCATION.equals(aQName)){
                if (chars.length() <= 0) return;

                if (NODE_ACTIVITY.equals(parentElements.peek())){
                    actLocations.add(new Lang(currentLang, chars.toString()));
                }
                else if (NODE_PAGE.equals(parentElements.peek())){
                    pageLocations.add(new Lang(currentLang, chars.toString()));
                }
            }
            else if (NODE_CONTENT.equals(aQName)){
                if ((chars.length() > 0) && (NODE_ACTIVITY.equals(parentElements.peek()))){
                    actContents.add(new Lang(currentLang, chars.toString()));
                }
            }
            else if (NODE_DESCRIPTION.equals(aQName)){
                if (chars.length() <= 0) return;

                else if (NODE_ACTIVITY.equals(parentElements.peek())){
                    actDescriptions.add(new Lang(currentLang, chars.toString()));
                }
                else if (NODE_META.equals(parentElements.peek())){
                    courseDescriptions.add(new Lang(currentLang==null?MobileLearning.DEFAULT_LANG:currentLang, chars.toString()));
                }
            }
            else if (NODE_VERSIONID.equals(aQName)){
                if (chars.length() <= 0) return;
                courseVersionId = Double.parseDouble(chars.toString());
            }
            else if (NODE_PRIORITY.equals(aQName)){
                if (chars.length() <= 0) return;

                if (NODE_META.equals(parentElements.peek())) {
                    coursePriority = Integer.parseInt(chars.toString());
                }
            }
            else if (NODE_ACTIVITY.equals(aQName)){

                currentActivity.setTitles(actTitles);
                currentActivity.setDescriptions(actDescriptions);
                currentActivity.setLocations(actLocations);
                currentActivity.setContents(actContents);
                currentActivity.setMedia(actMedia);
                parentElements.pop();

                if (NODE_SECTION.equals(parentElements.peek())){
                    currentActivity.setSectionId(currentSection.getOrder());
                    currentActivity.setCompleted(db.activityCompleted((int)courseId, currentActivity.getDigest(), userId));
                    currentSection.addActivity(currentActivity);
                }
                else if (NODE_META.equals(parentElements.peek())){
                    currentActivity.setSectionId(0);
                    currentActivity.setAttempted(db.activityAttempted(courseId, currentActivity.getDigest(), userId));
                    courseBaseline.add(currentActivity);
                }
            }
            else if (NODE_META.equals(aQName) || NODE_MEDIA.equals(aQName)){
                parentElements.pop();
            }
            else if (NODE_LANG.equals(aQName)){
                if (chars.length() <= 0) return;
                courseLangs.add(new Lang(chars.toString(),""));
            }
            else if (NODE_PAGE.equals(aQName)){
                for (Lang title : pageTitles){
                    for (Lang location : pageLocations){
                        if (title.getLang().equals(location.getLang())){
                            title.setLocation(location.getContent());
                            currentPage.addLang(title);
                        }
                    }
                }
                courseMetaPages.add(currentPage);
                parentElements.pop();
            }
        }

    }

	public ArrayList<Lang> getTitles(){ return handler.courseTitles; }
	public int getPriority(){ return handler.coursePriority; }
	public ArrayList<Lang> getDescriptions(){ return handler.courseDescriptions; }
	public ArrayList<Lang> getLangs(){ return handler.courseLangs;	}
	public double getVersionId(){  return handler.courseVersionId;	}
	public ArrayList<CourseMetaPage> getMetaPages(){ return handler.courseMetaPages; }
	public ArrayList<Activity> getBaselineActivities(long modId){ return handler.courseBaseline; }
	public ArrayList<Media> getMedia(){ return handler.courseMedia; }
	public String getCourseImage(){ return handler.courseIcon; }
    public ArrayList<Section> getSections(int modId){ return handler.sections; }

	/*
	 * This is used when installing a new course
	 * and so adding all the activities to the db
	 */
	public ArrayList<Activity> getActivities(long modId){
		ArrayList<Activity> activities = new ArrayList<Activity>();
        for (Section section : handler.sections){
            for (Activity act : section.getActivities()){
                act.setCourseId(modId);
                activities.add(act);
            }
        }
        return activities;
	}

	public Section getSection(int order){

        for (Section section : handler.sections){
            if (section.getOrder() == order) return section;
        }
        return null;
	}

    static class DefaultLexicalHandler extends DefaultHandler implements LexicalHandler {

        protected StringBuilder chars;

        @Override
        public void startDocument() throws SAXException {
            chars = new StringBuilder();
        }
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException{
            chars.append(ch, start, length);
        }
        @Override
        public void comment(char[] aArg0, int aArg1, int aArg2) throws SAXException {}
        @Override
        public void endCDATA() throws SAXException {}
        @Override
        public void endDTD() throws SAXException {}
        @Override
        public void endEntity(String aName) throws SAXException {}
        @Override
        public void startCDATA() throws SAXException {}
        @Override
        public void startDTD(String aArg0, String aArg1, String aArg2) throws SAXException {}
        @Override
        public void startEntity(String aName) throws SAXException {}
    }
}
