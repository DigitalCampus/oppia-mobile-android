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
package org.digitalcampus.oppia.model

import org.digitalcampus.oppia.utils.TextUtilsJava
import java.io.File
import java.io.Serializable

// TODO: Convert to data class when all references are migrated to kotlin
class Section : MultiLangInfoModel(), Serializable {
    var order = 0
    var activities: MutableList<Activity> = ArrayList()
    var imageFile: String? = null
    var password: String? = null
    var isUnlocked = false

    companion object {
        private const val serialVersionUID = 6360494638548755423L
        @JvmField
        val TAG = Section::class.simpleName
    }

    val isProtectedByPassword: Boolean
        get() { return !TextUtilsJava.isEmpty(password) }

    fun addActivity(activity: Activity) {
        activities.add(activity)
    }

    fun checkPassword(inputPassword: String?): Boolean {
        return TextUtilsJava.equals(password, inputPassword)
    }

    fun hasCustomImage(): Boolean {
        return !TextUtilsJava.isEmpty(imageFile)
    }

    fun getImageFilePath(prefix: String): String {
        val separator = if (!prefix.endsWith(File.separator)) File.separator else ""
        return prefix + separator + imageFile
    }

    fun getActivity(digest: String): Activity? {
        return activities.find { it.digest == digest }
    }

    fun getCompletedActivities(): Int {
        return activities.count { it.completed }
    }

}