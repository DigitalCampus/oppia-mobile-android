package org.digitalcampus.oppia.listener;

import org.digitalcampus.oppia.task.Payload;

public interface PreloadAccountsListener {
    void onPreloadAccountsComplete(Payload p);
}
