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

package org.digitalcampus.oppia.model;

import org.digitalcampus.oppia.api.Paths;
import org.digitalcampus.oppia.application.App;
import org.digitalcampus.oppia.exception.CourseNotFoundException;
import org.digitalcampus.oppia.exception.GamificationEventNotFound;
import org.digitalcampus.oppia.utils.storage.Storage;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Course extends MultiLangInfoModel implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 4412987572522420704L;
    public static final String SEQUENCING_MODE_NONE = "none";
    public static final String SEQUENCING_MODE_SECTION = "section";
    public static final String SEQUENCING_MODE_COURSE = "course";

    public static final String TAG = Course.class.getSimpleName();
    private int courseId;
    private String shortname;
    private Double versionId;
    private boolean installed;
    private boolean toUpdate;
    private String downloadUrl;
    private String imageFile;
    private List<Media> media = new ArrayList<>();
    private List<CourseMetaPage> metaPages = new ArrayList<>();
    private boolean isDraft = false;
    private int priority = 0;
    private int noActivities = 0;
    private int noActivitiesCompleted = 0;
    private String sequencingMode = SEQUENCING_MODE_NONE;
    private List<GamificationEvent> gamificationEvents = new ArrayList<>();

    private String root;

    public Course() {
    }

    public Course(String root) {
        this.root = root;
    }

    public boolean validate() throws CourseNotFoundException {
        File courseXML = new File(this.getCourseXMLLocation());
        if (!courseXML.exists()) {
            throw new CourseNotFoundException();
        } else {
            return true;
        }
    }

    public List<Media> getMedia() {
        return media;
    }

    public void setMedia(List<Media> media) {
        this.media = media;
    }

    public String getImageFile() {
        return imageFile;
    }

    public String getImageFileFromRoot() {
        return this.root + File.separator + Storage.APP_COURSES_DIR_NAME + File.separator + this.getShortname() + File.separator + imageFile;
    }

    public void setImageFile(String imageFile) {
        this.imageFile = imageFile;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getTrackerLogUrl() {
        return String.format(Paths.COURSE_ACTIVITY_PATH, this.getShortname());
    }

    public Double getVersionId() {
        return versionId;
    }

    public void setVersionId(Double versionId) {
        this.versionId = versionId;
    }

    public boolean isInstalled() {
        return installed;
    }

    public void setInstalled(boolean installed) {
        this.installed = installed;
    }

    public boolean isToUpdate() {
        return toUpdate;
    }

    public void setToUpdate(boolean toUpdate) {
        this.toUpdate = toUpdate;
    }

    public float getProgressPercent() {
        // prevent divide by zero errors
        if (this.noActivities != 0) {
            return (float) this.noActivitiesCompleted * 100 / (float) this.noActivities;
        } else {
            return 0;
        }
    }

    public String getShortname() {
        return shortname.toLowerCase(Locale.US);
    }

    public void setShortname(String shortname) {
        this.shortname = shortname.toLowerCase(Locale.US);
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public String getLocation() {
        return this.root + File.separator + Storage.APP_COURSES_DIR_NAME + File.separator + this.getShortname() + File.separator;

    }

    public String getCourseXMLLocation() {
        return this.root + File.separator + Storage.APP_COURSES_DIR_NAME + File.separator + this.getShortname() + File.separator + App.COURSE_XML;
    }

    public void setMetaPages(List<CourseMetaPage> ammp) {
        this.metaPages = ammp;
    }

    public List<CourseMetaPage> getMetaPages() {
        return this.metaPages;
    }

    public CourseMetaPage getMetaPage(int id) {
        for (CourseMetaPage mmp : this.metaPages) {
            if (id == mmp.getId()) {
                return mmp;
            }
        }
        return null;
    }

    public boolean isDraft() {
        return isDraft;
    }

    public void setDraft(boolean isDraft) {
        this.isDraft = isDraft;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getNoActivities() {
        return noActivities;
    }

    public void setNoActivities(int noActivities) {
        this.noActivities = noActivities;
    }

    public int getNoActivitiesCompleted() {
        return noActivitiesCompleted;
    }

    public void setNoActivitiesCompleted(int noActivitiesCompleted) {
        this.noActivitiesCompleted = noActivitiesCompleted;
    }

    public String getSequencingMode() {
        return sequencingMode;
    }

    public void setSequencingMode(String sequencingMode) {
        this.sequencingMode = sequencingMode;
    }

    public static String getLocalFilename(String shortname, Double versionID) {
        return shortname + "-" + String.format("%.0f", versionID) + ".zip";
    }

    public void setGamificationEvents(List<GamificationEvent> events) {
        gamificationEvents = events;
    }

    public GamificationEvent findGamificationEvent(String event) throws GamificationEventNotFound {
        for (GamificationEvent ge : gamificationEvents) {
            if (ge.getEvent().equals(event)) {
                return ge;
            }
        }
        throw new GamificationEventNotFound(event);
    }
}
