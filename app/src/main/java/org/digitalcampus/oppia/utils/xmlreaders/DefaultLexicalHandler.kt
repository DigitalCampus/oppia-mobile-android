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
package org.digitalcampus.oppia.utils.xmlreaders

import org.xml.sax.ext.LexicalHandler
import org.xml.sax.helpers.DefaultHandler

open class DefaultLexicalHandler : DefaultHandler(), LexicalHandler {
    @JvmField
    protected var chars: StringBuilder? = null
    override fun startDocument() {
        chars = StringBuilder()
    }

    override fun characters(ch: CharArray, start: Int, length: Int) {
        chars?.append(ch, start, length)
    }

    override fun comment(aArg0: CharArray, aArg1: Int, aArg2: Int) {
        // do nothing
    }

    override fun endCDATA() {
        // do nothing
    }

    override fun endDTD() {
        // do nothing
    }

    override fun endEntity(aName: String) {
        // do nothing
    }

    override fun startCDATA() {
        // do nothing
    }

    override fun startDTD(aArg0: String, aArg1: String, aArg2: String) {
        // do nothing
    }

    override fun startEntity(aName: String) {
        // do nothing
    }
}