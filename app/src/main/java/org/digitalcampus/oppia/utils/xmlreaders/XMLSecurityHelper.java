package org.digitalcampus.oppia.utils.xmlreaders;

import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class XMLSecurityHelper {

    // https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html#jaxp-documentbuilderfactory-saxparserfactory-and-dom4j
    // https://xerces.apache.org/xerces2-j/features.html

    private XMLSecurityHelper() {
        throw new IllegalStateException("Utility class");
    }

    public static DocumentBuilder getNewSecureDocumentBuilder() throws ParserConfigurationException {

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


    public static void makeParserSecure(XMLReader reader) throws SAXNotRecognizedException, SAXNotSupportedException {
        reader.setFeature("http://xml.org/sax/features/external-general-entities", false);
    }
}
