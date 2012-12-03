package org.digitalcampus.mobile.learning.listener;

import org.digitalcampus.mobile.learning.model.DownloadProgress;

public interface DownloadMediaListener {
	
	void downloadStarting();
    void downloadProgressUpdate(DownloadProgress msg);
    void downloadComplete();
}
