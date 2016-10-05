package org.digitalcampus.oppia.service;

import android.content.Context;
import android.content.Intent;

import org.digitalcampus.oppia.activity.DownloadActivity;
import org.digitalcampus.oppia.adapter.CourseIntallViewAdapter;
import org.digitalcampus.oppia.model.Course;

public class CourseInstallerServiceDelegate {

    public void installCourse(Context context, Intent intent, Course courseSelected){
        intent.putExtra(CourseIntallerService.SERVICE_ACTION, CourseIntallerService.ACTION_DOWNLOAD);
        intent.putExtra(CourseIntallerService.SERVICE_URL, courseSelected.getDownloadUrl());
        intent.putExtra(CourseIntallerService.SERVICE_VERSIONID, courseSelected.getVersionId());
        intent.putExtra(CourseIntallerService.SERVICE_SHORTNAME, courseSelected.getShortname());
        context.startService(intent);

    }

    public void updateCourse(Context context, Intent intent, Course courseSelected){
        intent.putExtra(CourseIntallerService.SERVICE_ACTION, CourseIntallerService.ACTION_UPDATE);
        intent.putExtra(CourseIntallerService.SERVICE_SCHEDULEURL, courseSelected.getScheduleURI());
        intent.putExtra(CourseIntallerService.SERVICE_SHORTNAME, courseSelected.getShortname());
        context.startService(intent);
    }

    public void cancelCourseInstall(Context context, Intent intent, Course courseSelected){
        intent.putExtra(CourseIntallerService.SERVICE_ACTION, CourseIntallerService.ACTION_CANCEL);
        intent.putExtra(CourseIntallerService.SERVICE_URL, courseSelected.getDownloadUrl());
        context.startService(intent);
    }
}
