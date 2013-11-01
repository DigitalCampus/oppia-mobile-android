package org.digitalcampus.oppia.listener;

import org.digitalcampus.oppia.task.Payload;

public interface UpgradeListener {

	void upgradeComplete(Payload p);
    void upgradeProgressUpdate(String s);
}
