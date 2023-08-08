package org.digitalcampus.oppia.utils.xmlreaders

import org.digitalcampus.oppia.analytics.Analytics
import org.xml.sax.SAXException
import org.xml.sax.XMLReader
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import javax.xml.parsers.SAXParserFactory

object XMLSecurityHelper {
    // https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html#jaxp-documentbuilderfactory-saxparserfactory-and-dom4j
    // https://xerces.apache.org/xerces2-j/features.html

    fun getNewSecureDocumentBuilder(): DocumentBuilder {
        val dbf = DocumentBuilderFactory.newInstance()
        try {
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
            dbf.setFeature("http://xml.org/sax/features/external-general-entities", false)
            dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false)
            dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
            dbf.isXIncludeAware = false
            dbf.isExpandEntityReferences = false
        } catch (e: ParserConfigurationException) {
            // This should catch a failed setFeature feature
        }
        return dbf.newDocumentBuilder()
    }

    fun getSecureXMLReader(): XMLReader?  {
        val parserFactory = SAXParserFactory.newInstance()
        try {
            parserFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
            parserFactory.setFeature("http://xml.org/sax/features/external-general-entities", false)
            parserFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false)
            parserFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
            parserFactory.isXIncludeAware = false
        } catch (e: ParserConfigurationException) {
            // This should catch a failed setFeature feature
        } catch (_: SAXException) {
        }

        return try {
            parserFactory.newSAXParser().xmlReader
        } catch (e: ParserConfigurationException) {
            Analytics.logException(e)
            null
        } catch (e: SAXException) {
            Analytics.logException(e)
            null
        }
    }
}