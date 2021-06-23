package org.digitalcampus.oppia.model;

import android.content.Context;

import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.exception.UserNotFoundException;
import org.digitalcampus.oppia.utils.storage.Storage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TrackerLogRepository {


    public String getLastTrackerDatetime(Context context) throws Exception {
        DbHelper db = DbHelper.getInstance(context);
        User user = db.getUser(SessionManager.getUsername(context));
        return db.getLastTrackerDatetime(user.getUserId());
    }
}
