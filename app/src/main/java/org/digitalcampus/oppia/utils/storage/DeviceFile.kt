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

import android.util.Log
import java.io.File
import java.lang.ref.WeakReference

class DeviceFile {
    companion object {
        val TAG = DeviceFile::class.simpleName
    }

    private var file: File
    private var children: WeakReference<Array<DeviceFile>>? = null
    private var bGrandPeeked = false

    constructor(path: String) {
        file = File(path)
    }

    constructor(file: File) {
        this.file = file
    }

    val parent: DeviceFile?
        get() {
            val parent = file.parent ?: return null
            return DeviceFile(parent)
        }
    val path: String
        get() = file.path

    private fun getOpenPaths(files: Array<File>?): Array<DeviceFile> {
        return files?.map { DeviceFile(it) }?.toTypedArray() ?: emptyArray()
    }

    val name: String
        get() = file.name
    private val isDirectory: Boolean
        get() = file.isDirectory

    fun canWrite(): Boolean {
        return file.canWrite()
    }

    fun list(): Array<DeviceFile> {
        return children?.get() ?: listFiles()
    }

    @JvmOverloads
    fun listFiles(grandPeek: Boolean = false): Array<DeviceFile> {
        children?.get()?.let { return it }

        val realFiles = file.listFiles()
        var children2: Array<DeviceFile>? = getOpenPaths(realFiles)
        if (children2.isNullOrEmpty() && !isDirectory && file.parentFile != null) {
            children2 = parent?.listFiles(grandPeek)
        }

        if (children2 == null) return emptyArray()

        if (grandPeek && !bGrandPeeked && children2.isNotEmpty()) {
            for (child in children2) {
                try {
                    if (!child.isDirectory) {
                        continue
                    }
                    child.list()
                } catch (e: ArrayIndexOutOfBoundsException) {
                    Log.d(TAG, "mChildren2 cannot find item in index no", e)
                }
            }
            bGrandPeeked = true
        }
        children = WeakReference(children2)
        return children2
    }
}