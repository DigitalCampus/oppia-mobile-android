package org.digitalcampus.oppia.utils.xmlreaders;

import com.splunk.mint.Mint;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

class XMLSecurityHelper {

    // https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html#jaxp-documentbuilderfactory-saxparserfactory-and-dom4j
    // https://xerces.apache.org/xerces2-j/features.html

    private XMLSecurityHelper() {
        throw new IllegalStateException("Utility class");
    }

    static DocumentBuilder getNewSecureDocumentBuilder() throws ParserConfigurationException {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {

            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            dbf.setXIncludeAware(false);
            dbf.setExpandEntityReferences(false);


        } catch (ParserConfigurationException e) {
            // This should catch a failed setFeature feature
        }
        return dbf.newDocumentBuilder();
    }


    static XMLReader getSecureXMLReader(){
        SAXParserFactory parserFactory  = SAXParserFactory.newInstance();
        try {
            parserFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            parserFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            parserFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            parserFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            parserFactory.setXIncludeAware(false);
        } catch (ParserConfigurationException| SAXException e) {
            // This should catch a failed setFeature feature
        }

        try {
            return parserFactory.newSAXParser().getXMLReader();
        } catch (ParserConfigurationException|SAXException e) {
            Mint.logException(e);
            return null;
        }


    }

}
