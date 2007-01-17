/**
 * Copyright (c) 2003-2007, David A. Czarnecki
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
import org.blojsom.blog.Blog;
import org.blojsom.blog.Comment;
import org.blojsom.blog.Entry;
import org.blojsom.event.EventBroadcaster;
import org.blojsom.fetcher.Fetcher;
import org.blojsom.fetcher.FetcherException;
import org.blojsom.plugin.comment.event.CommentAddedEvent;
import org.blojsom.plugin.common.ResponseConstants;
import org.blojsom.servlet.ServletConfigFactoryBean;
import org.blojsom.util.BlojsomConstants;
import org.blojsom.util.BlojsomUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.Date;
import java.util.HashMap;

/**
 * blojsom Comment API Implementation
 * <p/>
 * <a href="http://wellformedweb.org/story/9">Comment API specification</a>.
 * <p/>
 * For more information on the &lt;item/&gt; fragment and its content, check the <a href="http://blogs.law.harvard.edu/tech/rss">RSS 2.0 specification</a>.
 *
 * @author David Czarnecki
 * @author Mark Lussier
 * @since blojsom 3.0
 * @version $Id: CommentAPIServlet.java,v 1.4 2007-01-17 02:35:07 czarneckid Exp $
 */
public class CommentAPIServlet extends HttpServlet {

    private Log _logger = LogFactory.getLog(CommentAPIServlet.class);

    /**
     * RSS &lt;item/&gt; fragment tag containing the title
     */
    private static final String COMMENTAPI_TITLE = "title";

    /**
     * RSS &lt;item/&gt; fragment tag containing the link
     */
    private static final String COMMENTAPI_LINK = "link";

    /**
     * RSS &lt;item/&gt; fragment tag containing the description
     */
    private static final String COMMENTAPI_DESCRIPTION = "description";

    /**
     * RSS &lt;item/&gt; fragment tag containing the author
     */
    private static final String COMMENTAPI_AUTHOR = "author";

    /**
     * RSS &lt;dc:creator/&gt; fragment tag containing the author's name
     */
    private static final String COMMENTAPI_DC_CREATOR = "dc:creator";

    private static final String COMMENTAPI_TITLE_METADATA = "comment-api-metadata-title";

    private static final String COMMENTAPI_ACCEPTS_ONLY_POSTS_MESSAGE = "Comment API server only accepts POST requests.";

    private String[] BLOJSOM_CONFIGURATION_FILES = {"blojsom-commentapi.xml"};

    private ClassPathXmlApplicationContext _classPathXmlApplicationContext;

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

        ServletConfigFactoryBean.setServletConfig(servletConfig);

        _classPathXmlApplicationContext = new ClassPathXmlApplicationContext(BLOJSOM_CONFIGURATION_FILES);
        servletConfig.getServletContext().setAttribute(BlojsomConstants.BLOJSOM_COMMENTAPI_APPLICATION_CONTEXT, _classPathXmlApplicationContext);

        if (_logger.isDebugEnabled()) {
            _logger.debug("blojsom Comment API: All Your Blog Are Belong To Us");
        }
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
            httpServletRequest.setCharacterEncoding(BlojsomConstants.UTF8);
        } catch (UnsupportedEncodingException e) {
            _logger.error(e);
        }

        if (!"post".equalsIgnoreCase(httpServletRequest.getMethod())) {
            httpServletResponse.setContentType("text/html; charset=UTF-8");
            httpServletResponse.setContentLength(COMMENTAPI_ACCEPTS_ONLY_POSTS_MESSAGE.length());
            httpServletResponse.setStatus(HttpURLConnection.HTTP_BAD_METHOD);
            PrintWriter printWriter = httpServletResponse.getWriter();
            printWriter.print(COMMENTAPI_ACCEPTS_ONLY_POSTS_MESSAGE);
            printWriter.flush();

            return;
        }

        String commentAuthor = null;
        String commentEmail = null;
        String commentLink = null;
        String commentText = null;
        String commentTitle = null;
        String commentDCCreator = null;

        // Determine the appropriate user from the URL
        String blogId = BlojsomUtils.getBlogFromPath(httpServletRequest.getPathInfo());
        if (BlojsomUtils.checkNullOrBlank(blogId) || "/".equals(blogId)) {
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "Requested blog not found: " + blogId);

            return;
        }

        Fetcher fetcher = (Fetcher) _classPathXmlApplicationContext.getBean("fetcher");

        Blog blog;
        try {
            blog = fetcher.loadBlog(blogId);
        } catch (FetcherException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }

            return;
        }

        if (blog.getProperty(BlojsomConstants.USE_DYNAMIC_BLOG_URLS) != null) {
            BlojsomUtils.resolveDynamicBaseAndBlogURL(httpServletRequest, blog, blogId);
        }

        if (blog.getBlogCommentsEnabled().booleanValue() && httpServletRequest.getContentLength() > 0) {
            String permalink = httpServletRequest.getParameter(BlojsomConstants.PERMALINK_PARAM);

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

                                if (node.getNodeName().equals(COMMENTAPI_DC_CREATOR) && (node.getFirstChild() != null)) {
                                    commentDCCreator = node.getFirstChild().getNodeValue();
                                }
                            }
                        }
                    }
                }
            } catch (ParserConfigurationException e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }
                httpServletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());

                return;
            } catch (FactoryConfigurationError e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }
                httpServletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());

                return;
            } catch (SAXException e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }
                httpServletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());

                return;
            } catch (IOException e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }
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
                    if (commentAuthor == null) {
                        commentAuthor = commentEmail;
                    }
                } catch (AddressException e) {
                    if (_logger.isErrorEnabled()) {
                        _logger.error(e);
                    }
                }
            } else {
                commentAuthor = "";
                commentEmail = "";
            }

            // If the link is null, set it to an empty string
            if (commentLink == null) {
                commentLink = "";
            }

            // If the dc:creator element is available, assign that to the author name
            if (commentDCCreator != null) {
                commentAuthor = commentDCCreator;
            }

            if (commentText != null) {
                if (_logger.isDebugEnabled()) {
                    _logger.debug("Comment API ==============================================");
                    _logger.debug("      Blog: " + blog.getBlogId());
                    _logger.debug(" Permalink: " + permalink);
                    _logger.debug(" Commenter: " + commentAuthor);
                    _logger.debug("Cmtr Email: " + commentEmail);
                    _logger.debug("      Link: " + commentLink);
                    _logger.debug("   Comment: \n" + commentText);
                }

                // Create a new blog comment
                Comment comment = fetcher.newComment();
                try {
                    Entry entry = fetcher.loadEntry(blog, permalink);

                    HashMap commentMetaData = new HashMap();
                    commentMetaData.put(COMMENTAPI_TITLE_METADATA, commentTitle);

                    comment.setAuthor(commentAuthor);
                    comment.setAuthorEmail(commentEmail);
                    comment.setAuthorURL(commentLink);
                    comment.setBlogId(blog.getId());
                    comment.setComment(commentText);
                    comment.setCommentDate(new Date());
                    comment.setEntry(entry);
                    comment.setIp(httpServletRequest.getRemoteAddr());
                    comment.setMetaData(commentMetaData);
                    comment.setStatus(ResponseConstants.NEW_STATUS);

                    fetcher.saveComment(blog, comment);

                    EventBroadcaster eventBroadcaster = (EventBroadcaster) _classPathXmlApplicationContext.getBean("eventBroadcaster");

                    eventBroadcaster.broadcastEvent(new CommentAddedEvent(this, new Date(), comment, blog));

                    httpServletResponse.setStatus(HttpServletResponse.SC_OK);
                } catch (FetcherException e) {
                    _logger.error(e);

                    httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "Could not find blog entry: " + permalink);
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
        super.destroy();

        _classPathXmlApplicationContext.destroy();

        if (_logger.isDebugEnabled()) {
            _logger.debug("blojsom Comment API destroyed");
        }
    }
}

