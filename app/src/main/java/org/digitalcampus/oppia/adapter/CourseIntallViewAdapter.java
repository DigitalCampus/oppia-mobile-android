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

package org.digitalcampus.oppia.adapter;

import android.content.Context;

import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.service.courseinstall.CourseIntallerService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CourseIntallViewAdapter extends Course {

    private static final long serialVersionUID = -4251898143809197224L;

    public CourseIntallViewAdapter(String root) {
        super(root);
    }

    //Extension for UI purposes
    private boolean downloading;
    private boolean installing;
    private int progress;

    private String authorUsername;
    private String authorName;
    private String organisationName;

    private static final String JSON_PROPERTY_DESCRIPTION = "description";
    private static final String JSON_PROPERTY_TITLE = "title";
    private static final String JSON_PROPERTY_SHORTNAME = "shortname";
    private static final String JSON_PROPERTY_VERSION = "version";
    private static final String JSON_PROPERTY_URL = "url";
    private static final String JSON_PROPERTY_IS_DRAFT = "is_draft";
    private static final String JSON_PROPERTY_AUTHOR = "author";
    private static final String JSON_PROPERTY_USERNAME = "username";
    private static final String JSON_PROPERTY_SCHEDULE_URI = "schedule_uri";
    private static final String JSON_PROPERTY_SCHEDULE = "schedule";
    private static final String JSON_PROPERTY_ORGANISATION = "organisation";

    public boolean isDownloading() {
        return downloading;
    }

    public void setDownloading(boolean downloading) {
        this.downloading = downloading;
    }

    public boolean isInstalling() {
        return installing;
    }

    public void setInstalling(boolean installing) {
        this.installing = installing;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public void setAuthorUsername(String authorUsername) {
        this.authorUsername = authorUsername;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getDisplayAuthorName() {
        if ((authorName == null) && (authorUsername == null)) return null;

        /*** Previous code to display the author like "Name Surname (@username)"
        String displayName = authorName == null ? "" : authorName.trim();
        if (authorUsername != null) {
            if ( (authorName != null) && (authorName.trim().length() > 0))
                displayName += " (@" + authorUsername + ")";
            else
                displayName += authorUsername;
        }
        return displayName;
        */
        return authorName;
    }


    public static List<CourseIntallViewAdapter> parseCoursesJSON(
            Context ctx, JSONObject json, String location, boolean onlyAddUpdates)
            throws JSONException {

        ArrayList<String> downloadingCourses = CourseIntallerService.getTasksDownloading();
        ArrayList<CourseIntallViewAdapter> courses = new ArrayList<>();

        JSONArray coursesArray = json.getJSONArray(MobileLearning.SERVER_COURSES_NAME);
        for (int i = 0; i < coursesArray.length(); i++) {
            JSONObject json_obj = coursesArray.getJSONObject(i);
            CourseIntallViewAdapter course = new CourseIntallViewAdapter(location);

            ArrayList<Lang> titles = new ArrayList<>();
            JSONObject jsonTitles = json_obj.getJSONObject(JSON_PROPERTY_TITLE);
            Iterator<?> keys = jsonTitles.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                Lang l = new Lang(key, jsonTitles.getString(key));
                titles.add(l);
            }
            course.setTitles(titles);

            ArrayList<Lang> descriptions = new ArrayList<>();
            if (json_obj.has(JSON_PROPERTY_DESCRIPTION) && !json_obj.isNull(JSON_PROPERTY_DESCRIPTION)) {
                try {
                    JSONObject jsonDescriptions = json_obj.getJSONObject(JSON_PROPERTY_DESCRIPTION);
                    Iterator<?> dkeys = jsonDescriptions.keys();
                    while (dkeys.hasNext()) {
                        String key = (String) dkeys.next();
                        if (!jsonDescriptions.isNull(key)) {
                            Lang l = new Lang(key, jsonDescriptions.getString(key));
                            descriptions.add(l);
                        }
                    }
                    course.setDescriptions(descriptions);
                } catch (JSONException jsone) {
                    //do nothing
                }
            }

            course.setShortname(json_obj.getString(JSON_PROPERTY_SHORTNAME));
            course.setVersionId(json_obj.getDouble(JSON_PROPERTY_VERSION));
            course.setDownloadUrl(json_obj.getString(JSON_PROPERTY_URL));

            if (json_obj.has(JSON_PROPERTY_IS_DRAFT) && !json_obj.isNull(JSON_PROPERTY_IS_DRAFT))
                course.setDraft(json_obj.getBoolean(JSON_PROPERTY_IS_DRAFT));
            else
                course.setDraft(false);

            if (json_obj.has(JSON_PROPERTY_AUTHOR) && !json_obj.isNull(JSON_PROPERTY_AUTHOR))
                course.setAuthorName(json_obj.getString(JSON_PROPERTY_AUTHOR));

            if (json_obj.has(JSON_PROPERTY_USERNAME) && !json_obj.isNull(JSON_PROPERTY_USERNAME))
                course.setAuthorUsername(json_obj.getString(JSON_PROPERTY_USERNAME));

            DbHelper db = DbHelper.getInstance(ctx);
            course.setInstalled(db.isInstalled(course.getShortname()));
            course.setToUpdate(db.toUpdate(course.getShortname(), course.getVersionId()));
            if (json_obj.has(JSON_PROPERTY_SCHEDULE_URI)) {
                course.setScheduleVersionID(json_obj.getDouble(JSON_PROPERTY_SCHEDULE));
                course.setScheduleURI(json_obj.getString(JSON_PROPERTY_SCHEDULE_URI));
                course.setToUpdateSchedule(
                        db.toUpdateSchedule(course.getShortname(), course.getScheduleVersionID()));
            }

            if (json_obj.has(JSON_PROPERTY_ORGANISATION)) {
                try {
                    course.setOrganisationName(json.getString(JSON_PROPERTY_ORGANISATION));
                } catch (JSONException jsone) {
                    // do nothing
                }
            }

            if (downloadingCourses != null && downloadingCourses.contains(course.getDownloadUrl())) {
                course.setDownloading(true);
            }
            if (!onlyAddUpdates || course.isToUpdate()) {
                courses.add(course);
            }

        }

        return courses;
    }

    public String getOrganisationName() {
        return organisationName;
    }

    public void setOrganisationName(String organisationName) {
        this.organisationName = organisationName;
    }
}
