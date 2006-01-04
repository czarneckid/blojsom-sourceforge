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
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.blojsom.BlojsomException;
import org.blojsom.blog.*;
import org.blojsom.fetcher.BlojsomFetcherException;
import org.blojsom.plugin.comment.CommentPlugin;
import org.blojsom.plugin.comment.event.CommentAddedEvent;
import org.blojsom.plugin.email.EmailMessage;
import org.blojsom.plugin.email.EmailUtils;
import org.blojsom.plugin.email.SendEmailPlugin;
import org.blojsom.servlet.BlojsomBaseServlet;
import org.blojsom.util.BlojsomConstants;
import org.blojsom.util.BlojsomProperties;
import org.blojsom.util.BlojsomUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.mail.Session;
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
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * blojsom Comment API Implementation
 * <p/>
 * <a href="http://wellformedweb.org/story/9">Comment API specification</a>.
 * <p/>
 * For more information on the &lt;item/&gt; fragment and its content, check the <a href="http://blogs.law.harvard.edu/tech/rss">RSS 2.0 specification</a>.
 *
 * @author Mark Lussier
 * @author David Czarnecki
 * @version $Id: CommentAPIServlet.java,v 1.16 2006-01-04 16:24:24 czarneckid Exp $
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

    private final static String BLOG_VELOCITY_PROPERTIES_IP = "velocity-properties";
    private static final String DEFAULT_VELOCITY_PROPERTIES = "/WEB-INF/velocity.properties";

    private Log _logger = LogFactory.getLog(CommentAPIServlet.class);
    private Session _mailsession = null;
    private ServletConfig _servletConfig;
    private Properties _velocityProperties;

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
        _servletConfig = servletConfig;

        String _hostname = servletConfig.getInitParameter(SendEmailPlugin.SMTPSERVER_IP);
        if (_hostname != null) {
            Properties _props = new Properties();
            _props.put(SendEmailPlugin.SESSION_NAME, _hostname);
            _mailsession = Session.getInstance(_props);
        }

        String velocityConfiguration = servletConfig.getInitParameter(BLOG_VELOCITY_PROPERTIES_IP);
        if (BlojsomUtils.checkNullOrBlank(velocityConfiguration)) {
            velocityConfiguration = DEFAULT_VELOCITY_PROPERTIES;
        }

        _velocityProperties = new Properties();
        InputStream is = servletConfig.getServletContext().getResourceAsStream(velocityConfiguration);

        try {
            _velocityProperties.load(is);
            is.close();
        } catch (Exception e) {
            _logger.error(e);
        }

        configureBlojsom(servletConfig);
    }

    /**
     * Loads a {@link BlogUser} object for a given user ID
     *
     * @param userID User ID
     * @return {@link BlogUser} configured for the given user ID or <code>null</code> if there is an error loading the user
     * @since blojsom 2.16
     */
    private BlogUser loadBlogUser(String userID) {
        BlogUser blogUser = new BlogUser();
        blogUser.setId(userID);

        try {
            Properties userProperties = new BlojsomProperties();
            InputStream is = _servletConfig.getServletContext().getResourceAsStream(_baseConfigurationDirectory + userID + '/' + BLOG_DEFAULT_PROPERTIES);

            if (is == null) {
                return null;
            }

            userProperties.load(is);
            is.close();
            Blog userBlog;

            userBlog = new Blog(userProperties);
            blogUser.setBlog(userBlog);

            _logger.debug("Configured blojsom user: " + blogUser.getId());
        } catch (BlojsomConfigurationException e) {
            _logger.error(e);
            return null;
        } catch (IOException e) {
            _logger.error(e);
            return null;
        }

        return blogUser;
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
        BlogUser blogUser = loadBlogUser(user);
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
                                if (node.getNodeName().equals(COMMENTAPI_LINK)) {
                                    commentLink = node.getFirstChild().getNodeValue();
                                }
                                if (node.getNodeName().equals(COMMENTAPI_TITLE)) {
                                    commentTitle = node.getFirstChild().getNodeValue();
                                }
                                if (node.getNodeName().equals(COMMENTAPI_AUTHOR)) {
                                    commentAuthor = node.getFirstChild().getNodeValue();
                                }
                                if (node.getNodeName().equals(COMMENTAPI_DESCRIPTION)) {
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

                    comment.setAuthor(commentAuthor);
                    comment.setAuthorEmail(commentEmail);
                    comment.setAuthorURL(commentLink);
                    comment.setComment(commentText);
                    comment.setCommentDate(new Date());
                    comment.setBlogEntry(blogEntry);

                    comment.save(blogUser);

                    _blojsomConfiguration.getEventBroadcaster().broadcastEvent(new CommentAddedEvent(this, new Date(), comment, blogUser));

                    // Send a Comment Email
                    sendCommentEmail(blogUser, blogEntry, comment);

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
     * Return a path appropriate for the Velocity file resource loader
     *
     * @param userId User ID
     * @return blojsom installation directory + base configuration directory + user id + templates directory
     */
    private String getVelocityFileLoaderPath(String userId) {
        StringBuffer fileLoaderPath = new StringBuffer();
        fileLoaderPath.append(_blojsomConfiguration.getInstallationDirectory());
        fileLoaderPath.append(BlojsomUtils.removeInitialSlash(_baseConfigurationDirectory));
        fileLoaderPath.append(userId).append("/");
        fileLoaderPath.append(BlojsomUtils.removeInitialSlash(_blojsomConfiguration.getTemplatesDirectory()));
        fileLoaderPath.append(", ");
        fileLoaderPath.append(_blojsomConfiguration.getInstallationDirectory());
        fileLoaderPath.append(BlojsomUtils.removeInitialSlash(_baseConfigurationDirectory));
        fileLoaderPath.append(BlojsomUtils.removeInitialSlash(_blojsomConfiguration.getTemplatesDirectory()));

        return fileLoaderPath.toString();
    }

    /**
     * Merge a given template for the user with the appropriate context
     *
     * @param template Template
     * @param user     {@link BlogUser} information
     * @param context  Context with objects for use in the template
     * @return Merged template or <code>null</code> if there was an error setting properties, loading the template, or merging
     *         the template
     */
    private String mergeTemplate(String template, BlogUser user, Map context) {
        // Create the Velocity Engine
        VelocityEngine velocityEngine = new VelocityEngine();

        try {
            Properties updatedVelocityProperties = (Properties) _velocityProperties.clone();
            updatedVelocityProperties.setProperty(VelocityEngine.FILE_RESOURCE_LOADER_PATH, getVelocityFileLoaderPath(user.getId()));
            velocityEngine.init(updatedVelocityProperties);
        } catch (Exception e) {
            _logger.error(e);

            return null;
        }

        StringWriter writer = new StringWriter();

        // Setup the VelocityContext
        VelocityContext velocityContext = new VelocityContext(context);

        if (!velocityEngine.templateExists(template)) {
            _logger.error("Could not find template for user: " + template);

            return null;
        } else {
            try {
                velocityEngine.mergeTemplate(template, UTF8, velocityContext, writer);
            } catch (Exception e) {
                _logger.error(e);

                return null;
            }
        }

        _logger.debug("Merged template: " + template);

        return writer.toString();
    }

    /**
     * Sends the comment as an Email to the Blog Author
     *
     * @param blogUser    {@link BlogUser} information
     * @param entry       {@link BlogEntry}
     * @param comment     {@link BlogComment}
     */
    private void sendCommentEmail(BlogUser blogUser, BlogEntry entry, BlogComment comment) {
        // Merge the template e-mail
        Map emailTemplateContext = new HashMap();
        emailTemplateContext.put(BLOJSOM_BLOG, blogUser.getBlog());
        emailTemplateContext.put(BLOJSOM_USER, blogUser);
        emailTemplateContext.put(CommentPlugin.BLOJSOM_COMMENT_PLUGIN_BLOG_COMMENT, comment);
        emailTemplateContext.put(CommentPlugin.BLOJSOM_COMMENT_PLUGIN_BLOG_ENTRY, entry);

        String emailComment = mergeTemplate(CommentPlugin.COMMENT_PLUGIN_EMAIL_TEMPLATE, blogUser, emailTemplateContext);

        try {
            String commentPrefix = blogUser.getBlog().getBlogProperty(CommentPlugin.COMMENT_PREFIX_IP);
            if (BlojsomUtils.checkNullOrBlank(commentPrefix)) {
                commentPrefix = CommentPlugin.DEFAULT_COMMENT_PREFIX;
            }

            EmailMessage emailMessage = new EmailMessage(commentPrefix + entry.getTitle(), emailComment);
            InternetAddress defaultRecipient = new InternetAddress(blogUser.getBlog().getBlogOwnerEmail(), blogUser.getBlog().getBlogOwner());
            EmailUtils.sendMailMessage(_mailsession, emailMessage, defaultRecipient);
        } catch (UnsupportedEncodingException e) {
            _logger.error(e);
        }
    }

    /**
     * Called when removing the servlet from the servlet container
     */
    public void destroy() {
    }
}

