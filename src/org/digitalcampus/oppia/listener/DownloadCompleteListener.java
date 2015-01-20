package org.digitalcampus.oppia.listener;

import org.digitalcampus.oppia.task.Payload;

/**
 * Created by Joseba on 09/12/2014.
 */
public interface DownloadCompleteListener {
    public abstract void onComplete(Payload p);
}
