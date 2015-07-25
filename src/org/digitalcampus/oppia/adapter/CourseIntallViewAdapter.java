package org.digitalcampus.oppia.adapter;

import org.digitalcampus.oppia.model.Course;

public class CourseIntallViewAdapter extends Course {

    public CourseIntallViewAdapter(String root) {
        super(root);
    }

    //Extension for UI purposes
    private boolean downloading;
    private boolean installing;
    private int progress;

    public boolean isDownloading() {
        return downloading;
    }
    public void setDownloading(boolean downloading) { this.downloading = downloading; }

    public boolean isInstalling() {
        return installing;
    }
    public void setInstalling(boolean installing) {
        this.installing = installing;
    }

    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }
}
