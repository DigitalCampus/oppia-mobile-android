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

import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.application.App;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.CompleteCourse;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.CourseMetaPage;
import org.digitalcampus.oppia.model.GamificationEvent;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.model.Media;
import org.digitalcampus.oppia.model.Section;
import org.xml.sax.Attributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

class CourseXMLHandler extends DefaultLexicalHandler implements IMediaXMLHandler {

    private static final String NODE_LANG = "lang";
    private static final String NODE_TITLE = "title";
    private static final String NODE_PRIORITY = "priority";
    private static final String NODE_ID = "id";
    private static final String NODE_DESCRIPTION = "description";
    private static final String NODE_VERSIONID = "versionid";
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
    private static final String NODE_FILESIZE = "filesize";
    private static final String NODE_SECTION = "section";
    private static final String NODE_META = "meta";
    private static final String NODE_SEQUENCING = "sequencing";
    private static final String NODE_GAMIFICATION = "gamification";
    private static final String NODE_EVENT = "event";
    private static final String ATTR_NAME = "name";

    private long courseId;
    private long userId;
    private DbHelper db;

    private double courseVersionId;
    private String courseIcon;
    private String courseSequencingMode;
    private int coursePriority;
    private ArrayList<Lang> courseDescriptions = new ArrayList<>();
    private ArrayList<Lang> courseTitles = new ArrayList<>();
    private ArrayList<GamificationEvent> courseGamification = new ArrayList<>();
    private ArrayList<Lang> courseLangs = new ArrayList<>();
    private ArrayList<Activity> courseBaseline = new ArrayList<>();
    private ArrayList<Media> courseMedia = new ArrayList<>();
    private ArrayList<Section> sections = new ArrayList<>();
    private ArrayList<CourseMetaPage> courseMetaPages = new ArrayList<>();

    public CourseXMLHandler(long courseId, long userId, DbHelper db){
        this.courseId = courseId;
        this.userId = userId;
        this.db = db;
    }

    public String getCourseImage() { return courseIcon; }
    public List<Media> getCourseMedia() {
        return courseMedia;
    }

    //Vars for traversing the tree
    private Stack<String> parentElements = new Stack<>();

    //Temporary vars
    private Section currentSection;
    private String currentLang;
    private String currentGamifEvent;
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
    private ArrayList<GamificationEvent> actGamification;

    private ArrayList<GamificationEvent> currentGamification = new ArrayList<>();
    private ArrayList<Media> currentMedia = new ArrayList<>();

    @Override
    public void startElement(String aUri, String aLocalName,String aQName, Attributes aAttributes) {
        chars.setLength(0);

        if (NODE_SECTION.equals(aQName)) {
            currentSection = new Section();
            sectTitles = new ArrayList<>();
            currentSection.setOrder(Integer.parseInt(aAttributes.getValue(NODE_ORDER)));
            parentElements.push(NODE_SECTION);
        }
        else if (NODE_ACTIVITY.equals(aQName)){
            currentActivity = new Activity();
            currentActivity.setCourseId(courseId);
            currentActivity.setDigest(aAttributes.getValue(NODE_DIGEST));
            currentActivity.setActType(aAttributes.getValue(NODE_TYPE));
            currentActivity.setActId(Integer.parseInt(aAttributes.getValue(NODE_ORDER)));
            actTitles = new ArrayList<>();
            actLocations = new ArrayList<>();
            actContents = new ArrayList<>();
            actDescriptions = new ArrayList<>();
            actMedia = new ArrayList<>();
            actGamification = new ArrayList<>();
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
        }
        else if (NODE_FILE.equals(aQName)) {
            Media m = new Media();
            m.setFilename(aAttributes.getValue(NODE_FILENAME));
            m.setDownloadUrl(aAttributes.getValue(NODE_DOWNLOAD_URL));
            m.setDigest(aAttributes.getValue(NODE_DIGEST));
            String mediaLength = aAttributes.getValue(NODE_LENGTH);
            String mediaFilesize = aAttributes.getValue(NODE_FILESIZE);
            m.setLength(mediaLength==null?0:Integer.parseInt(mediaLength));
            m.setFileSize(mediaFilesize == null ? 0 : Double.parseDouble(mediaFilesize));
            currentMedia.add(m);
        }
        else if (NODE_META.equals(aQName)){
            parentElements.push(NODE_META);
        }
        else if (NODE_MEDIA.equals(aQName)){
            parentElements.push(NODE_MEDIA);
        }
        else if (NODE_PAGE.equals(aQName)) {
            currentPage = new CourseMetaPage();
            pageTitles = new ArrayList<>();
            pageLocations = new ArrayList<>();
            currentPage.setId(Integer.parseInt(aAttributes.getValue(NODE_ID)));
            parentElements.push(NODE_PAGE);
        }
        else if (NODE_GAMIFICATION.equals(aQName)){
            parentElements.push(NODE_GAMIFICATION);
        }
        else if (NODE_EVENT.equals(aQName)){
            currentGamifEvent = aAttributes.getValue(ATTR_NAME);
        }
    }

    @Override
    public void endElement(String aUri, String aLocalName, String aQName) {

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
                courseTitles.add(new Lang(currentLang==null? App.DEFAULT_LANG:currentLang, chars.toString()));
            }
            else if (NODE_PAGE.equals(parentElements.peek())){
                pageTitles.add(new Lang(currentLang==null? App.DEFAULT_LANG:currentLang, chars.toString()));
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

            if (NODE_ACTIVITY.equals(parentElements.peek())){
                actDescriptions.add(new Lang(currentLang, chars.toString()));
            }
            else if (NODE_META.equals(parentElements.peek())){
                courseDescriptions.add(new Lang(currentLang==null? App.DEFAULT_LANG:currentLang, chars.toString()));
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
        else if (NODE_SEQUENCING.equals(aQName)){
            if (chars.length() <= 0) return;

            if (NODE_META.equals(parentElements.peek())) {
                courseSequencingMode = chars.toString();
            }
        }
        else if (NODE_ACTIVITY.equals(aQName)){

            currentActivity.setTitles(actTitles);
            currentActivity.setDescriptions(actDescriptions);
            currentActivity.setLocations(actLocations);
            currentActivity.setContents(actContents);
            currentActivity.setMedia(actMedia);
            currentActivity.setGamificationEvents(actGamification);
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
        else if (NODE_MEDIA.equals(aQName)){
            parentElements.pop();
            if (!parentElements.empty() && NODE_ACTIVITY.equals(parentElements.peek())){
                actMedia = new ArrayList<>();
                actMedia.addAll(currentMedia);
            }
            else{
                courseMedia.addAll(currentMedia);
            }
            currentMedia.clear();
        }
        else if (NODE_META.equals(aQName)){
            parentElements.pop();
        }
        else if (NODE_LANG.equals(aQName)){
            if (chars.length() <= 0) return;
            courseLangs.add(new Lang(chars.toString(), ""));
        }
        else if (NODE_PAGE.equals(aQName)){
            for (Lang title : pageTitles){
                for (Lang location : pageLocations){
                    if (title.getLanguage().equals(location.getLanguage())){
                        title.setLocation(location.getContent());
                        currentPage.addLang(title);
                    }
                }
            }
            courseMetaPages.add(currentPage);
            parentElements.pop();
        }
        else if (NODE_GAMIFICATION.equals(aQName)){
            parentElements.pop();
            if (NODE_ACTIVITY.equals(parentElements.peek())){
                actGamification = new ArrayList<>();
                actGamification.addAll(currentGamification);
            }
            else{
                courseGamification.addAll(currentGamification);
            }
            currentGamification.clear();
        }
        else if (NODE_EVENT.equals(aQName)){
            if (chars.length() <= 0) return;

            int points = Integer.parseInt(chars.toString());
            GamificationEvent event = new GamificationEvent();
            event.setEvent(currentGamifEvent);
            event.setPoints(points);
            currentGamification.add(event);
        }
    }

    public CompleteCourse getCourse(String root) {
        CompleteCourse c = new CompleteCourse(root);
        c.setVersionId(courseVersionId);
        c.setImageFile(courseIcon);
        c.setPriority(coursePriority);
        c.setTitles(courseTitles);
        c.setLangs(courseLangs);
        c.setDescriptions(courseDescriptions);
        c.setBaselineActivities(courseBaseline);
        c.setMedia(courseMedia);
        c.setMetaPages(courseMetaPages);
        c.setSections(sections);
        c.setGamification(courseGamification);

        if ((courseSequencingMode!=null) && (courseSequencingMode.equals(Course.SEQUENCING_MODE_COURSE) ||
                courseSequencingMode.equals(Course.SEQUENCING_MODE_SECTION) || courseSequencingMode.equals(Course.SEQUENCING_MODE_NONE))){
            c.setSequencingMode(courseSequencingMode);
        }

        return c;
    }
}
