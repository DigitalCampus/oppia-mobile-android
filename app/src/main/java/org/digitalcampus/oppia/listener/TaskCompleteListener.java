package org.digitalcampus.oppia.listener;

import org.digitalcampus.oppia.task.Payload;

/**
 * Created by Alberto on 23/06/2016.
 */
public interface TaskCompleteListener {
    public void onComplete(Payload response);
}
