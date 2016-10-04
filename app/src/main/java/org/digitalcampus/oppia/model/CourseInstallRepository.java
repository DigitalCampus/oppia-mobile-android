package org.digitalcampus.oppia.model;


import android.content.Context;

import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.adapter.CourseIntallViewAdapter;
import org.digitalcampus.oppia.listener.APIRequestListener;
import org.digitalcampus.oppia.task.APIUserRequestTask;
import org.digitalcampus.oppia.task.Payload;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CourseInstallRepository {

    public void getCourseList(Context ctx, String url) {
        APIUserRequestTask task = new APIUserRequestTask(ctx);
        Payload p = new Payload(url);
        task.setAPIRequestListener((APIRequestListener) ctx);
        task.execute(p);
    }

    public void refreshCourseList(Context ctx, ArrayList<CourseIntallViewAdapter> courses,
                                  JSONObject json, String storage, boolean showUpdatesOnly) throws JSONException{
        courses.addAll(CourseIntallViewAdapter.parseCoursesJSON(ctx, json, storage, showUpdatesOnly));
    }
}
