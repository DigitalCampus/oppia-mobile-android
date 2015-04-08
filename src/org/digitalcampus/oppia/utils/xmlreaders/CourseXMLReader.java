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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.exception.InvalidXMLException;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.model.Media;
import org.digitalcampus.oppia.model.CourseMetaPage;
import org.digitalcampus.oppia.model.Section;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.*;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class CourseXMLReader {

	public static final String TAG = CourseXMLReader.class.getSimpleName();
	private Context ctx;
	private SharedPreferences prefs;

    private XMLReader reader;
    private CourseXMLHandler completeParseHandler;
    private CourseXMLHandler mediaParseHandler;
    private File courseXML;
    private long courseId;

    public CourseXMLReader(String filename, long courseId, Context ctx) throws InvalidXMLException{
        this.ctx = ctx;
        this.courseId = courseId;
        prefs = PreferenceManager.getDefaultSharedPreferences(this.ctx);
        courseXML = new File(filename);

        if (courseXML.exists()) {
            try {
                SAXParserFactory parserFactory  = SAXParserFactory.newInstance();
                SAXParser parser = parserFactory.newSAXParser();
                reader = parser.getXMLReader();

            } catch (ParserConfigurationException e) {
                throw new InvalidXMLException(e);
            } catch (SAXException e) {
                throw new InvalidXMLException(e);
            }
        } else {
            Log.d(TAG, "course XML not found at: " + filename);
            throw new InvalidXMLException("Course XML not found at: " + filename);
        }
    }

    private CourseXMLHandler getCompleteResponses(){
        if (completeParseHandler == null){
            if (courseXML.exists()) {
                try {
                    SAXParserFactory parserFactory  = SAXParserFactory.newInstance();
                    SAXParser parser = parserFactory.newSAXParser();
                    reader = parser.getXMLReader();
                    DbHelper db = new DbHelper(ctx);
                    long userId = db.getUserId(prefs.getString(PrefsActivity.PREF_USER_NAME, ""));
                    completeParseHandler = new CourseXMLHandler(courseId, userId, db);

                    reader.setContentHandler(completeParseHandler);
                    reader.setProperty("http://xml.org/sax/properties/lexical-handler", completeParseHandler);
                    InputStream in = new BufferedInputStream(new FileInputStream(courseXML));
                    reader.parse(new InputSource(in));

                } catch (Exception e) {
                    //TODO: register error
                    e.printStackTrace();
                }
            } else {
                Log.d(TAG, "course XML not found at: " + courseXML.getPath());
            }
        }
        return completeParseHandler;
    }

    private CourseXMLHandler getMetaResponses(){
        if (completeParseHandler != null){
            return completeParseHandler;
        }
        else{
            //create metaParseHandler
            return getCompleteResponses();
        }
    }

    private CourseXMLHandler getMediaResponses(){
        if (completeParseHandler != null){
            return completeParseHandler;
        }
        else{
            //create mediaParseHandler
            return getCompleteResponses();
        }
    }

	public ArrayList<Lang> getTitles(){ return getMetaResponses().getCourseTitles(); }
	public int getPriority(){ return getMetaResponses().getCoursePriority(); }
	public ArrayList<Lang> getDescriptions(){ return getMetaResponses().getCourseDescriptions(); }
	public ArrayList<Lang> getLangs(){ return getMetaResponses().getCourseLangs();	}
	public double getVersionId(){  return getMetaResponses().getCourseVersionId();	}
	public ArrayList<CourseMetaPage> getMetaPages(){ return getMetaResponses().getCourseMetaPages(); }
	public ArrayList<Activity> getBaselineActivities(long modId){ return getCompleteResponses().getCourseBaseline(); }
	public ArrayList<Media> getMedia(){ return getMediaResponses().getCourseMedia(); }
	public String getCourseImage(){ return getMetaResponses().getCourseImage(); }
    public ArrayList<Section> getSections(int modId){ return getCompleteResponses().getSections(); }

	/*
	 * This is used when installing a new course
	 * and so adding all the activities to the db
	 */
	public ArrayList<Activity> getActivities(long modId){
		ArrayList<Activity> activities = new ArrayList<Activity>();
        for (Section section : getCompleteResponses().getSections()){
            for (Activity act : section.getActivities()){
                act.setCourseId(modId);
                activities.add(act);
            }
        }
        return activities;
	}

	public Section getSection(int order){

        for (Section section : getCompleteResponses().getSections()){
            if (section.getOrder() == order) return section;
        }
        return null;
	}

}
