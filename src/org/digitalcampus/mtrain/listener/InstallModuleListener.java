package org.digitalcampus.mtrain.listener;

public interface InstallModuleListener {
	void installComplete();
    void installProgressUpdate(String msg);
}
