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

    public static DocumentBuilder getNewSecureDocumentBuilder() throws ParserConfigurationException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setExpandEntityReferences(false);
        return factory.newDocumentBuilder();
    }


    public static void makeParserSecure(XMLReader reader) throws SAXNotRecognizedException, SAXNotSupportedException {
        reader.setFeature("http://xml.org/sax/features/external-general-entities", false);
    }
}
