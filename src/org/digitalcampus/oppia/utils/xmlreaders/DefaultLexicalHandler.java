package org.digitalcampus.oppia.utils.xmlreaders;

import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

class DefaultLexicalHandler extends DefaultHandler implements LexicalHandler {

    protected StringBuilder chars;

    @Override
    public void startDocument() throws SAXException {
        chars = new StringBuilder();
    }
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException{
        chars.append(ch, start, length);
    }
    @Override
    public void comment(char[] aArg0, int aArg1, int aArg2) throws SAXException {}
    @Override
    public void endCDATA() throws SAXException {}
    @Override
    public void endDTD() throws SAXException {}
    @Override
    public void endEntity(String aName) throws SAXException {}
    @Override
    public void startCDATA() throws SAXException {}
    @Override
    public void startDTD(String aArg0, String aArg1, String aArg2) throws SAXException {}
    @Override
    public void startEntity(String aName) throws SAXException {}
}