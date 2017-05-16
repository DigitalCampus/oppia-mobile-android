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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.exception.InvalidXMLException;
import org.digitalcampus.oppia.model.CompleteCourse;
import org.digitalcampus.oppia.model.Media;
import org.xml.sax.SAXException;
import org.xml.sax.*;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.splunk.mint.Mint;

public class CourseXMLReader {

	public static final String TAG = CourseXMLReader.class.getSimpleName();
	private Context ctx;

    private XMLReader reader;
    private CourseXMLHandler completeParseHandler;
    private CourseMediaXMLHandler mediaParseHandler;
    private File courseXML;
    private long courseId;

    public enum ParseMode{
        COMPLETE, ONLY_META, ONLY_MEDIA
    }

    public CourseXMLReader(String filename, long courseId, Context ctx) throws InvalidXMLException{
        this.ctx = ctx;
        this.courseId = courseId;
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

    public boolean parse(ParseMode PARSE_MODE){
        if (courseXML.exists()) {
            try {
                if (PARSE_MODE == ParseMode.ONLY_MEDIA)
                    parseMedia();
                else
                    parseComplete();

            } catch (Exception e) {
                Mint.logException(e);
                e.printStackTrace();
                return false;
            }
        } else {
            Log.d(TAG, "course XML not found at: " + courseXML.getPath());
            return false;
        }
        return true;
    }

    private void parseComplete() throws ParserConfigurationException, SAXException, IOException {

        SAXParserFactory parserFactory  = SAXParserFactory.newInstance();
        SAXParser parser = parserFactory.newSAXParser();
        reader = parser.getXMLReader();
        DbHelper db = DbHelper.getInstance(ctx);
        long userId = db.getUserId(SessionManager.getUsername(ctx));
        completeParseHandler = new CourseXMLHandler(courseId, userId, db);

        reader.setContentHandler(completeParseHandler);
        reader.setProperty("http://xml.org/sax/properties/lexical-handler", completeParseHandler);
        InputStream in = new BufferedInputStream(new FileInputStream(courseXML));
        reader.parse(new InputSource(in));

    }

    private void parseMedia() throws ParserConfigurationException, SAXException, IOException {
        mediaParseHandler = new CourseMediaXMLHandler();
        SAXParserFactory parserFactory  = SAXParserFactory.newInstance();
        SAXParser parser = parserFactory.newSAXParser();
        reader = parser.getXMLReader();
        reader.setContentHandler(mediaParseHandler);
        reader.setProperty("http://xml.org/sax/properties/lexical-handler", mediaParseHandler);
        InputStream in = new BufferedInputStream(new FileInputStream(courseXML));
        reader.parse(new InputSource(in));
    }


    public CompleteCourse getParsedCourse(){
        if (completeParseHandler == null){
            parse(ParseMode.COMPLETE);
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String location = prefs.getString(PrefsActivity.PREF_STORAGE_LOCATION, "");
        return completeParseHandler.getCourse(location);
    }

    public IMediaXMLHandler getMediaResponses(){

        if (mediaParseHandler != null){
            return mediaParseHandler;
        }
        else if (completeParseHandler != null){
            return completeParseHandler;
        }
        else{
            parse(ParseMode.ONLY_MEDIA);
            return mediaParseHandler;
        }
    }

	public ArrayList<Media> getMedia(){ return getMediaResponses().getCourseMedia(); }

}
