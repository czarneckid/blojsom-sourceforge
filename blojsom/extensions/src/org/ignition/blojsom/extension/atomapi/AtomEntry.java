/**
 * Copyright (c) 2003, David A. Czarnecki
 * All rights reserved.
 *
 * Portions Copyright (c) 2003 by Mark Lussier
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the "David A. Czarnecki" and "blojsom" nor the names of
 * its contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * Products derived from this software may not be called "blojsom",
 * nor may "blojsom" appear in their name, without prior written permission of
 * David A. Czarnecki.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.ignition.blojsom.extension.atomapi;

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
 * AtomEntry
 *
 * @author Mark Lussier
 * @version $Id: AtomEntry.java,v 1.1 2003-07-18 01:20:55 czarneckid Exp $
 */
public class AtomEntry implements AtomConstants, BlojsomConstants {

    private Document atomdocument = null;

    private Log _logger = LogFactory.getLog(AtomEntry.class);

    public AtomEntry(Blog blog, BlogEntry entry) {
        atomdocument = initializeDocument();
        if (atomdocument != null) {
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
        Element result = atomdocument.createElement(name);
        result.appendChild(atomdocument.createTextNode(value));
        return result;
    }

    /**
     * Creates the Author section of the Atom Document
     * @param blog
     * @return
     */
    private Element createAuthorElement(Blog blog) {
        Element author = atomdocument.createElement(ELEMENT_AUTHOR);

        if (blog.getBlogOwner() != null) {
            author.appendChild(createTextElement(ELEMENT_NAME, blog.getBlogOwner()));
        }

        if (blog.getBlogURL() != null) {
            author.appendChild(createTextElement(ELEMENT_HOMEPAGE, blog.getBlogURL()));
            author.appendChild(createTextElement(ELEMENT_WEBLOG, blog.getBlogURL()));
        }

        return author;

    }

    /**
     *
     * @return
     */
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

        Element theentry = atomdocument.getDocumentElement();

        theentry.appendChild(createTextElement(ELEMENT_TITLE, entry.getTitle()));
        theentry.appendChild(createTextElement(ELEMENT_SUBTITLE, ""));
        theentry.appendChild(createTextElement(ELEMENT_SUMMARY, ""));

        theentry.appendChild(createAuthorElement(blog));

        theentry.appendChild(createTextElement(ELEMENT_ISSUED, entry.getISO8601Date()));
        theentry.appendChild(createTextElement(ELEMENT_CREATED, entry.getDateAsFormat(ATOM_DATE_FORMAT)));
        theentry.appendChild(createTextElement(ELEMENT_MODIFIED, entry.getDateAsFormat(ATOM_DATE_FORMAT)));

        theentry.appendChild(createTextElement(ELEMENT_LINK, entry.getLink()));
        theentry.appendChild(createTextElement(ELEMENT_ID, entry.getLink()));


        Element content = atomdocument.createElement(ELEMENT_CONTENT);
        content.setAttribute("type", "text/html");
        content.setAttribute("xml:lang", blog.getBlogLanguage());
        content.appendChild(atomdocument.createCDATASection(entry.getDescription()));


        theentry.appendChild(content);

    }


    /**
     * Convert the Entry DOM into an XML String
     * @return The AtomEntry as a String
     */
    public String getAsString() {
        String result = "";

        try {
            Transformer xform = TransformerFactory.newInstance().newTransformer();
            DOMSource source = new DOMSource(atomdocument.getDocumentElement());
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
