package org.digitalcampus.oppia.utils.xmlreaders;

import org.digitalcampus.oppia.model.Media;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.ArrayList;
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

    private ArrayList<Media> courseMedia = new ArrayList<Media>();
    private boolean insideMediaTag = false;
    private Stack<String> parentElements = new Stack<String>();

    @Override
    public ArrayList<Media> getCourseMedia() {
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
