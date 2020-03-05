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

package org.digitalcampus.oppia.utils.xmlreaders;

import org.digitalcampus.oppia.model.Media;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class CourseMediaXMLHandler extends DefaultLexicalHandler implements IMediaXMLHandler {

    private static final String NODE_DIGEST = "digest";
    private static final String NODE_FILENAME = "filename";
    private static final String NODE_LENGTH = "length";
    private static final String NODE_MEDIA = "media";
    private static final String NODE_DOWNLOAD_URL = "download_url";
    private static final String NODE_FILE = "file";
    private static final String NODE_FILESIZE = "filesize";
    private static final String NODE_MODULE = "module";

    private List<Media> courseMedia = new ArrayList<>();
    private boolean insideMediaTag = false;
    private Stack<String> parentElements = new Stack<>();

    public List<Media> getCourseMedia() {
        return courseMedia;
    }

    @Override
    public void startElement(String aUri, String aLocalName,String aQName, Attributes aAttributes) throws SAXException {
        chars.setLength(0);

        if (insideMediaTag && NODE_FILE.equals(aQName)) {
            Media mediaObject = new Media();
            mediaObject.setFilename(aAttributes.getValue(NODE_FILENAME));
            mediaObject.setDownloadUrl(aAttributes.getValue(NODE_DOWNLOAD_URL));
            mediaObject.setDigest(aAttributes.getValue(NODE_DIGEST));
            String mediaLength = aAttributes.getValue(NODE_LENGTH);
            String mediaFilesize = aAttributes.getValue(NODE_FILESIZE);
            mediaObject.setLength(mediaLength==null?0:Integer.parseInt(mediaLength));
            mediaObject.setFileSize(mediaFilesize==null?0:Double.parseDouble(mediaFilesize));

            courseMedia.add(mediaObject);
        }
        else if (NODE_MEDIA.equals(aQName) && NODE_MODULE.equals(parentElements.peek())){
            insideMediaTag = true;
        }

        parentElements.push(aQName);
    }

    @Override
    public void endElement(String aUri, String aLocalName, String aQName) throws SAXException {
        if (NODE_MEDIA.equals(aQName)){
            insideMediaTag = false;
        }
        parentElements.pop();
    }
}
