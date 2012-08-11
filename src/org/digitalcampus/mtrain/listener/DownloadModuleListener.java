package org.digitalcampus.mtrain.listener;

public interface DownloadModuleListener {
	void downloadComplete();
    void downloadProgressUpdate(String msg);
}
