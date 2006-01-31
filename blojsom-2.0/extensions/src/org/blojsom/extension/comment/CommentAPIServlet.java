/**
 * Copyright (c) 2003-2006, David A. Czarnecki
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *     following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 *     following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of "David A. Czarnecki" and "blojsom" nor the names of its contributors may be used to
 *     endorse or promote products derived from this software without specific prior written permission.
 * Products derived from this software may not be called "blojsom", nor may "blojsom" appear in their name,
 *     without prior written permission of David A. Czarnecki.
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
package org.blojsom.extension.comment;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.blojsom.BlojsomException;
import org.blojsom.blog.Blog;
import org.blojsom.blog.BlogComment;
import org.blojsom.blog.BlogEntry;
import org.blojsom.blog.BlogUser;
import org.blojsom.fetcher.BlojsomFetcherException;
import org.blojsom.plugin.comment.event.CommentAddedEvent;
import org.blojsom.plugin.comment.CommentPlugin;
import org.blojsom.servlet.BlojsomBaseServlet;
import org.blojsom.util.BlojsomConstants;
import org.blojsom.util.BlojsomUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;

/**
 * blojsom Comment API Implementation
 * <p/>
 * <a href="http://wellformedweb.org/story/9">Comment API specification</a>.
 * <p/>
 * For more information on the &lt;item/&gt; fragment and its content, check the <a href="http://blogs.law.harvard.edu/tech/rss">RSS 2.0 specification</a>.
 *
 * @author Mark Lussier
 * @author David Czarnecki
 * @version $Id: CommentAPIServlet.java,v 1.17 2006-01-31 19:08:33 czarneckid Exp $
 */
public class CommentAPIServlet extends BlojsomBaseServlet implements BlojsomConstants {

    /**
     * RSS <item/> fragment tag containing the Title
     */
    private static final String COMMENTAPI_TITLE = "title";

    /**
     * RSS <item/> fragment tag containing the Link
     */
    private static final String COMMENTAPI_LINK = "link";

    /**
     * RSS <item/> fragment tag containing the Description
     */
    private static final String COMMENTAPI_DESCRIPTION = "description";

    /**
     * RSS <item/> fragment tag containing the Author
     */
    private static final String COMMENTAPI_AUTHOR = "author";

    private Log _logger = LogFactory.getLog(CommentAPIServlet.class);

    /**
     * Default constructor
     */
    public CommentAPIServlet() {
    }

    /**
     * Initialize the blojsom Comment API servlet
     *
     * @param servletConfig Servlet configuration information
     * @throws ServletException If there is an error initializing the servlet
     */
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);

        configureBlojsom(servletConfig);
    }

    /**
     * Service a Comment API request
     *
     * @param httpServletRequest  Request
     * @param httpServletResponse Response
     * @throws ServletException If there is an error processing the request
     * @throws IOException      If there is an error during I/O
     */
    protected void service(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        try {
            httpServletRequest.setCharacterEncoding(UTF8);
        } catch (UnsupportedEncodingException e) {
            _logger.error(e);
        }

        String commentAuthor = null;
        String commentEmail = null;
        String commentLink = null;
        String commentText = null;
        String commentTitle = null;

        // Determine the appropriate user from the URL
        String user;
        String userFromPath = BlojsomUtils.getUserFromPath(httpServletRequest.getPathInfo());
        String requestedCategory;

        if (userFromPath == null) {
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "Requested user not available in URL");
            return;
        } else {
            user = userFromPath;
            requestedCategory = BlojsomUtils.getCategoryFromPath(httpServletRequest.getPathInfo());
            requestedCategory = BlojsomUtils.urlDecode(requestedCategory);
            requestedCategory = BlojsomUtils.normalize(requestedCategory);
        }

        if (BlojsomUtils.checkNullOrBlank(user) || "/".equals(user)) {
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "Requested user not available in URL");
            return;
        }

        // Fetch the user and their blog
        BlogUser blogUser = null;
        try {
            blogUser = _blojsomConfiguration.loadBlog(user);
        } catch (BlojsomException e) {
            _logger.error(e);
        }

        if (blogUser == null) {
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "Requested user not available: " + user);

            return;
        }

        Blog blog = blogUser.getBlog();

        // Check to see if we need to dynamically determine blog-base-url and blog-url?
        BlojsomUtils.resolveDynamicBaseAndBlogURL(httpServletRequest, blog, user);

        _logger.info("Processing a comment for [" + user + "] in category [" + requestedCategory + "]");

        if (blog.getBlogCommentsEnabled().booleanValue() && httpServletRequest.getContentLength() > 0) {
            String permalink = httpServletRequest.getParameter(PERMALINK_PARAM);

            try {
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document document = builder.parse(httpServletRequest.getInputStream());

                // Walk through the RSS2 Item Fragment
                Element docElement = document.getDocumentElement();
                if (docElement.hasChildNodes()) {
                    NodeList comment = docElement.getChildNodes();
                    if (comment.getLength() > 0) {
                        for (int x = 0; x < comment.getLength(); x++) {
                            Node node = comment.item(x);
                            if (node.getNodeType() == Node.ELEMENT_NODE) {
                                if (node.getNodeName().equals(COMMENTAPI_LINK) && (node.getFirstChild() != null)) {
                                    commentLink = node.getFirstChild().getNodeValue();
                                }
                                if (node.getNodeName().equals(COMMENTAPI_TITLE) && (node.getFirstChild() != null)) {
                                    commentTitle = node.getFirstChild().getNodeValue();
                                }
                                if (node.getNodeName().equals(COMMENTAPI_AUTHOR) && (node.getFirstChild() != null)) {
                                    commentAuthor = node.getFirstChild().getNodeValue();
                                }
                                if (node.getNodeName().equals(COMMENTAPI_DESCRIPTION) && (node.getFirstChild() != null)) {
                                    commentText = node.getFirstChild().getNodeValue();
                                }
                            }
                        }
                    }
                }
            } catch (ParserConfigurationException e) {
                _logger.error(e);
                httpServletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());

                return;
            } catch (FactoryConfigurationError e) {
                _logger.error(e);
                httpServletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());

                return;
            } catch (SAXException e) {
                _logger.error(e);
                httpServletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());

                return;
            } catch (IOException e) {
                _logger.error(e);
                httpServletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());

                return;
            }

            // Try to extract an email address from "User Name <useremail@.com>" formatted string,
            // otherwise, just use the Name
            if (commentAuthor != null) {
                try {
                    InternetAddress emailaddress = new InternetAddress(commentAuthor);
                    commentEmail = emailaddress.getAddress();
                    commentAuthor = emailaddress.getPersonal();
                } catch (AddressException e) {
                    _logger.error(e);
                }
            } else {
                commentAuthor = "";
                commentEmail = "";
            }

            // If the link is null, set it to an empty string
            if (commentLink == null) {
                commentLink = "";
            }

            if (commentText != null) {
                _logger.debug("Comment API ==============================================");
                _logger.debug(" Blog User: " + user);
                _logger.debug("  Category: " + requestedCategory);
                _logger.debug(" Permalink: " + permalink);
                _logger.debug(" Commenter: " + commentAuthor);
                _logger.debug("Cmtr Email: " + commentEmail);
                _logger.debug("      Link: " + commentLink);
                _logger.debug("   Comment: \n" + commentText);

                // Create a new blog comment
                BlogComment comment = _fetcher.newBlogComment();
                try {
                    BlogEntry blogEntry = BlojsomUtils.fetchEntry(_fetcher, blogUser, requestedCategory, permalink);

                    HashMap commentMetaData = new HashMap();
                    commentMetaData.put(CommentPlugin.BLOJSOM_COMMENT_PLUGIN_METADATA_IP, httpServletRequest.getRemoteAddr());

                    comment.setAuthor(commentAuthor);
                    comment.setAuthorEmail(commentEmail);
                    comment.setAuthorURL(commentLink);
                    comment.setComment(commentText);
                    comment.setCommentDate(new Date());
                    comment.setBlogEntry(blogEntry);
                    comment.setMetaData(commentMetaData);

                    comment.save(blogUser);

                    _blojsomConfiguration.getEventBroadcaster().broadcastEvent(new CommentAddedEvent(this, new Date(), comment, blogUser));

                    httpServletResponse.setStatus(HttpServletResponse.SC_OK);
                } catch (BlojsomFetcherException e) {
                    _logger.error(e);

                    httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "Could not find blog entry: " + requestedCategory + permalink);
                } catch (BlojsomException e) {
                    _logger.error(e);

                    httpServletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                }
            } else {
                httpServletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No comment text available");
            }
        } else {
            httpServletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Blog comments not enabled or No content in request");
        }
    }

    /**
     * Called when removing the servlet from the servlet container
     */
    public void destroy() {
    }
}

