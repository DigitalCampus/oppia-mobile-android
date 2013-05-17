package org.digitalcampus.mobile.learning.listener;

import org.digitalcampus.mobile.learning.task.Payload;

public interface UpgradeListener {

	void upgradeComplete(Payload p);
    void upgradeProgressUpdate(String s);
}
