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
import org.blojsom.blog.Blog;
import org.blojsom.blog.Comment;
import org.blojsom.blog.Entry;
import org.blojsom.blog.User;
import org.blojsom.event.Event;
import org.blojsom.event.EventBroadcaster;
import org.blojsom.event.Listener;
import org.blojsom.fetcher.Fetcher;
import org.blojsom.fetcher.FetcherException;
import org.blojsom.plugin.PluginException;
import org.blojsom.plugin.comment.event.CommentAddedEvent;
import org.blojsom.plugin.comment.event.CommentResponseSubmissionEvent;
import org.blojsom.plugin.common.ResponseConstants;
import org.blojsom.plugin.email.EmailConstants;
import org.blojsom.plugin.velocity.StandaloneVelocityPlugin;
import org.blojsom.util.BlojsomConstants;
import org.blojsom.util.BlojsomUtils;
import org.blojsom.util.CookieUtils;

import javax.mail.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.io.IOException;

/**
 * CommentPlugin
 *
 * @author David Czarnecki
 * @version $Id: CommentPlugin.java,v 1.8 2006-05-28 21:12:21 czarneckid Exp $
 * @since blojsom 3.0
 */
public class CommentPlugin extends StandaloneVelocityPlugin implements Listener {

    private Log _logger = LogFactory.getLog(CommentPlugin.class);

    /**
     * Template for comment e-mails
     */
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
     * Form item, comment parent ID
     */
    private static final String COMMENT_PARENT_ID = "comment_parent_id";

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
    private String _mailServer;
    private String _mailServerUsername;
    private String _mailServerPassword;
    private Session _session;
    private Fetcher _fetcher;
    protected EventBroadcaster _eventBroadcaster;

    /**
     * Set the {@link EventBroadcaster} event broadcaster
     *
     * @param eventBroadcaster {@link EventBroadcaster}
     */
    public void setEventBroadcaster(EventBroadcaster eventBroadcaster) {
        _eventBroadcaster = eventBroadcaster;
    }

    /**
     * Set the {@link Fetcher}
     *
     * @param fetcher {@link Fetcher}
     */
    public void setFetcher(Fetcher fetcher) {
        _fetcher = fetcher;
    }

    /**
     * Initialize this plugin. This method only called when the plugin is instantiated.
     *
     * @throws PluginException If there is an error initializing the plugin
     */
    public void init() throws PluginException {
        super.init();

        _mailServer = _servletConfig.getInitParameter(EmailConstants.SMTPSERVER_IP);

        if (_mailServer != null) {
            if (_mailServer.startsWith("java:comp/env")) {
                try {
                    Context context = new InitialContext();
                    _session = (Session) context.lookup(_mailServer);
                } catch (NamingException e) {
                    if (_logger.isErrorEnabled()) {
                        _logger.error(e);
                    }

                    throw new PluginException(e);
                }
            } else {
                _mailServerUsername = _servletConfig.getInitParameter(EmailConstants.SMTPSERVER_USERNAME_IP);
                _mailServerPassword = _servletConfig.getInitParameter(EmailConstants.SMTPSERVER_PASSWORD_IP);
            }
        } else {
            if (_logger.isErrorEnabled()) {
                _logger.error("Missing SMTP servername servlet initialization parameter: " + EmailConstants.SMTPSERVER_IP);
            }
        }

        _ipAddressCommentTimes = new WeakHashMap();
        _eventBroadcaster.addListener(this);
    }

    /**
     * Process the blog entries
     *
     * @param httpServletRequest  Request
     * @param httpServletResponse Response
     * @param blog                {@link Blog} instance
     * @param context             Context
     * @param entries             Blog entries retrieved for the particular request
     * @return Modified set of blog entries
     * @throws PluginException If there is an error processing the blog entries
     */
    public Entry[] process(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Blog blog, Map context, Entry[] entries) throws PluginException {
        context.put(BLOJSOM_COMMENT_PLUGIN_ENABLED, blog.getBlogCommentsEnabled());
        if (!blog.getBlogCommentsEnabled().booleanValue()) {
            if (_logger.isDebugEnabled()) {
                _logger.debug("Comments not enabled for blog: " + blog.getBlogId());
            }

            return entries;
        }

        Boolean _blogCommentsEnabled;
        _blogCommentsEnabled = blog.getBlogCommentsEnabled();

        int _cookieExpiration;
        String cookieExpiration = blog.getProperty(COMMENT_COOKIE_EXPIRATION_DURATION_IP);
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

        String author = httpServletRequest.getParameter(AUTHOR_PARAM);
        String authorEmail = httpServletRequest.getParameter(AUTHOR_EMAIL_PARAM);
        String authorURL = httpServletRequest.getParameter(AUTHOR_URL_PARAM);
        String rememberMe = httpServletRequest.getParameter(REMEMBER_ME_PARAM);

        // Check to see if the person has requested they be "remembered" and if so
        // extract their information from the appropriate cookies
        Cookie authorCookie = CookieUtils.getCookie(httpServletRequest, COOKIE_AUTHOR);
        if ((authorCookie != null) && ((author == null) || "".equals(author))) {
            author = authorCookie.getValue();
            if (_logger.isDebugEnabled()) {
                _logger.debug("Pulling author from cookie: " + author);
            }

            if ("".equals(author)) {
                author = null;
            } else {
                context.put(BLOJSOM_COMMENT_PLUGIN_AUTHOR, author);
            }

            Cookie authorEmailCookie = CookieUtils.getCookie(httpServletRequest, COOKIE_EMAIL);
            if ((authorEmailCookie != null) && ((authorEmail == null) || "".equals(authorEmail))) {
                authorEmail = authorEmailCookie.getValue();
                if (_logger.isDebugEnabled()) {
                    _logger.debug("Pulling author email from cookie: " + authorEmail);
                }

                if (authorEmail == null) {
                    authorEmail = "";
                } else {
                    context.put(BLOJSOM_COMMENT_PLUGIN_AUTHOR_EMAIL, authorEmail);
                }
            }

            Cookie authorUrlCookie = CookieUtils.getCookie(httpServletRequest, COOKIE_URL);
            if ((authorUrlCookie != null) && ((authorURL == null) || "".equals(authorURL))) {
                authorURL = authorUrlCookie.getValue();
                if (_logger.isDebugEnabled()) {
                    _logger.debug("Pulling author URL from cookie: " + authorURL);
                }

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

        String remoteIPAddress = httpServletRequest.getRemoteAddr();

        // Comment handling
        if ("y".equalsIgnoreCase(httpServletRequest.getParameter(COMMENT_PARAM)) && _blogCommentsEnabled.booleanValue()) {
            String commentText = httpServletRequest.getParameter(COMMENT_TEXT_PARAM);
            String remember = httpServletRequest.getParameter(REMEMBER_ME_PARAM);

            if ((author != null && !"".equals(author)) && (commentText != null && !"".equals(commentText))) {
                // Check for comment throttling
                String commentThrottleValue = blog.getProperty(COMMENT_THROTTLE_MINUTES_IP);
                if (!BlojsomUtils.checkNullOrBlank(commentThrottleValue)) {
                    int commentThrottleMinutes;

                    try {
                        commentThrottleMinutes = Integer.parseInt(commentThrottleValue);
                    } catch (NumberFormatException e) {
                        commentThrottleMinutes = COMMENT_THROTTLE_DEFAULT_MINUTES;
                    }
                    if (_logger.isDebugEnabled()) {
                        _logger.debug("Comment throttling enabled at: " + commentThrottleMinutes + " minutes");
                    }

                    if (_ipAddressCommentTimes.containsKey(remoteIPAddress)) {
                        Calendar currentTime = Calendar.getInstance();
                        Calendar timeOfLastComment = (Calendar) _ipAddressCommentTimes.get(remoteIPAddress);
                        long timeDifference = currentTime.getTimeInMillis() - timeOfLastComment.getTimeInMillis();

                        long differenceInMinutes = timeDifference / (60 * 1000);
                        if (differenceInMinutes < commentThrottleMinutes) {
                            if (_logger.isDebugEnabled()) {
                                _logger.debug("Comment throttle enabled. Comment from IP address: " + remoteIPAddress + " in less than " + commentThrottleMinutes + " minutes");
                            }

                            return entries;
                        } else {
                            if (_logger.isDebugEnabled()) {
                                _logger.debug("Comment throttle enabled. Resetting date of last comment to current time");
                            }

                            _ipAddressCommentTimes.put(remoteIPAddress, currentTime);
                        }
                    } else {
                        Calendar calendar = Calendar.getInstance();
                        _ipAddressCommentTimes.put(remoteIPAddress, calendar);
                    }
                }

                author = author.trim();
                author = BlojsomUtils.escapeStringSimple(author);
                author = BlojsomUtils.stripLineTerminators(author, " ");

                commentText = commentText.trim();

                // Check if autoformatting of comment text should be done
                boolean autoformatComments = Boolean.valueOf(blog.getProperty(COMMENT_AUTOFORMAT_IP)).booleanValue();
                if (autoformatComments) {
                    commentText = BlojsomUtils.replace(commentText, "\n", "<br />");
                }

                if (authorEmail != null) {
                    authorEmail = authorEmail.trim();
                    authorEmail = BlojsomUtils.escapeStringSimple(authorEmail);
                    authorEmail = BlojsomUtils.stripLineTerminators(authorEmail, " ");
                } else {
                    authorEmail = "";
                }

                if (authorURL != null) {
                    authorURL = authorURL.trim();
                    authorURL = BlojsomUtils.escapeStringSimple(authorURL);
                    authorURL = BlojsomUtils.stripLineTerminators(authorURL, " ");
                } else {
                    authorURL = "";
                }

                if (!BlojsomUtils.checkNullOrBlank(authorURL) && !authorURL.toLowerCase().startsWith("http://")) {
                    authorURL = "http://" + authorURL;
                }

                Entry entryForComment = _fetcher.newEntry();
                try {
                    String blogEntryId = BlojsomUtils.getRequestValue("entry_id", httpServletRequest);
                    Integer entryId;
                    try {
                        entryId = Integer.valueOf(blogEntryId);
                    } catch (NumberFormatException e) {
                        if (_logger.isErrorEnabled()) {
                            _logger.error(e);
                        }

                        return entries;
                    }

                    entryForComment.setId(entryId);

                    _fetcher.loadEntry(blog, entryForComment);
                    if (_logger.isDebugEnabled()) {
                        _logger.debug("Loaded entry for comment: " + entryId.toString());
                    }

                    if (entryForComment.allowsComments().booleanValue()) {
                        // Check for a comment where the number of days between comment auto-expiration has passed
                        String commentDaysExpiration = blog.getProperty(COMMENT_DAYS_EXPIRATION_IP);
                        if (!BlojsomUtils.checkNullOrBlank(commentDaysExpiration)) {
                            try {
                                int daysExpiration = Integer.parseInt(commentDaysExpiration);
                                int daysBetweenDates = BlojsomUtils.daysBetweenDates(entryForComment.getDate(), new Date());
                                if ((daysExpiration > 0) && (daysBetweenDates >= daysExpiration)) {
                                    if (_logger.isDebugEnabled()) {
                                        _logger.debug("Comment period for this entry has expired. Expiration period set at " + daysExpiration + " days. Difference in days: " + daysBetweenDates);
                                    }

                                    return entries;
                                }
                            } catch (NumberFormatException e) {
                                if (_logger.isErrorEnabled()) {
                                    _logger.error("Error in parameter " + COMMENT_DAYS_EXPIRATION_IP + ": " + commentDaysExpiration);
                                }
                            }
                        }
                    } else {
                        if (_logger.isDebugEnabled()) {
                            _logger.debug("Comments have been disabled for blog entry: " + entryForComment.getId());
                        }

                        return entries;
                    }
                } catch (FetcherException e) {
                    if (_logger.isErrorEnabled()) {
                        _logger.error(e);
                    }

                    return entries;
                }

                Map commentMetaData = new HashMap();

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

                CommentResponseSubmissionEvent commentResponseSubmissionEvent = new CommentResponseSubmissionEvent(this, new Date(), blog, httpServletRequest, httpServletResponse, author, authorEmail, authorURL, commentText, entryForComment, commentMetaData);
                _eventBroadcaster.processEvent(commentResponseSubmissionEvent);
                author = commentResponseSubmissionEvent.getSubmitter();
                authorEmail = commentResponseSubmissionEvent.getSubmitterItem1();
                authorURL = commentResponseSubmissionEvent.getSubmitterItem2();
                commentText = commentResponseSubmissionEvent.getContent();

                // Check to see if the comment should be destroyed (not saved) automatically
                if (!commentMetaData.containsKey(BLOJSOM_PLUGIN_COMMENT_METADATA_DESTROY)) {
                    Comment comment = addBlogComment(author, authorEmail, authorURL, commentText, _blogCommentsEnabled.booleanValue(), commentMetaData, blog, entries[0], httpServletRequest);

                    // For persisting the Last-Modified time
                    context.put(BlojsomConstants.BLOJSOM_LAST_MODIFIED, new Long(new Date().getTime()));

                    if (comment != null) {
                        try {
                            _fetcher.loadEntry(blog, entries[0]);
                            _fetcher.loadEntry(blog, entryForComment);
                            _eventBroadcaster.broadcastEvent(new CommentAddedEvent(this, new Date(), comment, blog));
                        } catch (FetcherException e) {
                            if (_logger.isErrorEnabled()) {
                                _logger.error(e);
                            }
                        }
                    }
                } else {
                    if (_logger.isDebugEnabled()) {
                        _logger.debug("Comment meta-data contained destroy key. Comment was not saved");
                    }
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

                String redirectTo = BlojsomUtils.getRequestValue(BlojsomConstants.REDIRECT_TO_PARAM, httpServletRequest);
                if (!BlojsomUtils.checkNullOrBlank(redirectTo)) {
                    try {
                        httpServletResponse.sendRedirect(redirectTo);
                    } catch (IOException e) {                        
                    }
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
    private Comment addBlogComment(String author, String authorEmail, String authorURL, String userComment, boolean blogCommentsEnabled, Map commentMetaData, Blog blog, Entry entry, HttpServletRequest httpServletRequest) {
        Comment comment = null;

        if (blogCommentsEnabled) {
            try {
                comment = _fetcher.newComment();
                comment.setBlogEntryId(entry.getId());
                comment.setEntry(entry);
                comment.setAuthor(author);
                comment.setAuthorEmail(authorEmail);
                comment.setAuthorURL(authorURL);
                comment.setComment(userComment);
                comment.setCommentDate(new Date());
                comment.setBlogId(blog.getBlogId());
                comment.setIp(httpServletRequest.getRemoteAddr());
                if (commentMetaData.containsKey(CommentModerationPlugin.BLOJSOM_COMMENT_MODERATION_PLUGIN_APPROVED) &&
                        "true".equals(commentMetaData.get(CommentModerationPlugin.BLOJSOM_COMMENT_MODERATION_PLUGIN_APPROVED))) {
                    comment.setStatus(ResponseConstants.APPROVED_STATUS);
                } else {
                    comment.setStatus(ResponseConstants.NEW_STATUS);
                }
                comment.setMetaData(commentMetaData);

                String commentParentID = BlojsomUtils.getRequestValue(COMMENT_PARENT_ID, httpServletRequest);
                if (!BlojsomUtils.checkNullOrBlank(commentParentID)) {
                    try {
                        comment.setParentId(Integer.valueOf(commentParentID));
                    } catch (NumberFormatException e) {
                    }
                }

                _fetcher.saveComment(blog, comment);
            } catch (FetcherException e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }

                comment = null;
            }
        }

        return comment;
    }

    /**
     * Perform any cleanup for the plugin. Called after {@link #process}.
     *
     * @throws PluginException If there is an error performing cleanup for this plugin
     */
    public void cleanup() throws PluginException {
    }

    /**
     * Called when BlojsomServlet is taken out of service
     *
     * @throws PluginException If there is an error in finalizing this plugin
     */
    public void destroy() throws PluginException {
    }

    /**
     * Setup the comment e-mail
     *
     * @param blog  {@link Blog} information
     * @param email Email message
     * @throws EmailException If there is an error preparing the e-mail message
     */
    protected void setupEmail(Blog blog, Entry entry, Email email) throws EmailException {
        email.setCharset(BlojsomConstants.UTF8);

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

        String author = entry.getAuthor();
        if (BlojsomUtils.checkNullOrBlank(author)) {
            author = blog.getBlogOwner();
        }

        String authorEmail = blog.getBlogOwnerEmail();

        if (author != null) {
            try {
                User user = _fetcher.loadUser(blog, author);

                if (user == null) {
                    authorEmail = blog.getBlogOwnerEmail();
                } else {
                    authorEmail = user.getUserEmail();
                    if (BlojsomUtils.checkNullOrBlank(authorEmail)) {
                        authorEmail = blog.getBlogOwnerEmail();
                    }
                }
            } catch (FetcherException e) {
            }
        }

        email.addTo(authorEmail, author);
        email.setSentDate(new Date());
    }

    /**
     * Handle an event broadcast from another component
     *
     * @param event {@link org.blojsom.event.Event} to be handled
     */
    public void handleEvent(Event event) {
        if (event instanceof CommentAddedEvent) {
            HtmlEmail email = new HtmlEmail();
            CommentAddedEvent commentAddedEvent = (CommentAddedEvent) event;

            if (commentAddedEvent.getBlog().getBlogEmailEnabled().booleanValue() && _mailServer != null) {
                try {
                    setupEmail(commentAddedEvent.getBlog(), commentAddedEvent.getEntry(), email);

                    Map emailTemplateContext = new HashMap();
                    emailTemplateContext.put(BlojsomConstants.BLOJSOM_BLOG, commentAddedEvent.getBlog());
                    emailTemplateContext.put(BLOJSOM_COMMENT_PLUGIN_BLOG_COMMENT, commentAddedEvent.getComment());
                    emailTemplateContext.put(BLOJSOM_COMMENT_PLUGIN_BLOG_ENTRY, commentAddedEvent.getEntry());

                    String htmlText = mergeTemplate(COMMENT_PLUGIN_EMAIL_TEMPLATE_HTML, commentAddedEvent.getBlog(), emailTemplateContext);
                    String plainText = mergeTemplate(COMMENT_PLUGIN_EMAIL_TEMPLATE_TEXT, commentAddedEvent.getBlog(), emailTemplateContext);

                    email.setHtmlMsg(htmlText);
                    email.setTextMsg(plainText);

                    String emailPrefix = (String) commentAddedEvent.getBlog().getProperties().get(COMMENT_PREFIX_IP);
                    if (BlojsomUtils.checkNullOrBlank(emailPrefix)) {
                        emailPrefix = DEFAULT_COMMENT_PREFIX;
                    }

                    email = (HtmlEmail) email.setSubject(emailPrefix + commentAddedEvent.getEntry().getTitle());

                    email.send();
                } catch (EmailException e) {
                    if (_logger.isErrorEnabled()) {
                        _logger.error(e);
                    }
                }
            } else {
                if (_logger.isErrorEnabled()) {
                    _logger.error("Missing SMTP servername servlet initialization parameter: " + EmailConstants.SMTPSERVER_IP);
                }
            }
        }
    }

    /**
     * Process an event from another component
     *
     * @param event {@link org.blojsom.event.Event} to be handled
     */
    public void processEvent(Event event) {
    }
}
