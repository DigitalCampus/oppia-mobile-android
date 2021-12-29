package org.digitalcampus.oppia.listener;

import org.digitalcampus.oppia.task.result.BasicResult;

public interface ExportActivityListener {
    void onExportComplete(BasicResult result);
}
