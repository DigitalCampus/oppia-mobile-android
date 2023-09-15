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

import java.io.Serializable

open class Tag : Serializable {

    companion object {
        private const val serialVersionUID = 5144400570961137778L
        val TAG = Tag::class.simpleName
    }

    var name: String? = null
    private var count = 0
    var id = 0
    var description: String? = null
    var orderPriority = 0
    var icon: String? = null
    var isHighlight = false
    var countNewDownloadEnabled = -1
    var countAvailable = 0
    private val courses = ArrayList<Course>()

    fun getCount() : Int {
        // count should not be under 0
        return count.coerceAtLeast(0)
    }

    fun setCount(count: Int) {
        this.count = count
    }

    fun getCourses(): List<Course> {
        return courses
    }

    fun setCourses(courses: List<Course>) {
        this.courses.clear()
        this.courses.addAll(courses)
    }

    fun incrementCountAvailable() {
        countAvailable++
    }

    fun decrementCountAvailable() {
        countAvailable--
    }
}