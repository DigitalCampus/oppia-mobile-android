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

class CourseMetaPage : Serializable {
    companion object {
        private const val serialVersionUID = -1597711519611488890L
        @JvmField
        val TAG = CourseMetaPage::class.simpleName
    }

    var id = 0
    private val langs = ArrayList<Lang>()

    fun addLang(l: Lang) {
        langs.add(l)
    }

    fun getLang(langStr: String?): Lang? {
        return langs.find { it.language.equals(langStr, ignoreCase = true) } ?: langs.firstOrNull()
    }
}