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
package org.ignition.blojsom.extension.comment;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ignition.blojsom.blog.Blog;
import org.ignition.blojsom.blog.BlogComment;
import org.ignition.blojsom.blog.BlojsomConfigurationException;
import org.ignition.blojsom.plugin.comment.CommentUtils;
import org.ignition.blojsom.plugin.email.EmailMessage;
import org.ignition.blojsom.plugin.email.EmailUtils;
import org.ignition.blojsom.plugin.email.SendEmailPlugin;
import org.ignition.blojsom.util.BlojsomConstants;
import org.ignition.blojsom.util.BlojsomUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.Date;
import java.util.Properties;

/**
 * Blojsom Comment API Implementation
 *
 * Comment API Specification can be found at http://wellformedweb.org/story/9
 *
 * @author Mark Lussier
 * @version $Id: CommentAPIServlet.java,v 1.3 2003-04-20 16:39:29 intabulas Exp $
 */
public class CommentAPIServlet extends HttpServlet implements BlojsomConstants {

    private static final String BLOG_CONFIGURATION_IP = "blog-configuration";
    private static final String DEFAULT_BLOJSOM_CONFIGURATION = "/WEB-INF/blojsom.properties";


    private Log _logger = LogFactory.getLog(CommentAPIServlet.class);

    protected Blog _blog = null;
    protected String _commentsDirectory;
    protected String _blogHome;
    protected String[] _blogFileExtensions;
    protected String _blogUrlPrefix;
    private Session _mailsession = null;
    private String _defaultrecipientname;
    private String _defaultrecipientemail;

    private static final String COMMENTAPI_TITLE = "title";
    private static final String COMMENTAPI_LINK = "link";
    private static final String COMMENTAPI_DESCRIPTION = "description";
    private static final String COMMENTAPI_AUTHOR = "author";

    /**
     * Public Constructor
     */
    public CommentAPIServlet() {
    }


    /**
     * Load blojsom configuration information
     *
     * @param context Servlet context
     * @param filename blojsom configuration file to be loaded
     */
    public void processBlojsomCongfiguration(ServletContext context, String filename) {
        Properties _configuration = new Properties();
        InputStream _cis = context.getResourceAsStream(filename);

        try {
            _configuration.load(_cis);
            _cis.close();
            _blog = new Blog(_configuration);
            _commentsDirectory = (String) _blog.getBlogProperties().get(BLOG_COMMENTS_DIRECTORY_IP);
            _blogHome = (String) _blog.getBlogProperties().get(BlojsomConstants.BLOG_HOME_IP);
            _blogFileExtensions = (String[]) _blog.getBlogProperties().get(BlojsomConstants.BLOG_FILE_EXTENSIONS_IP);
            _blogUrlPrefix = (String) _blog.getBlogProperties().get(BlojsomConstants.BLOG_URL_IP);

            if (_blog.getBlogProperties().containsKey(BlojsomConstants.BLOG_OWNER_EMAIL)) {
                _defaultrecipientemail = (String) _blog.getBlogProperties().get(BlojsomConstants.BLOG_OWNER_EMAIL);
            }

            if (_blog.getBlogProperties().containsKey(BlojsomConstants.BLOG_OWNER)) {
                _defaultrecipientname = (String) _blog.getBlogProperties().get(BlojsomConstants.BLOG_OWNER);
            }


        } catch (IOException e) {
            _logger.error(e);
        } catch (BlojsomConfigurationException e) {
            _logger.error(e);
        }
    }


    /**
     * Initialize the blojsom XML-RPC servlet
     *
     * @param servletConfig Servlet configuration information
     * @throws javax.servlet.ServletException If there is an error initializing the servlet
     */
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);


        String _hostname = servletConfig.getInitParameter(SendEmailPlugin.SMTPSERVER_IP);
        if (_hostname != null) {
            Properties _props = new Properties();
            _props.put(SendEmailPlugin.SESSION_NAME, _hostname);
            _mailsession = Session.getInstance(_props);
        }

        String _cfgfile = servletConfig.getInitParameter(BLOG_CONFIGURATION_IP);

        if (_cfgfile == null || _cfgfile.equals("")) {
            _logger.info("blojsom configuration not specified, using " + DEFAULT_BLOJSOM_CONFIGURATION);
            _cfgfile = DEFAULT_BLOJSOM_CONFIGURATION;
        }

        processBlojsomCongfiguration(servletConfig.getServletContext(), _cfgfile);

    }


    /**
     * Service a Comment API request
     *
     * @param httpServletRequest Request
     * @param httpServletResponse Response
     * @throws ServletException If there is an error processing the request
     * @throws IOException If there is an error during I/O
     */
    protected void service(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {

        String commentAuthor = null;
        String commentEmail = null;
        String commentLink = null;
        String commentText = null;
        String commentTitle = null;

        if (_blog.getBlogCommentsEnabled().booleanValue() && httpServletRequest.getContentLength() > 0) {

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
            } catch (FactoryConfigurationError factoryConfigurationError) {
                _logger.error(factoryConfigurationError);
            } catch (SAXException e) {
                _logger.error(e);
            } catch (IOException e) {
                _logger.error(e);
            }


            /**
             * Try to extract an email address from "User Name <useremail@.com>" formatted string,
             * otherwise, just use the Name
             */
            if (commentAuthor != null) {
                try {
                    InternetAddress emailaddress = new InternetAddress(commentAuthor);
                    commentEmail = emailaddress.getAddress();
                    commentAuthor = emailaddress.getPersonal();
                } catch (AddressException e) {
                    // eat the exception since it's expected
                }
            } else {
                commentAuthor = "";
                commentEmail = "";
            }

            /* If the link is null, set it to an empty string */
            if (commentLink == null) {
                commentLink = "";
            }
            String requestedCategory = httpServletRequest.getPathInfo();
            if (requestedCategory == null) {
                requestedCategory = "/";
            } else if (!requestedCategory.endsWith("/")) {
                requestedCategory += "/";
            }

            if (commentText != null) {

                _logger.info("Comment API =================");
                _logger.info("  Category: " + requestedCategory);
                _logger.info(" Permalink: " + permalink);
                _logger.info("    Author: " + commentAuthor);
                _logger.info("    Author: " + commentEmail);
                _logger.info("      Link: " + commentLink);
                _logger.info("   Comment: \n" + commentText);

                /**
                 * Create new Comment Object
                 */
                BlogComment comment = new BlogComment();
                comment.setAuthor(commentAuthor);
                comment.setAuthorEmail(commentEmail);
                comment.setAuthorURL(commentLink);
                comment.setComment(commentText);
                comment.setCommentDate(new Date());

                /**
                 * Construct the Comment Filename
                 */
                String permalinkFilename = BlojsomUtils.getFilenameForPermalink(permalink, _blogFileExtensions);
                StringBuffer commentDirectory = new StringBuffer(_blogHome);
                commentDirectory.append(BlojsomUtils.removeInitialSlash(requestedCategory));
                commentDirectory.append(_commentsDirectory);
                commentDirectory.append(File.separator);
                commentDirectory.append(permalinkFilename);
                commentDirectory.append(File.separator);
                String hashedComment = BlojsomUtils.digestString(commentText).toUpperCase();
                String commentFilename = commentDirectory.toString() + hashedComment + BlojsomConstants.COMMENT_EXTENSION;
                File commentDir = new File(commentDirectory.toString());


                /* Ensure it exists */
                if (!commentDir.exists()) {
                    if (!commentDir.mkdirs()) {
                        _logger.error("Could not create directory for comments: " + commentDirectory);
                        httpServletResponse.setStatus(404);
                    }
                }

                /**
                 * Create the Comment Entry
                 */
                File commentEntry = new File(commentFilename);
                if (!commentEntry.exists()) {
                    try {
                        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(commentEntry), BlojsomConstants.UTF8));
                        bw.write(comment.getAuthor());
                        bw.newLine();
                        bw.write(comment.getAuthorEmail());
                        bw.newLine();
                        bw.write(comment.getAuthorURL());
                        bw.newLine();
                        bw.write(comment.getComment());
                        bw.newLine();
                        bw.close();
                        _logger.debug("Added blog comment: " + commentFilename);

                        /* Send a Comment Email */
                        sendCommentEmail(commentTitle, requestedCategory, permalink, commentAuthor, commentEmail,
                                commentLink, commentText);

                    } catch (IOException e) {
                        _logger.error(e);
                        httpServletResponse.setStatus(403);
                    }
                } else {
                    _logger.error("Duplicate comment submission detected, ignoring subsequent submission");
                    httpServletResponse.setStatus(403);
                }

                httpServletResponse.setStatus(200);
            } else {
                httpServletResponse.setStatus(403);
            }
        } else {
            httpServletResponse.setStatus(403);
        }

    }


    /**
     * Sends the comment as an Email to the Blog Author
     *
     * @param title Entry title that this comment is for
     * @param category Category for the entry
     * @param permalink Permalink to the origional entry
     * @param author Name of person commenting
     * @param authorEmail Email address of the person commenting
     * @param authorURL Homepage URL for the person commenting
     * @param userComment The comment
     */
    private synchronized void sendCommentEmail(String title, String category, String permalink, String author,
                                               String authorEmail, String authorURL, String userComment) {

        String url = _blogUrlPrefix + BlojsomUtils.removeInitialSlash(category);
        String commentMessage = CommentUtils.constructCommentEmail(permalink, author, authorEmail, authorURL, userComment,
                url);
        try {
            EmailMessage emailMessage = new EmailMessage("[blojsom] Comment on: " + title, commentMessage);
            InternetAddress defaultRecipient = new InternetAddress(_defaultrecipientemail, _defaultrecipientname);
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

