package org.digitalcampus.oppia.listener;

import org.digitalcampus.oppia.task.result.BasicResult;

/**
 * Created by Joseba on 03/06/2015.
 */
public interface DeleteCourseListener {
    void onCourseDeletionComplete(BasicResult result);
}
