package org.digitalcampus.oppia.listener

import org.digitalcampus.oppia.task.result.BasicResult

interface ExportActivityListener {
    fun onExportComplete(result: BasicResult?)
}