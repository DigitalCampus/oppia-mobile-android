package org.digitalcampus.oppia.model;


import android.content.Context;

import org.digitalcampus.oppia.listener.APIRequestListener;
import org.digitalcampus.oppia.task.APIUserRequestTask;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class CourseInstallRepository {

    public void getCourseList(Context ctx, String url) {
        APIUserRequestTask task = new APIUserRequestTask(ctx);
        task.setAPIRequestListener((APIRequestListener) ctx);
        task.execute(url);
    }

    public void refreshCourseList(Context ctx, List<CourseInstallViewAdapter> courses,
                                  JSONObject json, String storage, boolean showUpdatesOnly) throws JSONException{
        courses.addAll(CourseInstallViewAdapter.parseCoursesJSON(ctx, json, storage, showUpdatesOnly));
    }
}
