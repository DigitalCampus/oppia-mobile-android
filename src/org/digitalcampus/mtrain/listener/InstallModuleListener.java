package org.digitalcampus.mtrain.listener;

public interface InstallModuleListener {
	void installComplete();
    void progressUpdate(String msg);
}
