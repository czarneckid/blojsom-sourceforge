package org.ignition.blojsom.extension.echoapi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ignition.blojsom.blog.Blog;
import org.ignition.blojsom.blog.BlogEntry;
import org.ignition.blojsom.util.BlojsomConstants;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;

/**
 *
 * @author Mark Lussier
 */

public class EchoEntry implements EchoConstants, BlojsomConstants {

    private Document echodocument = null;

    private Log _logger = LogFactory.getLog(EchoEntry.class);

    public EchoEntry(Blog blog, BlogEntry entry) {
        echodocument = initializeDocument();
        if (echodocument != null) {
            populateDocument(blog, entry);
        }

    }



    // ==================

    /**
     * Creates a  Element that contains a TextNode (ie: <element>nodevalue</element>
     * @param name Name the the element
     * @param value TextNode's content
     * @return a populated Element
     */
    private Element createTextElement(String name, String value) {
        Element result = echodocument.createElement(name);
        result.appendChild(echodocument.createTextNode(value));
        return result;
    }

    /**
     * Creates the Author section of the NEcho Document
     * @param blog
     * @return
     */
    private Element createAuthorElement(Blog blog) {
        Element author = echodocument.createElement(ELEMENT_AUTHOR);

        if (blog.getBlogOwner() != null) {
            author.appendChild(createTextElement(ELEMENT_NAME, blog.getBlogOwner()));
        }

        if (blog.getBlogURL() != null) {
            author.appendChild(createTextElement(ELEMENT_HOMEPAGE, blog.getBlogURL()));
            author.appendChild(createTextElement(ELEMENT_WEBLOG, blog.getBlogURL()));
        }

        return author;

    }


    private Document initializeDocument() {
        Document result = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            DOMImplementation impl = builder.getDOMImplementation();
            result = impl.createDocument("http://example.com/newformat#", "entry", null);
        } catch (FactoryConfigurationError factoryConfigurationError) {
            factoryConfigurationError.printStackTrace();
        } catch (ParserConfigurationException e) {
            _logger.error(e);
        }

        return result;
    }


    /**
     *
     * @param blog
     */
    private void populateDocument(Blog blog, BlogEntry entry) {

        Element theentry = echodocument.getDocumentElement();

        theentry.appendChild(createTextElement(ELEMENT_TITLE, entry.getTitle()));
        theentry.appendChild(createTextElement(ELEMENT_SUBTITLE, ""));
        theentry.appendChild(createTextElement(ELEMENT_SUMMARY, ""));

        theentry.appendChild(createAuthorElement(blog));

        theentry.appendChild(createTextElement(ELEMENT_ISSUED, entry.getISO8601Date()));
        theentry.appendChild(createTextElement(ELEMENT_CREATED, entry.getDateAsFormat(BLOJSOM_ECHO_DATE)));
        theentry.appendChild(createTextElement(ELEMENT_MODIFIED, entry.getDateAsFormat(BLOJSOM_ECHO_DATE)));

        theentry.appendChild(createTextElement(ELEMENT_LINK, entry.getLink()));
        theentry.appendChild(createTextElement(ELEMENT_ID, entry.getLink()));


        Element content = echodocument.createElement(ELEMENT_CONTENT);
        content.setAttribute("type", "text/html");
        content.setAttribute("xml:lang", blog.getBlogLanguage());
        content.appendChild(echodocument.createCDATASection(entry.getDescription()));


        theentry.appendChild(content);

    }


    public String getAsString() {
        String result = "";

        try {
            Transformer xform = TransformerFactory.newInstance().newTransformer();
            DOMSource source = new DOMSource(echodocument.getDocumentElement());
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            StreamResult dest = new StreamResult(os);
            xform.transform(source, dest);
            result = os.toString();
        } catch (TransformerFactoryConfigurationError transformerFactoryConfigurationError) {
            transformerFactoryConfigurationError.printStackTrace();
        } catch (TransformerException e) {
            _logger.error(e);
        }

        return result;

    }


}
