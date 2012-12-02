package org.digitalcampus.mobile.learning.listener;

import org.digitalcampus.mobile.learning.task.Payload;

public interface ScanMediaListener {
	
	void scanStart();
    void scanProgressUpdate(String msg);
    void scanComplete(Payload response);

}
