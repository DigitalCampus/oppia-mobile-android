package org.digitalcampus.oppia.service.courseinstall;

import android.content.Context;
import android.content.Intent;

import org.digitalcampus.oppia.model.Course;

public class CourseInstallerServiceDelegate {

    public void installCourse(Context context, Intent intent, Course courseSelected){
        intent.putExtra(CourseInstallerService.SERVICE_ACTION, CourseInstallerService.ACTION_DOWNLOAD);
        intent.putExtra(CourseInstallerService.SERVICE_URL, courseSelected.getDownloadUrl());
        intent.putExtra(CourseInstallerService.SERVICE_VERSIONID, courseSelected.getVersionId());
        intent.putExtra(CourseInstallerService.SERVICE_SHORTNAME, courseSelected.getShortname());
        context.startService(intent);

    }

    public void updateCourse(Context context, Intent intent, Course courseSelected){
        intent.putExtra(CourseInstallerService.SERVICE_ACTION, CourseInstallerService.ACTION_UPDATE);
        intent.putExtra(CourseInstallerService.SERVICE_SHORTNAME, courseSelected.getShortname());
        context.startService(intent);
    }

    public void cancelCourseInstall(Context context, Intent intent, Course courseSelected){
        intent.putExtra(CourseInstallerService.SERVICE_ACTION, CourseInstallerService.ACTION_CANCEL);
        intent.putExtra(CourseInstallerService.SERVICE_URL, courseSelected.getDownloadUrl());
        context.startService(intent);
    }
}
