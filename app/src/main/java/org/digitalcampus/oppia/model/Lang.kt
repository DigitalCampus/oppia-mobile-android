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

data class Lang(
    var language: String,
    var content: String
) : Serializable {

    var location: String? = null

    companion object {
        private const val serialVersionUID = -8960131611429444591L
        val TAG = Lang::class.simpleName
    }

    init {
        // only set the first part of the lang - not the full localisation
        val langCountry = language.split("[_-]")
        language = langCountry[0]
    }

    override fun toString(): String {
        return "Lang{" +
                "language='" + language + '\'' +
                ", content='" + content + '\'' +
                ", location='" + location + '\'' +
                '}'
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Lang

        return language.equals(other.language, ignoreCase = true)
    }

    override fun hashCode(): Int {
        return language.hashCode()
    }
}