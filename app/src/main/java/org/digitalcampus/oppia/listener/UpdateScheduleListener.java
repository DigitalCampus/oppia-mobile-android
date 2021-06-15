package org.digitalcampus.oppia.listener;

import org.digitalcampus.oppia.model.DownloadProgress;
import org.digitalcampus.oppia.task.result.BasicResult;

public interface UpdateScheduleListener {
	
	void updateComplete(BasicResult result);
    void updateProgressUpdate(DownloadProgress dp);

}
