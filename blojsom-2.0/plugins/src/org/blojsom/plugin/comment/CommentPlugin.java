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
package org.blojsom.plugin.comment;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.blojsom.BlojsomException;
import org.blojsom.blog.*;
import org.blojsom.event.BlojsomEvent;
import org.blojsom.event.BlojsomListener;
import org.blojsom.fetcher.BlojsomFetcher;
import org.blojsom.fetcher.BlojsomFetcherException;
import org.blojsom.plugin.BlojsomPluginException;
import org.blojsom.plugin.comment.event.CommentAddedEvent;
import org.blojsom.plugin.comment.event.CommentResponseSubmissionEvent;
import org.blojsom.plugin.common.VelocityPlugin;
import org.blojsom.plugin.email.EmailConstants;
import org.blojsom.util.BlojsomConstants;
import org.blojsom.util.BlojsomMetaDataConstants;
import org.blojsom.util.BlojsomUtils;
import org.blojsom.util.CookieUtils;

import javax.mail.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * CommentPlugin
 *
 * @author David Czarnecki
 * @version $Id: CommentPlugin.java,v 1.44 2006-02-22 18:51:19 czarneckid Exp $
 */
public class CommentPlugin extends VelocityPlugin implements BlojsomMetaDataConstants, BlojsomListener, EmailConstants {

    private Log _logger = LogFactory.getLog(CommentPlugin.class);

    /**
     * Template for comment e-mails
     */
    public static final String COMMENT_PLUGIN_EMAIL_TEMPLATE = "org/blojsom/plugin/comment/comment-plugin-email-template.vm";
    public static final String COMMENT_PLUGIN_EMAIL_TEMPLATE_TEXT = "org/blojsom/plugin/comment/comment-plugin-email-template-text.vm";
    public static final String COMMENT_PLUGIN_EMAIL_TEMPLATE_HTML = "org/blojsom/plugin/comment/comment-plugin-email-template-html.vm";

    /**
     * Default prefix for comment e-mail notification
     */
    public static final String DEFAULT_COMMENT_PREFIX = "[blojsom] Comment on: ";

    /**
     * Initialization parameter for e-mail prefix
     */
    public static final String COMMENT_PREFIX_IP = "plugin-comment-email-prefix";

    /**
     * Initialization parameter to do plugin autoformatting
     */
    public static final String COMMENT_AUTOFORMAT_IP = "plugin-comment-autoformat";

    /**
     * Initialization parameter for the duration of the "remember me" cookies
     */
    public static final String COMMENT_COOKIE_EXPIRATION_DURATION_IP = "plugin-comment-expiration-duration";

    /**
     * Initialization parameter for the throttling of comments from IP addresses
     */
    public static final String COMMENT_THROTTLE_MINUTES_IP = "plugin-comment-throttle";

    /**
     * Initialization parameter for disabling comments on entries after a certain number of days
     */
    public static final String COMMENT_DAYS_EXPIRATION_IP = "plugin-comment-days-expiration";

    /**
     * Default throttle value for comments from a particular IP address
     */
    private static final int COMMENT_THROTTLE_DEFAULT_MINUTES = 5;

    /**
     * Request parameter for the "comment"
     */
    public static final String COMMENT_PARAM = "comment";

    /**
     * Request parameter for the "author"
     */
    public static final String AUTHOR_PARAM = "author";

    /**
     * Request parameter for the "authorEmail"
     */
    public static final String AUTHOR_EMAIL_PARAM = "authorEmail";

    /**
     * Request parameter for the "authorURL"
     */
    public static final String AUTHOR_URL_PARAM = "authorURL";

    /**
     * Request parameter for the "commentText"
     */
    public static final String COMMENT_TEXT_PARAM = "commentText";

    /**
     * Request parameter to "remember" the poster
     */
    private static final String REMEMBER_ME_PARAM = "remember";

    /**
     * Comment "Remember Me" Cookie for the Authors Name
     */
    private static final String COOKIE_AUTHOR = "blojsom.cookie.author";

    /**
     * Comment "Remember Me" Cookie for the Authors Email
     */
    private static final String COOKIE_EMAIL = "blojsom.cookie.authorEmail";

    /**
     * Comment "Remember Me" Cookie for the Authors URL
     */
    private static final String COOKIE_URL = "blojsom.cookie.authorURL";

    /**
     * Comment "Remember Me" Cookie for the "Remember Me" checkbox
     */
    private static final String COOKIE_REMEMBER_ME = "blojsom.cookie.rememberme";

    /**
     * Expiration age for the cookie (1 week)
     */
    private static final int COOKIE_EXPIRATION_AGE = 604800;

    /**
     * Key under which the indicator this plugin is "live" will be placed
     * (example: on the request for the JSPDispatcher)
     */
    public static final String BLOJSOM_COMMENT_PLUGIN_ENABLED = "BLOJSOM_COMMENT_PLUGIN_ENABLED";

    /**
     * Key under which the author from the "remember me" cookie will be placed
     * (example: on the request for the JSPDispatcher)
     */
    public static final String BLOJSOM_COMMENT_PLUGIN_AUTHOR = "BLOJSOM_COMMENT_PLUGIN_AUTHOR";

    /**
     * Key under which the author's e-mail from the "remember me" cookie will be placed
     * (example: on the request for the JSPDispatcher)
     */
    public static final String BLOJSOM_COMMENT_PLUGIN_AUTHOR_EMAIL = "BLOJSOM_COMMENT_PLUGIN_AUTHOR_EMAIL";

    /**
     * Key under which the author's URL from the "remember me" cookie will be placed
     * (example: on the request for the JSPDispatcher)
     */
    public static final String BLOJSOM_COMMENT_PLUGIN_AUTHOR_URL = "BLOJSOM_COMMENT_PLUGIN_AUTHOR_URL";

    /**
     * Key under which the "remember me" checkbox from the "remember me" cookie will be placed
     * (example: on the request for the JSPDispatcher)
     */
    public static final String BLOJSOM_COMMENT_PLUGIN_REMEMBER_ME = "BLOJSOM_COMMENT_PLUGIN_REMEMBER_ME";

    /**
     * IP address meta-data
     */
    public static final String BLOJSOM_COMMENT_PLUGIN_METADATA_IP = "BLOJSOM_COMMENT_PLUGIN_METADATA_IP";

    /**
     * Key under which the blog entry will be placed for merging the comment e-mail
     */
    public static final String BLOJSOM_COMMENT_PLUGIN_BLOG_ENTRY = "BLOJSOM_COMMENT_PLUGIN_BLOG_ENTRY";

    /**
     * Key under which the blog comment will be placed for merging the comment e-mail
     */
    public static final String BLOJSOM_COMMENT_PLUGIN_BLOG_COMMENT = "BLOJSOM_COMMENT_PLUGIN_BLOG_COMMENT";

    public static final String BLOJSOM_PLUGIN_COMMENT_METADATA = "BLOJSOM_PLUGIN_COMMENT_METADATA";

    public static final String BLOJSOM_PLUGIN_COMMENT_METADATA_DESTROY = "BLOJSOM_PLUGIN_COMMENT_METADATA_DESTROY";

    private Map _ipAddressCommentTimes;
    private BlojsomFetcher _fetcher;
    private String _mailServer;
    private String _mailServerUsername;
    private String _mailServerPassword;
    private Session _session;

    private BlojsomConfiguration _configuration;

    /**
     * Initialize this plugin. This method only called when the plugin is instantiated.
     *
     * @param servletConfig        Servlet config object for the plugin to retrieve any initialization parameters
     * @param blojsomConfiguration {@link BlojsomConfiguration} information
     * @throws BlojsomPluginException If there is an error initializing the plugin
     */
    public void init(ServletConfig servletConfig, BlojsomConfiguration blojsomConfiguration) throws BlojsomPluginException {
        super.init(servletConfig, blojsomConfiguration);

        _configuration = blojsomConfiguration;

        _ipAddressCommentTimes = new HashMap(10);
        String fetcherClassName = blojsomConfiguration.getFetcherClass();
        try {
            Class fetcherClass = Class.forName(fetcherClassName);
            _fetcher = (BlojsomFetcher) fetcherClass.newInstance();
            _fetcher.init(servletConfig, blojsomConfiguration);
            _logger.info("Added blojsom fetcher: " + fetcherClassName);
        } catch (ClassNotFoundException e) {
            _logger.error(e);
            throw new BlojsomPluginException(e);
        } catch (InstantiationException e) {
            _logger.error(e);
            throw new BlojsomPluginException(e);
        } catch (IllegalAccessException e) {
            _logger.error(e);
            throw new BlojsomPluginException(e);
        } catch (BlojsomFetcherException e) {
            _logger.error(e);
            throw new BlojsomPluginException(e);
        }

        _mailServer = servletConfig.getInitParameter(SMTPSERVER_IP);

        if (_mailServer != null) {
            if (_mailServer.startsWith("java:comp/env")) {
                try {
                    Context context = new InitialContext();
                    _session = (Session) context.lookup(_mailServer);
                } catch (NamingException e) {
                    _logger.error(e);
                    throw new BlojsomPluginException(e);
                }
            } else {
                _mailServerUsername = servletConfig.getInitParameter(SMTPSERVER_USERNAME_IP);
                _mailServerPassword = servletConfig.getInitParameter(SMTPSERVER_PASSWORD_IP);
            }
        } else {
            throw new BlojsomPluginException("Missing SMTP servername servlet initialization parameter: " + SMTPSERVER_IP);
        }

        blojsomConfiguration.getEventBroadcaster().addListener(this);
    }

    /**
     * Process the blog entries
     *
     * @param httpServletRequest  Request
     * @param httpServletResponse Response
     * @param user                {@link BlogUser} instance
     * @param context             Context
     * @param entries             Blog entries retrieved for the particular request
     * @return Modified set of blog entries
     * @throws BlojsomPluginException If there is an error processing the blog entries
     */
    public BlogEntry[] process(HttpServletRequest httpServletRequest,
                               HttpServletResponse httpServletResponse,
                               BlogUser user,
                               Map context,
                               BlogEntry[] entries) throws BlojsomPluginException {
        Blog blog = user.getBlog();
        context.put(BLOJSOM_COMMENT_PLUGIN_ENABLED, blog.getBlogCommentsEnabled());
        if (!blog.getBlogCommentsEnabled().booleanValue()) {
            _logger.debug("blog comments not enabled for user: " + user.getId());
            return entries;
        }

        String bannedIPListParam = blog.getBlogProperty(BANNED_IP_ADDRESSES_IP);
        String[] bannedIPList;
        if (bannedIPListParam == null) {
            bannedIPList = null;
            _logger.info("Blog configuration parameter not supplied for: " + BANNED_IP_ADDRESSES_IP);
        } else {
            bannedIPList = BlojsomUtils.parseCommaList(bannedIPListParam);
        }

        Boolean _blogCommentsEnabled;
        _blogCommentsEnabled = blog.getBlogCommentsEnabled();

        int _cookieExpiration;
        String cookieExpiration = blog.getBlogProperty(COMMENT_COOKIE_EXPIRATION_DURATION_IP);
        if (BlojsomUtils.checkNullOrBlank(cookieExpiration)) {
            _cookieExpiration = COOKIE_EXPIRATION_AGE;
        } else {
            try {
                _cookieExpiration = Integer.parseInt(cookieExpiration);
            } catch (NumberFormatException e) {
                _cookieExpiration = COOKIE_EXPIRATION_AGE;
            }
        }

        if (entries.length == 0) {
            return entries;
        }

        // Check for a comment from a banned IP address
        String remoteIPAddress = httpServletRequest.getRemoteAddr();
        if (isIPBanned(bannedIPList, remoteIPAddress)) {
            _logger.debug("Attempted comment from banned IP address: " + remoteIPAddress);
            return entries;
        }

        String author = httpServletRequest.getParameter(AUTHOR_PARAM);
        String authorEmail = httpServletRequest.getParameter(AUTHOR_EMAIL_PARAM);
        String authorURL = httpServletRequest.getParameter(AUTHOR_URL_PARAM);
        String rememberMe = httpServletRequest.getParameter(REMEMBER_ME_PARAM);

        // Check to see if the person has requested they be "remembered" and if so
        // extract their information from the appropriate cookies
        Cookie authorCookie = CookieUtils.getCookie(httpServletRequest, COOKIE_AUTHOR);
        if ((authorCookie != null) && ((author == null) || "".equals(author))) {
            author = authorCookie.getValue();
            _logger.debug("Pulling author from cookie: " + author);
            if ("".equals(author)) {
                author = null;
            } else {
                context.put(BLOJSOM_COMMENT_PLUGIN_AUTHOR, author);
            }

            Cookie authorEmailCookie = CookieUtils.getCookie(httpServletRequest, COOKIE_EMAIL);
            if ((authorEmailCookie != null) && ((authorEmail == null) || "".equals(authorEmail))) {
                authorEmail = authorEmailCookie.getValue();
                _logger.debug("Pulling author email from cookie: " + authorEmail);
                if (authorEmail == null) {
                    authorEmail = "";
                } else {
                    context.put(BLOJSOM_COMMENT_PLUGIN_AUTHOR_EMAIL, authorEmail);
                }
            }

            Cookie authorUrlCookie = CookieUtils.getCookie(httpServletRequest, COOKIE_URL);
            if ((authorUrlCookie != null) && ((authorURL == null) || "".equals(authorURL))) {
                authorURL = authorUrlCookie.getValue();
                _logger.debug("Pulling author URL from cookie: " + authorURL);
                if (authorURL == null) {
                    authorURL = "";
                } else {
                    context.put(BLOJSOM_COMMENT_PLUGIN_AUTHOR_URL, authorURL);
                }
            }

            Cookie rememberMeCookie = CookieUtils.getCookie(httpServletRequest, COOKIE_REMEMBER_ME);
            if ((rememberMeCookie != null) && ((rememberMe == null) || "".equals(rememberMe))) {
                rememberMe = rememberMeCookie.getValue();
                if (rememberMe != null) {
                    context.put(BLOJSOM_COMMENT_PLUGIN_REMEMBER_ME, rememberMe);
                }
            }
        }

        // Comment handling
        if ("y".equalsIgnoreCase(httpServletRequest.getParameter(COMMENT_PARAM)) && _blogCommentsEnabled.booleanValue())
        {
            String commentText = httpServletRequest.getParameter(COMMENT_TEXT_PARAM);
            String permalink = httpServletRequest.getParameter(BlojsomConstants.PERMALINK_PARAM);
            String category = httpServletRequest.getParameter(BlojsomConstants.CATEGORY_PARAM);
            category = BlojsomUtils.urlDecode(category);
            String remember = httpServletRequest.getParameter(REMEMBER_ME_PARAM);

            if ((author != null && !"".equals(author)) && (commentText != null && !"".equals(commentText))
                    && (permalink != null && !"".equals(permalink)) && (category != null && !"".equals(category))) {

                // Check for comment throttling
                String commentThrottleValue = blog.getBlogProperty(COMMENT_THROTTLE_MINUTES_IP);
                if (!BlojsomUtils.checkNullOrBlank(commentThrottleValue)) {
                    int commentThrottleMinutes;

                    try {
                        commentThrottleMinutes = Integer.parseInt(commentThrottleValue);
                    } catch (NumberFormatException e) {
                        commentThrottleMinutes = COMMENT_THROTTLE_DEFAULT_MINUTES;
                    }
                    _logger.debug("Comment throttling enabled at: " + commentThrottleMinutes + " minutes");

                    remoteIPAddress = httpServletRequest.getRemoteAddr();
                    if (_ipAddressCommentTimes.containsKey(remoteIPAddress)) {
                        Calendar currentTime = Calendar.getInstance();
                        Calendar timeOfLastComment = (Calendar) _ipAddressCommentTimes.get(remoteIPAddress);
                        long timeDifference = currentTime.getTimeInMillis() - timeOfLastComment.getTimeInMillis();

                        long differenceInMinutes = timeDifference / (60 * 1000);
                        if (differenceInMinutes < commentThrottleMinutes) {
                            _logger.debug("Comment throttle enabled. Comment from IP address: " + remoteIPAddress + " in less than " + commentThrottleMinutes + " minutes");

                            return entries;
                        } else {
                            _logger.debug("Comment throttle enabled. Resetting date of last comment to current time");
                            _ipAddressCommentTimes.put(remoteIPAddress, currentTime);
                        }
                    } else {
                        Calendar calendar = Calendar.getInstance();
                        _ipAddressCommentTimes.put(remoteIPAddress, calendar);
                    }
                }

                author = author.trim();
                commentText = commentText.trim();

                // Check if autoformatting of comment text should be done
                boolean autoformatComments = Boolean.valueOf(blog.getBlogProperty(COMMENT_AUTOFORMAT_IP)).booleanValue();
                if (autoformatComments) {
                    commentText = BlojsomUtils.replace(commentText, "\n", "<br />");
                }

                if (authorEmail != null) {
                    authorEmail = authorEmail.trim();
                } else {
                    authorEmail = "";
                }

                if (authorURL != null) {
                    authorURL = authorURL.trim();
                } else {
                    authorURL = "";
                }

                if (!category.endsWith("/")) {
                    category += "/";
                }

                if ((authorURL != null) && (!"".equals(authorURL))) {
                    if (!authorURL.toLowerCase().startsWith("http://")) {
                        authorURL = "http://" + authorURL;
                    }
                }

                // Check to see if comments have been disabled for this blog entry
                BlogCategory blogCategory = _fetcher.newBlogCategory();
                blogCategory.setCategory(category);
                blogCategory.setCategoryURL(user.getBlog().getBlogURL() + BlojsomUtils.removeInitialSlash(category));

                Map fetchMap = new HashMap();
                fetchMap.put(BlojsomFetcher.FETCHER_CATEGORY, blogCategory);
                fetchMap.put(BlojsomFetcher.FETCHER_PERMALINK, permalink);

                try {
                    BlogEntry[] fetchedEntries = _fetcher.fetchEntries(fetchMap, user);
                    if (fetchedEntries.length > 0) {
                        BlogEntry entry = fetchedEntries[0];
                        if (BlojsomUtils.checkMapForKey(entry.getMetaData(), BLOG_METADATA_COMMENTS_DISABLED)) {
                            _logger.debug("Comments have been disabled for blog entry: " + entry.getId());

                            return entries;
                        }

                        // Check for a comment where the number of days between comment auto-expiration has passed
                        String commentDaysExpiration = blog.getBlogProperty(COMMENT_DAYS_EXPIRATION_IP);
                        if (!BlojsomUtils.checkNullOrBlank(commentDaysExpiration)) {
                            try {
                                int daysExpiration = Integer.parseInt(commentDaysExpiration);
                                int daysBetweenDates = BlojsomUtils.daysBetweenDates(entry.getDate(), new Date());
                                if ((daysExpiration > 0) && (daysBetweenDates >= daysExpiration)) {
                                    _logger.debug("Comment period for this entry has expired. Expiration period set at " + daysExpiration + " days. Difference in days: " + daysBetweenDates);

                                    return entries;
                                }
                            } catch (NumberFormatException e) {
                                _logger.error("Error in parameter " + COMMENT_DAYS_EXPIRATION_IP + ": " + commentDaysExpiration);
                            }
                        }
                    }
                } catch (BlojsomFetcherException e) {
                    _logger.error(e);
                }

                Map commentMetaData = new HashMap();
                commentMetaData.put(BLOJSOM_COMMENT_PLUGIN_METADATA_IP, remoteIPAddress);

                // Check to see if a previous plugin populated meta-data for the comment
                if (context.containsKey(BLOJSOM_PLUGIN_COMMENT_METADATA)) {
                    Map metaData = (Map) context.get(BLOJSOM_PLUGIN_COMMENT_METADATA);

                    Iterator metaDataKeys = metaData.keySet().iterator();
                    Object key;
                    Object value;
                    while (metaDataKeys.hasNext()) {
                        key = metaDataKeys.next();
                        value = metaData.get(key);
                        commentMetaData.put(key, value);
                    }
                }

                CommentResponseSubmissionEvent commentResponseSubmissionEvent = new CommentResponseSubmissionEvent(this, new Date(), user, httpServletRequest, httpServletResponse, author, authorEmail, authorURL, commentText, entries[0], commentMetaData);
                _configuration.getEventBroadcaster().processEvent(commentResponseSubmissionEvent);
                author = commentResponseSubmissionEvent.getSubmitter();
                authorEmail = commentResponseSubmissionEvent.getSubmitterItem1();
                authorURL = commentResponseSubmissionEvent.getSubmitterItem2();
                commentText = commentResponseSubmissionEvent.getContent();

                // Check to see if the comment should be destroyed (not saved) automatically
                if (!commentMetaData.containsKey(BLOJSOM_PLUGIN_COMMENT_METADATA_DESTROY)) {
                    BlogComment _comment = addBlogComment(author, authorEmail, authorURL,
                            commentText, _blogCommentsEnabled.booleanValue(), commentMetaData, user, entries[0]);

                    // For persisting the Last-Modified time
                    context.put(BlojsomConstants.BLOJSOM_LAST_MODIFIED, new Long(new Date().getTime()));

                    if (_comment != null) {
                        try {
                            entries[0].load(user);
                        } catch (BlojsomException e) {
                            _logger.error(e);
                        }

                        _configuration.getEventBroadcaster().broadcastEvent(new CommentAddedEvent(this, new Date(), _comment, user));
                    }
                } else {
                    _logger.info("Comment meta-data contained destroy key. Comment was not saved");
                }

                // If we're asked to remember the person, then add the appropriate cookies
                if ((remember != null) && (!"".equals(remember))) {
                    CookieUtils.addCookie(httpServletResponse, _cookieExpiration, COOKIE_AUTHOR, author);
                    context.put(BLOJSOM_COMMENT_PLUGIN_AUTHOR, author);
                    CookieUtils.addCookie(httpServletResponse, _cookieExpiration, COOKIE_EMAIL, authorEmail);
                    context.put(BLOJSOM_COMMENT_PLUGIN_AUTHOR_EMAIL, authorEmail);
                    CookieUtils.addCookie(httpServletResponse, _cookieExpiration, COOKIE_URL, authorURL);
                    context.put(BLOJSOM_COMMENT_PLUGIN_AUTHOR_URL, authorURL);
                    CookieUtils.addCookie(httpServletResponse, _cookieExpiration, COOKIE_REMEMBER_ME, "true");
                    context.put(BLOJSOM_COMMENT_PLUGIN_REMEMBER_ME, "true");
                }
            }
        }

        return entries;
    }

    /**
     * Add a comment to a particular blog entry
     *
     * @param author      Comment author
     * @param authorEmail Comment author e-mail
     * @param authorURL   Comment author URL
     * @param userComment Comment
     * @return BlogComment Entry
     */
    private BlogComment addBlogComment(String author,
                                       String authorEmail, String authorURL, String userComment,
                                       boolean blogCommentsEnabled, Map commentMetaData, BlogUser blogUser, BlogEntry blogEntry) {
        BlogComment comment = null;

        if (blogCommentsEnabled) {

            //Escape out any HTML in the post;
            userComment = BlojsomUtils.escapeMetaAndLink(userComment);

            try {
                comment = _fetcher.newBlogComment();
                comment.setBlogEntry(blogEntry);
                comment.setAuthor(author);
                comment.setAuthorEmail(authorEmail);
                comment.setAuthorURL(authorURL);
                comment.setComment(userComment);
                comment.setCommentDate(new Date());
                comment.setMetaData(commentMetaData);
                comment.save(blogUser);
            } catch (BlojsomException e) {
                _logger.error(e);

                comment = null;
            }
        }

        return comment;
    }

    /**
     * Perform any cleanup for the plugin. Called after {@link #process}.
     *
     * @throws BlojsomPluginException If there is an error performing cleanup for this plugin
     */
    public void cleanup() throws BlojsomPluginException {
    }

    /**
     * Called when BlojsomServlet is taken out of service
     *
     * @throws BlojsomPluginException If there is an error in finalizing this plugin
     */
    public void destroy() throws BlojsomPluginException {
    }

    /**
     * Setup the comment e-mail
     *
     * @param blog  {@link Blog} information
     * @param email Email message
     * @throws EmailException If there is an error preparing the e-mail message
     */
    protected void setupEmail(Blog blog, BlogEntry entry, Email email) throws EmailException {
        email.setCharset(UTF8);

        // If we have a mail session for the environment, use that
        if (_session != null) {
            email.setMailSession(_session);
        } else {
            // Otherwise, if there is a username and password for the mail server, use that
            if (!BlojsomUtils.checkNullOrBlank(_mailServerUsername) && !BlojsomUtils.checkNullOrBlank(_mailServerPassword)) {
                email.setHostName(_mailServer);
                email.setAuthentication(_mailServerUsername, _mailServerPassword);
            } else {
                email.setHostName(_mailServer);
            }
        }

        email.setFrom(blog.getBlogOwnerEmail(), "Blojsom Comment");

        String author;
        if (entry.getMetaData().get(BlojsomMetaDataConstants.BLOG_ENTRY_METADATA_AUTHOR) != null) {
            author = (String) entry.getMetaData().get(BlojsomMetaDataConstants.BLOG_ENTRY_METADATA_AUTHOR);
        } else {
            author = blog.getBlogOwner();
        }

        String authorEmail = blog.getBlogOwnerEmail();

        if (author != null) {
            authorEmail = blog.getAuthorizedUserEmail(author);
            if (BlojsomUtils.checkNullOrBlank(authorEmail)) {
                authorEmail = blog.getBlogOwnerEmail();
            }
        }

        email.addTo(authorEmail, author);
        email.setSentDate(new Date());
    }

    /**
     * Handle an event broadcast from another component
     *
     * @param event {@link org.blojsom.event.BlojsomEvent} to be handled
     */
    public void handleEvent(BlojsomEvent event) {
        if (event instanceof CommentAddedEvent) {
            HtmlEmail email = new HtmlEmail();
            CommentAddedEvent commentAddedEvent = (CommentAddedEvent) event;

            if (commentAddedEvent.getBlogUser().getBlog().getBlogEmailEnabled().booleanValue()) {
                try {
                    setupEmail(commentAddedEvent.getBlogUser().getBlog(), commentAddedEvent.getBlogEntry(), email);

                    Map emailTemplateContext = new HashMap();
                    emailTemplateContext.put(BLOJSOM_BLOG, commentAddedEvent.getBlogUser().getBlog());
                    emailTemplateContext.put(BLOJSOM_USER, commentAddedEvent.getBlogUser());
                    emailTemplateContext.put(BLOJSOM_COMMENT_PLUGIN_BLOG_COMMENT, commentAddedEvent.getBlogComment());
                    emailTemplateContext.put(BLOJSOM_COMMENT_PLUGIN_BLOG_ENTRY, commentAddedEvent.getBlogEntry());

                    String htmlText = mergeTemplate(COMMENT_PLUGIN_EMAIL_TEMPLATE_HTML, commentAddedEvent.getBlogUser(), emailTemplateContext);
                    String plainText = mergeTemplate(COMMENT_PLUGIN_EMAIL_TEMPLATE_TEXT, commentAddedEvent.getBlogUser(), emailTemplateContext);

                    email.setHtmlMsg(htmlText);
                    email.setTextMsg(plainText);

                    String emailPrefix = commentAddedEvent.getBlogUser().getBlog().getBlogProperty(COMMENT_PREFIX_IP);
                    if (BlojsomUtils.checkNullOrBlank(emailPrefix)) {
                        emailPrefix = DEFAULT_COMMENT_PREFIX;
                    }

                    email = (HtmlEmail) email.setSubject(emailPrefix + commentAddedEvent.getBlogEntry().getTitle());

                    email.send();
                } catch (EmailException e) {
                    _logger.error(e);
                }
            }
        }
    }

    /**
     * Process an event from another component
     *
     * @param event {@link org.blojsom.event.BlojsomEvent} to be handled
     */
    public void processEvent(BlojsomEvent event) {
    }
}
