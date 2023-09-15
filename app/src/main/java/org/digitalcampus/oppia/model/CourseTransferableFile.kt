package org.digitalcampus.oppia.model

import org.digitalcampus.oppia.task.ExportActivityTask
import org.digitalcampus.oppia.utils.DateUtils
import org.digitalcampus.oppia.utils.storage.FileUtils
import org.joda.time.format.DateTimeFormat
import java.io.File
import java.io.Serializable

class CourseTransferableFile(
    var shortname: String? = null,
    var versionId: Double? = null,
    var type: String = TYPE_COURSE_BACKUP,
    var filename: String? = null,
    var fileSize: Long = 0,
    var file: File? = null,
    var relatedMedia: List<String> = emptyList(),
    var relatedFilesize: Long = 0
) : Serializable {

    companion object {
        const val serialVersionUID = 123456789123456789L
        const val TYPE_COURSE_BACKUP = "backup"
        const val TYPE_COURSE_MEDIA = "media"
        const val TYPE_ACTIVITY_LOG = "activity"
    }

    var title: String? = null
        get() = field?.trim()

    val displayFileSize: String
        get() = FileUtils.readableFileSize(fileSize + relatedFilesize)

    val displayDateTimeFromFilename: String
        get() {
            val f = DateTimeFormat.forPattern(ExportActivityTask.activityTimestampFormat)
            val dateTime = f.parseDateTime(
                filename?.substringAfterLast('_')?.substringBeforeLast('.') ?: ""
            )
            return DateUtils.DISPLAY_DATETIME_FORMAT.print(dateTime)
        }


    override fun equals(other: Any?): Boolean {
        if (other is CourseTransferableFile) {
            return other.filename == filename
        }
        return false
    }

    override fun hashCode(): Int {
        return filename?.hashCode() ?: 0
    }

    val notificationName: String?
        get() {
            return when(type) {
                TYPE_COURSE_BACKUP -> shortname
                TYPE_ACTIVITY_LOG -> activityLogUsername + " log"
                else -> null
            }
        }

    private val activityLogUsername: String
        get() = filename?.substringBefore('_') ?: ""

    fun setTitleFromFilename() {
        if (type == TYPE_ACTIVITY_LOG) {
            title = activityLogUsername
        }
    }
}