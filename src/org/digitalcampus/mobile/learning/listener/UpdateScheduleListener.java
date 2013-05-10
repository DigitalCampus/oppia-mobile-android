package org.digitalcampus.mobile.learning.listener;

import org.digitalcampus.mobile.learning.model.DownloadProgress;
import org.digitalcampus.mobile.learning.task.Payload;

public interface UpdateScheduleListener {
	
	void updateComplete(Payload p);
    void updateProgressUpdate(DownloadProgress dp);

}
