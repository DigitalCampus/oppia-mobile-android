package org.digitalcampus.oppia.listener;

import org.digitalcampus.oppia.model.DownloadProgress;
import org.digitalcampus.oppia.task.Payload;

public interface UpdateScheduleListener {
	
	void updateComplete(Payload p);
    void updateProgressUpdate(DownloadProgress dp);

}
