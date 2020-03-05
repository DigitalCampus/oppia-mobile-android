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
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;
import android.util.Log;

import com.splunk.mint.Mint;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.exception.InvalidXMLException;
import org.digitalcampus.oppia.model.CompleteCourse;
import org.digitalcampus.oppia.model.Media;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

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
        reader = XMLSecurityHelper.getSecureXMLReader();

        if (!courseXML.exists() || (reader == null)) {
            Log.d(TAG, "course XML not found at: " + filename);
            throw new InvalidXMLException("Course XML not found at: " + filename);
        }
    }


    public boolean parse(ParseMode parseMode) throws InvalidXMLException {
        if (courseXML.exists()) {
            try {
                if (parseMode == ParseMode.ONLY_MEDIA)
                    parseMedia();
                else
                    parseComplete();

            } catch (Exception e) {
                Mint.logException(e);
                Log.d(TAG, "Error loading course", e);
                throw new InvalidXMLException(e, ctx.getResources().getString(R.string.error_reading_xml));
            }
        } else {
            Log.d(TAG, "course XML not found at: " + courseXML.getPath());
            return false;
        }
        return true;
    }

    private void parseComplete() throws SAXException, IOException {

        DbHelper db = DbHelper.getInstance(ctx);
        long userId = db.getUserId(SessionManager.getUsername(ctx));
        completeParseHandler = new CourseXMLHandler(courseId, userId, db);

        reader.setContentHandler(completeParseHandler);
        reader.setProperty("http://xml.org/sax/properties/lexical-handler", completeParseHandler);
        InputStream in = new BufferedInputStream(new FileInputStream(courseXML));
        reader.parse(new InputSource(in));

    }

    private void parseMedia() throws SAXException, IOException {
        mediaParseHandler = new CourseMediaXMLHandler();
        reader.setContentHandler(mediaParseHandler);
        reader.setProperty("http://xml.org/sax/properties/lexical-handler", mediaParseHandler);
        InputStream in = new BufferedInputStream(new FileInputStream(courseXML));
        reader.parse(new InputSource(in));
    }


    public CompleteCourse getParsedCourse() throws InvalidXMLException {
        if (completeParseHandler == null){
            parse(ParseMode.COMPLETE);
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String location = prefs.getString(PrefsActivity.PREF_STORAGE_LOCATION, "");
        return completeParseHandler.getCourse(location);
    }

    public IMediaXMLHandler getMediaResponses() throws InvalidXMLException {

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

	public List<Media> getMedia() throws InvalidXMLException {
        return getMediaResponses().getCourseMedia();
	}

}
