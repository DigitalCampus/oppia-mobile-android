package org.digitalcampus.mobile.learning.listener;

public interface InstallModuleListener {
	
	void downloadComplete();
    void downloadProgressUpdate(String msg);
    
	void installComplete();
    void installProgressUpdate(String msg);
}
