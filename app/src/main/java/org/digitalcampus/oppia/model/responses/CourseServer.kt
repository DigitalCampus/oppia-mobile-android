package org.digitalcampus.oppia.model.responses

import org.digitalcampus.oppia.model.Course
import org.digitalcampus.oppia.utils.TextUtilsJava

class CourseServer {
    var id = 0
    var shortname: String? = null
    var version: Double = 0.0
    var organisation: String? = null
    var author: String? = null
    var title: Map<String, String> = HashMap()
    var description: Map<String, String> = HashMap()
    var priority = 0
    var status = Course.STATUS_LIVE
    var isRestricted = false
    var restrictedCohorts: List<Int>? = null

    fun hasStatus(status: String?): Boolean {
        return TextUtilsJava.equals(this.status, status)
    }
}