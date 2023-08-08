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

import org.digitalcampus.oppia.model.Media
import org.xml.sax.Attributes
import java.util.ArrayList
import java.util.Stack

class CourseMediaXMLHandler : DefaultLexicalHandler(), IMediaXMLHandler {

    companion object {
        private const val NODE_DIGEST = "digest"
        private const val NODE_FILENAME = "filename"
        private const val NODE_LENGTH = "length"
        private const val NODE_MEDIA = "media"
        private const val NODE_DOWNLOAD_URL = "download_url"
        private const val NODE_FILE = "file"
        private const val NODE_FILESIZE = "filesize"
        private const val NODE_MODULE = "module"
    }

    private var insideMediaTag = false
    private val parentElements = Stack<String>()
    override val courseMedia: MutableList<Media> = ArrayList()

    override fun startElement(aUri: String, aLocalName: String, aQName: String, attributes: Attributes) {
        chars?.setLength(0)
        if (insideMediaTag && NODE_FILE == aQName) {
            val mediaObject = Media()
            mediaObject.filename = attributes.getValue(NODE_FILENAME)
            mediaObject.downloadUrl = attributes.getValue(NODE_DOWNLOAD_URL)
            mediaObject.digest = attributes.getValue(NODE_DIGEST)
            val mediaLength = attributes.getValue(NODE_LENGTH)
            val mediaFilesize = attributes.getValue(NODE_FILESIZE)
            mediaObject.length = mediaLength?.toIntOrNull() ?: 0
            mediaObject.fileSize = mediaFilesize?.toDoubleOrNull() ?: 0.0
            courseMedia.add(mediaObject)
        } else if (NODE_MEDIA == aQName && NODE_MODULE == parentElements.peek()) {
            insideMediaTag = true
        }
        parentElements.push(aQName)
    }

    override fun endElement(aUri: String, aLocalName: String, aQName: String) {
        if (NODE_MEDIA == aQName) {
            insideMediaTag = false
        }
        parentElements.pop()
    }
}