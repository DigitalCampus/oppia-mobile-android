package org.digitalcampus.mtrain.listener;

public interface DownloadModuleListener {
	void downloadComplete();
    void progressUpdate(String msg);
}
