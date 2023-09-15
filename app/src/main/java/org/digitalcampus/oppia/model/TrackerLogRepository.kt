package org.digitalcampus.oppia.model

import android.content.Context
import org.digitalcampus.oppia.database.DbHelper

class TrackerLogRepository {

    fun getLastTrackerDatetime(context: Context): String {
        val db = DbHelper.getInstance(context)
        //        User user = db.getUser(SessionManager.getUsername(context)); #reminders-multi-user
        return db.getLastTrackerDatetime(-1 /*user.getUserId()*/)
    }
}