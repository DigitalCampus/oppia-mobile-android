package org.digitalcampus.mtrain.listener;

public interface InstallModuleListener {
	
	void downloadComplete();
    void downloadProgressUpdate(String msg);
    
	void installComplete();
    void installProgressUpdate(String msg);
}
