package org.digitalcampus.oppia.model

import android.content.Context
import org.digitalcampus.oppia.listener.APIRequestListener
import org.digitalcampus.oppia.task.APIUserRequestTask
import org.json.JSONException
import org.json.JSONObject

class CourseInstallRepository {
    fun getCourseList(ctx: Context?, url: String?) {
        val task = APIUserRequestTask(ctx)
        task.setAPIRequestListener(ctx as APIRequestListener?)
        task.execute(url)
    }

    @Throws(JSONException::class)
    fun refreshCourseList(ctx: Context?, courses: MutableList<CourseInstallViewAdapter?>, json: JSONObject?, storage: String?, showUpdatesOnly: Boolean) {
        courses.addAll(CourseInstallViewAdapter.parseCoursesJSON(ctx, json, storage, showUpdatesOnly))
    }
}