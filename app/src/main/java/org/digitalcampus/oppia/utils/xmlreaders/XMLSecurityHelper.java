package org.digitalcampus.oppia.utils.xmlreaders;

import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

import javax.xml.XMLConstants;
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

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        factory.setExpandEntityReferences(false);
        return factory.newDocumentBuilder();
    }


    public static void makeParserSecure(XMLReader reader) throws SAXNotRecognizedException, SAXNotSupportedException {
        reader.setFeature("http://xml.org/sax/features/external-general-entities", false);
    }
}
