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

    public void comment(char[] aArg0, int aArg1, int aArg2) throws SAXException {
        // do nothing
    }
    
    public void endCDATA() throws SAXException {
        // do nothing
    }
    
    public void endDTD() throws SAXException {
        // do nothing
    }
    
    public void endEntity(String aName) throws SAXException {
        // do nothing
    }
    
    public void startCDATA() throws SAXException {
        // do nothing
    }
    
    public void startDTD(String aArg0, String aArg1, String aArg2) throws SAXException {
        // do nothing
    }
    
    public void startEntity(String aName) throws SAXException {
        // do nothing
    }
}