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

class GamificationEvent : Serializable {
    var event: String? = null
    var points = 0
    var isCompleted = false

    constructor() {}
    constructor(event: String?, points: Int) {
        this.event = event
        this.points = points
        isCompleted = false
    }

    constructor(event: String?, points: Int, completed: Boolean) {
        this.event = event
        this.points = points
        isCompleted = completed
    }

    companion object {
        private const val serialVersionUID = 3649466060301114481L
    }
}

// TODO: Replace with the below class when all the GamificationEvent references are migrated to kotlin
//       This is because Java does not allow constructors with default parameter values
//class GamificationEvent(
//    var event: String = "",
//    var points: Int = 0,
//    var isCompleted: Boolean = false
//) : Serializable {
//
//    companion object {
//        private const val serialVersionUID = 3649466060301114481L
//    }
//}