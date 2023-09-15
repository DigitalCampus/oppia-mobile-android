package org.digitalcampus.oppia.model

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class OfflineCourseFile(val file: File) {
    enum class FileType { COURSE, MEDIA, INVALID }
    enum class Status { SELECTED, IMPORTING, IMPORTED }

    companion object {
        private const val MODULES_XML_PATH = ".*/module.xml"
        private const val MEDIA_REGEX = ".*\\.(?:mp4|m4v|mpeg|3gp|3gpp)$"
    }

    val type: FileType = getFileType(file)
    var status: Status = Status.SELECTED
        private set

    fun updateStatus(newStatus: Status) {
        status = newStatus
    }

    private fun getFileType(file: File): FileType {
        var fileType = FileType.INVALID
        try {
            val inputStream: InputStream = FileInputStream(file)
            val zipInputStream = ZipInputStream(inputStream)
            var entry: ZipEntry
            while (zipInputStream.nextEntry.also { entry = it } != null) {
                val name = entry.name
                if (name.matches(MODULES_XML_PATH.toRegex()) && !entry.isDirectory) {
                    fileType = FileType.COURSE
                    break
                }
                if (name.matches(MEDIA_REGEX.toRegex()) && !entry.isDirectory) {
                    fileType = FileType.MEDIA
                    break
                }
            }
            zipInputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return fileType
    }
}