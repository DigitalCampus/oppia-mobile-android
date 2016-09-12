package org.digitalcampus.oppia.model;

import android.content.Context;

import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.utils.xmlreaders.IMediaXMLHandler;

import java.util.ArrayList;

public class CompleteCourse extends Course implements IMediaXMLHandler{

    private ArrayList<Activity> baseline = new ArrayList<>();
    private ArrayList<Section> sections = new ArrayList<>();
    private ArrayList<CourseMetaPage> metaPages = new ArrayList<>();

    public CompleteCourse(){
        super("");
    }
    public CompleteCourse(String root) {
        super(root);
    }

    public void setBaselineActivities(ArrayList<Activity> baseline) {
        this.baseline = baseline;
    }
    public ArrayList<Activity> getBaselineActivities() {
        return baseline;
    }


    public void setSections(ArrayList<Section> sections) { this.sections = sections; }
    public ArrayList<Section> getSections() {
        return sections;
    }

    public Section getSection(int order){
        for (Section section : sections){
            if (section.getOrder() == order) return section;
        }
        return null;
    }

    public void updateCourseActivity(Context ctx){

        DbHelper db = DbHelper.getInstance(ctx);
        long userId = db.getUserId(SessionManager.getUsername(ctx));

        for (Section section : sections){
            for (Activity activity : section.getActivities()){
                activity.setCompleted(db.activityCompleted(getCourseId(), activity.getDigest(), userId));
            }
        }
        for (Activity activity : baseline){
            activity.setAttempted(db.activityAttempted(getCourseId(), activity.getDigest(), userId));
        }
    }


    public ArrayList<Activity> getActivities(long courseId) {
        ArrayList<Activity> activities = new ArrayList<>();
        for (Section section : sections){
            for (Activity act : section.getActivities()){
                act.setCourseId(courseId);
                activities.add(act);
            }
        }
        return activities;
    }

    @Override
    public ArrayList<Media> getCourseMedia() {
        return getMedia();
    }
}
