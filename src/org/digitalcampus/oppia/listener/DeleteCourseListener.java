package org.digitalcampus.oppia.listener;

import org.digitalcampus.oppia.task.Payload;

/**
 * Created by Joseba on 03/06/2015.
 */
public interface DeleteCourseListener {
    void onCourseDeletionComplete(Payload response);
}
