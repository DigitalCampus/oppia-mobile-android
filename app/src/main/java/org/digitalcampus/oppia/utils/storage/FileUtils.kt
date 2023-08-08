/*
 * This file is part of OppiaMobile - https://digital-campus.org/
 *
 * OppiaMobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OppiaMobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OppiaMobile. If not, see <http://www.gnu.org/licenses/>.
 */
package org.digitalcampus.oppia.utils.storage

import android.content.Context
import android.util.Log
import android.webkit.MimeTypeMap
import org.apache.commons.io.IOUtils
import org.digitalcampus.mobile.learning.R
import org.digitalcampus.oppia.analytics.Analytics
import org.digitalcampus.oppia.application.App
import org.digitalcampus.oppia.exception.CourseInstallException
import org.digitalcampus.oppia.utils.TextUtilsJava
import org.digitalcampus.oppia.utils.storage.Storage.getAvailableStorageSize
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.DataInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.security.MessageDigest
import java.text.DecimalFormat
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object FileUtils {

    val TAG = FileUtils::class.simpleName
    const val BUFFER_SIZE = 1024

    // This function converts the zip file into uncompressed files which are
    // placed in the destination directory
    // destination directory should be created first
    @JvmStatic
    @Throws(CourseInstallException::class)
    fun unzipFiles(context: Context, srcDirectory: String, srcFile: String, destDirectory: String) {
        // first make sure that all the arguments are valid and not null
        if (TextUtilsJava.isEmpty(srcDirectory)
                || TextUtilsJava.isEmpty(srcFile)
                || TextUtilsJava.isEmpty(destDirectory)) {
            throw CourseInstallException(context.getString(R.string.invalid_parameters))
        }

        // now make sure that these directories exist
        val sourceDirectory = File(srcDirectory)
        val sourceFile = File(srcDirectory, srcFile)
        val destinationDirectory = File(destDirectory)
        if (!sourceDirectory.exists()) {
            throw CourseInstallException(context.getString(R.string.source_dir_does_not_exist))
        }
        if (!sourceFile.exists()) {
            throw CourseInstallException(context.getString(R.string.source_file_does_not_exist))
        }
        if (!destinationDirectory.exists()) {
            throw CourseInstallException(context.getString(R.string.dest_dir_does_not_exist))
        }
        val availableStorage = getAvailableStorageSize(context)
        var uncompressedSize: Long = 0
        try {
            ZipInputStream(FileInputStream(sourceFile)).use { zipInputStream ->
                val buffer = ByteArray(4096)
                var zipEntry: ZipEntry?
                while (zipInputStream.nextEntry.also { zipEntry = it } != null) {
                    if (zipEntry?.isDirectory == false) {
                        var bytesRead: Int
                        while (zipInputStream.read(buffer).also { bytesRead = it } != -1) {
                            uncompressedSize += bytesRead.toLong()
                        }
                    }
                    zipInputStream.closeEntry()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            throw CourseInstallException(e.message)
        }
        Log.i(TAG, "unzipFiles: sizes: uncompressed: $uncompressedSize. available storage: $availableStorage")
        if (uncompressedSize > availableStorage) {
            throw CourseInstallException(context.getString(R.string.error_insufficient_storage_available))
        }
        try {
            FileInputStream(sourceFile).use { fis ->
                ZipInputStream(BufferedInputStream(fis)).use { zis ->

                    // now start with unzip process
                    var entry: ZipEntry?
                    while (zis.nextEntry.also { entry = it } != null) {
                        val f = entry?.name?.let { File(destDirectory, it) }
                        val canonicalPath = f?.canonicalPath
                        val fileDestDir = File(destDirectory)
                        val destDirCanonicalPath = fileDestDir.canonicalPath
                        if (canonicalPath?.startsWith(destDirCanonicalPath) == false) {
                            throw SecurityException("Suspect file: " + entry?.name
                                    + ". Possibility of trying to access parent directory")
                        }
                        entry?.let { createDirIfNeeded(destDirectory, it) }

                        var count: Int
                        val data = ByteArray(BUFFER_SIZE)

                        // write the file to the disk
                        if (f?.isDirectory == false) {
                            FileOutputStream(f).use { fos ->
                                BufferedOutputStream(fos, BUFFER_SIZE).use { dest ->
                                    while (zis.read(data, 0, BUFFER_SIZE).also { count = it } != -1) {
                                        dest.write(data, 0, count)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Analytics.logException(e)
            Log.d(TAG, "Exception:", e)
            throw CourseInstallException(e.message)
        }
    }

    @JvmStatic
    fun getDigestFromMessage(mDigest: MessageDigest): String {
        val digest = mDigest.digest()
        val resultMD5 = StringBuilder()
        for (aDigest in digest) {
            resultMD5.append(((aDigest.toInt() and 0xff) + 0x100).toString(16).substring(1))
        }
        return resultMD5.toString()
    }

    @JvmStatic
    fun zipFileAtPath(sourceFile: File, zipDestination: File): Boolean {
        val buffer = 2048
        Log.d(TAG, "Zipping $sourceFile into $zipDestination")
        try {
            FileOutputStream(zipDestination).use { dest ->
                ZipOutputStream(BufferedOutputStream(dest)).use { out ->
                    FileInputStream(zipDestination).use { fi ->
                        BufferedInputStream(fi, buffer).use { origin ->
                            if (sourceFile.isDirectory) {
                                sourceFile.parent?.let { zipSubFolder(out, sourceFile, it.length) }
                            } else {
                                val data = ByteArray(buffer)
                                val entry = ZipEntry(getLastPathComponent(zipDestination.path))
                                out.putNextEntry(entry)
                                var count: Int
                                while (origin.read(data, 0, buffer).also { count = it } != -1) {
                                    out.write(data, 0, count)
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: IOException) {
            Analytics.logException(e)
            Log.d(TAG, "Exception:", e)
            return false
        }
        return true
    }

    @Throws(IOException::class)
    private fun zipSubFolder(out: ZipOutputStream, folder: File,
                             basePathLength: Int) {
        val BUFFER = 2048
        Log.d(TAG, "Zipping folder " + folder.path)
        val fileList = folder.listFiles()
        for (file in fileList) {
            if (file.isDirectory) {
                zipSubFolder(out, file, basePathLength)
            } else {
                val unmodifiedFilePath = file.path
                FileInputStream(unmodifiedFilePath).use { fi ->
                    BufferedInputStream(fi, BUFFER).use { origin ->
                        val data = ByteArray(BUFFER)
                        val relativePath = unmodifiedFilePath.substring(basePathLength)
                        val entry = ZipEntry(relativePath)
                        out.putNextEntry(entry)
                        var count: Int
                        while (origin.read(data, 0, BUFFER).also { count = it } != -1) {
                            out.write(data, 0, count)
                        }
                    }
                }
            }
        }
    }

    private fun getLastPathComponent(filePath: String): String {
        val segments = filePath.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return if (segments.isEmpty()) "" else segments[segments.size - 1]
    }

    private fun createDirIfNeeded(destDirectory: String, entry: ZipEntry) {
        val name = entry.name
        if (name.contains(File.separator)) {
            val index = name.lastIndexOf(File.separator)
            val dirSequence = name.substring(0, index)
            val newDirs = File(destDirectory + File.separator + dirSequence)

            // create the directory
            newDirs.mkdirs()
        }
    }

    @JvmStatic
    fun cleanDir(dir: File): Boolean {
        if (dir.isDirectory) {
            val children = dir.list()
            for (dirFiles in children) {
                val fileToDelete = File(dir, dirFiles)
                val success = deleteDir(fileToDelete)
                if (!success) {
                    return false
                }
            }
        }
        return true
    }

    // Deletes all files and subdirectories under dir.
    // Returns true if all deletions were successful.
    // If a deletion fails, the method stops attempting to delete and returns
    // false.
    @JvmStatic
    fun deleteDir(dir: File): Boolean {
        return if (cleanDir(dir)) {
            // The directory is now empty so delete it
            dir.delete() //NOSONAR (Files.delete() is available from API 26)
        } else {
            false
        }
    }

    @JvmStatic
    fun dirSize(dir: File): Long {
        if (dir.exists() && dir.isDirectory) {
            var result: Long = 0
            val fileList = dir.listFiles()
            for (file in fileList) {
                result += if (file.isDirectory) {
                    dirSize(file)
                } else {
                    file.length()
                }
            }
            return result
        }
        return 0
    }

    @JvmStatic
    fun cleanUp(tempDir: File, path: String?): Boolean {
        deleteDir(tempDir)

        // delete zip file from download dir
        val zip = File(path)
        return zip.delete() //NOSONAR (Files.delete() is available from API 26)
    }

    @JvmStatic
    @Throws(IOException::class)
    fun readFile(file: String?): String {
        val fstream = FileInputStream(file)
        return readFile(fstream)
    }

    @JvmStatic
    @Throws(IOException::class)
    fun readFile(file: File?): String {
        val fstream = FileInputStream(file)
        return readFile(fstream)
    }

    @JvmStatic
    @Throws(IOException::class)
    fun readFile(fileStream: InputStream?): String {
        DataInputStream(fileStream).use { `in` ->
            BufferedReader(InputStreamReader(`in`)).use { br ->
                var strLine: String?
                val stringBuilder = StringBuilder()
                while (br.readLine().also { strLine = it } != null) {
                    stringBuilder.append(strLine)
                }
                return stringBuilder.toString()
            }
        }
    }

    @JvmStatic
    fun deleteFile(file: File?): Boolean {
        if (file != null && file.exists() && !file.isDirectory) {
            val deleted = file.delete() //NOSONAR (Files.delete() is available from API 26)
            Log.d(TAG, file.name + if (deleted) " deleted succesfully." else " deletion failed!")
            return deleted
        }
        return false
    }

    @JvmStatic
    fun getMimeType(url: String): String? {
        var type: String? = null
        val lastIndex = url.lastIndexOf('.')
        if (lastIndex > 0) {
            val extension = url.substring(lastIndex + 1)
            val mime = MimeTypeMap.getSingleton()
            type = mime.getMimeTypeFromExtension(extension.lowercase(Locale.getDefault()))
        }
        return type
    }

    @JvmStatic
    fun isSupportedMediafileType(mimeType: String?): Boolean {
        Log.d(TAG, mimeType!!)
        if (mimeType == null) {
            return false
        }
        for (s in App.SUPPORTED_MEDIA_TYPES) {
            if (mimeType == s) {
                return true
            }
        }
        return false
    }

    @JvmStatic
    fun moveFileToDir(file: File?, mediaDir: File?, deleteOnError: Boolean) {
        try {
            org.apache.commons.io.FileUtils.moveFileToDirectory(file, mediaDir, true)
        } catch (e: IOException) {
            Analytics.logException(e)
            Log.d(TAG, "Moving file failed", e)
            if (deleteOnError) {
                deleteFile(file)
            }
        }
    }

    @JvmStatic
    fun readableFileSize(size: Long): String {
        if (size <= 0) return "0"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(size / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
    }

    @JvmStatic
    fun copyFile(sourceFile: File, destinationFolder: File) {
        if (!destinationFolder.exists()) {
            destinationFolder.mkdirs()
        }
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        try {
            val destinationFile = File(destinationFolder, sourceFile.name)
            inputStream = FileInputStream(sourceFile)
            outputStream = FileOutputStream(destinationFile)
            IOUtils.copy(inputStream, outputStream)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            IOUtils.closeQuietly(inputStream)
            IOUtils.closeQuietly(outputStream)
        }
    }
}