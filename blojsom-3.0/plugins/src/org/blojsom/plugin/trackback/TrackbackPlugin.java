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
package org.blojsom.plugin.trackback;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.blojsom.blog.Blog;
import org.blojsom.blog.Entry;
import org.blojsom.blog.Trackback;
import org.blojsom.event.Event;
import org.blojsom.event.EventBroadcaster;
import org.blojsom.event.Listener;
import org.blojsom.fetcher.Fetcher;
import org.blojsom.fetcher.FetcherException;
import org.blojsom.plugin.PluginException;
import org.blojsom.plugin.common.ResponseConstants;
import org.blojsom.plugin.email.EmailConstants;
import org.blojsom.plugin.trackback.event.TrackbackAddedEvent;
import org.blojsom.plugin.trackback.event.TrackbackResponseSubmissionEvent;
import org.blojsom.plugin.velocity.StandaloneVelocityPlugin;
import org.blojsom.util.BlojsomConstants;
import org.blojsom.util.BlojsomMetaDataConstants;
import org.blojsom.util.BlojsomUtils;

import javax.mail.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * TrackbackPlugin
 *
 * @author David Czarnecki
 * @since blojsom 3.0
 * @version $Id: TrackbackPlugin.java,v 1.1 2006-03-20 21:31:01 czarneckid Exp $
 */
public class TrackbackPlugin extends StandaloneVelocityPlugin implements BlojsomMetaDataConstants, Listener, EmailConstants {

    private Log _logger = LogFactory.getLog(TrackbackPlugin.class);

    /**
     * Template for comment e-mails
     */
    public static final String TRACKBACK_PLUGIN_EMAIL_TEMPLATE_TEXT = "org/blojsom/plugin/trackback/trackback-plugin-email-template-text.vm";
    public static final String TRACKBACK_PLUGIN_EMAIL_TEMPLATE_HTML = "org/blojsom/plugin/trackback/trackback-plugin-email-template-html.vm";

    /**
     * Default prefix for trackback e-mail notification
     */
    public static final String DEFAULT_TRACKBACK_PREFIX = "[blojsom] Trackback on: ";

    /**
     * Initialization parameter for e-mail prefix
     */
    public static final String TRACKBACK_PREFIX_IP = "plugin-trackback-email-prefix";

    /**
     * Initialization parameter for the throttling of trackbacks from IP addresses
     */
    public static final String TRACKBACK_THROTTLE_MINUTES_IP = "plugin-trackback-throttle";

    /**
     * Initialization parameter for disabling trackbacks on entries after a certain number of days
     */
    public static final String TRACKBACK_DAYS_EXPIRATION_IP = "plugin-trackback-days-expiration";

    /**
     * Default throttle value for trackbacks from a particular IP address
     */
    private static final int TRACKBACK_THROTTLE_DEFAULT_MINUTES = 5;

    /**
     * Request parameter to indicate a trackback "tb"
     */
    public static final String TRACKBACK_PARAM = "tb";

    /**
     * Request parameter for the trackback "title"
     */
    public static final String TRACKBACK_TITLE_PARAM = "title";

    /**
     * Request parameter for the trackback "excerpt"
     */
    public static final String TRACKBACK_EXCERPT_PARAM = "excerpt";

    /**
     * Request parameter for the trackback "url"
     */
    public static final String TRACKBACK_URL_PARAM = "url";

    /**
     * Request parameter for the trackback "blog_name"
     */
    public static final String TRACKBACK_BLOG_NAME_PARAM = "blog_name";

    /**
     * Key under which the indicator this plugin is "live" will be placed
     * (example: on the request for the JSPDispatcher)
     */
    public static final String BLOJSOM_TRACKBACK_PLUGIN_ENABLED = "BLOJSOM_TRACKBACK_PLUGIN_ENABLED";

    /**
     * Key under which the trackback return code will be placed
     * (example: on the request for the JSPDispatcher)
     */
    public static final String BLOJSOM_TRACKBACK_RETURN_CODE = "BLOJSOM_TRACKBACK_RETURN_CODE";

    /**
     * Key under which the trackback error message will be placed
     * (example: on the request for the JSPDispatcher)
     */
    public static final String BLOJSOM_TRACKBACK_MESSAGE = "BLOJSOM_TRACKBACK_MESSAGE";

    /**
     * IP address meta-data
     */
    public static final String BLOJSOM_TRACKBACK_PLUGIN_METADATA_IP = "BLOJSOM_TRACKBACK_PLUGIN_METADATA_IP";

    /**
     * Trackback success page
     */
    private static final String TRACKBACK_SUCCESS_PAGE = "/trackback-success";

    /**
     * Trackback failure page
     */
    private static final String TRACKBACK_FAILURE_PAGE = "/trackback-failure";

    /**
     * Key under which the blog entry will be placed for merging the trackback e-mail
     */
    public static final String BLOJSOM_TRACKBACK_PLUGIN_BLOG_ENTRY = "BLOJSOM_TRACKBACK_PLUGIN_BLOG_ENTRY";

    /**
     * Key under which the blog comment will be placed for merging the trackback e-mail
     */
    public static final String BLOJSOM_TRACKBACK_PLUGIN_TRACKBACK = "BLOJSOM_TRACKBACK_PLUGIN_TRACKBACK";

    public static final String BLOJSOM_PLUGIN_TRACKBACK_METADATA = "BLOJSOM_PLUGIN_TRACKBACK_METADATA";

    public static final String BLOJSOM_PLUGIN_TRACKBACK_METADATA_DESTROY = "BLOJSOM_PLUGIN_TRACKBACK_METADATA_DESTROY";

    private Map _ipAddressTrackbackTimes;
    private String _mailServer;
    private String _mailServerUsername;
    private String _mailServerPassword;
    private Session _session;
    private Fetcher _fetcher;
    private EventBroadcaster _eventBroadcaster;

    /**
     * Default constructor
     */
    public TrackbackPlugin() {
    }

    /**
     * Set the {@link oEventBroadcaster} event broadcaster
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

        _mailServer = _servletConfig.getInitParameter(SMTPSERVER_IP);

        if (_mailServer != null) {
            if (_mailServer.startsWith("java:comp/env")) {
                try {
                    Context context = new InitialContext();
                    _session = (Session) context.lookup(_mailServer);
                } catch (NamingException e) {
                    _logger.error(e);
                    throw new PluginException(e);
                }
            } else {
                _mailServerUsername = _servletConfig.getInitParameter(SMTPSERVER_USERNAME_IP);
                _mailServerPassword = _servletConfig.getInitParameter(SMTPSERVER_PASSWORD_IP);
            }
        } else {
            throw new PluginException("Missing SMTP servername servlet initialization parameter: " + SMTPSERVER_IP);
        }

        _ipAddressTrackbackTimes = new WeakHashMap();
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
        context.put(BLOJSOM_TRACKBACK_PLUGIN_ENABLED, blog.getBlogTrackbacksEnabled());
        if (!blog.getBlogTrackbacksEnabled().booleanValue()) {
            _logger.debug("Trackbacks not enabled for blog: " + blog.getBlogId());

            return entries;
        }

        Boolean _blogTrackbacksEnabled;

        _blogTrackbacksEnabled = blog.getBlogTrackbacksEnabled();

        if (entries.length == 0) {
            return entries;
        }

        if (!_blogTrackbacksEnabled.booleanValue()) {
            return entries;
        }

        String url = httpServletRequest.getParameter(TRACKBACK_URL_PARAM);
        String permalink = httpServletRequest.getParameter(BlojsomConstants.PERMALINK_PARAM);
        String title = httpServletRequest.getParameter(TRACKBACK_TITLE_PARAM);
        String excerpt = httpServletRequest.getParameter(TRACKBACK_EXCERPT_PARAM);
        String blogName = httpServletRequest.getParameter(TRACKBACK_BLOG_NAME_PARAM);
        String tb = httpServletRequest.getParameter(TRACKBACK_PARAM);
        String remoteIPAddress = httpServletRequest.getRemoteAddr();

        if ((permalink != null) && (!"".equals(permalink)) && (tb != null) && ("y".equalsIgnoreCase(tb))) {
            if ((url == null) || ("".equals(url.trim()))) {
                context.put(BLOJSOM_TRACKBACK_RETURN_CODE, new Integer(1));
                context.put(BLOJSOM_TRACKBACK_MESSAGE, "No url parameter for trackback. url must be specified.");
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, TRACKBACK_FAILURE_PAGE);

                return entries;
            }

            // Check for trackback throttling
            String trackbackThrottleValue = blog.getProperty(TRACKBACK_THROTTLE_MINUTES_IP);
            if (!BlojsomUtils.checkNullOrBlank(trackbackThrottleValue)) {
                int trackbackThrottleMinutes;

                try {
                    trackbackThrottleMinutes = Integer.parseInt(trackbackThrottleValue);
                } catch (NumberFormatException e) {
                    trackbackThrottleMinutes = TRACKBACK_THROTTLE_DEFAULT_MINUTES;
                }
                _logger.debug("Trackback throttling enabled at: " + trackbackThrottleMinutes + " minutes");

                if (_ipAddressTrackbackTimes.containsKey(remoteIPAddress)) {
                    Calendar currentTime = Calendar.getInstance();
                    Calendar timeOfLastTrackback = (Calendar) _ipAddressTrackbackTimes.get(remoteIPAddress);
                    long timeDifference = currentTime.getTimeInMillis() - timeOfLastTrackback.getTimeInMillis();

                    long differenceInMinutes = timeDifference / (60 * 1000);
                    if (differenceInMinutes < trackbackThrottleMinutes) {
                        _logger.debug("Trackback throttle enabled. Comment from IP address: " + remoteIPAddress + " in less than " + trackbackThrottleMinutes + " minutes");

                        context.put(BLOJSOM_TRACKBACK_RETURN_CODE, new Integer(1));
                        context.put(BLOJSOM_TRACKBACK_MESSAGE, "Trackback throttling enabled.");
                        httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, TRACKBACK_FAILURE_PAGE);

                        return entries;
                    } else {
                        _logger.debug("Trackback throttle enabled. Resetting date of last comment to current time");
                        _ipAddressTrackbackTimes.put(remoteIPAddress, currentTime);
                    }
                } else {
                    Calendar calendar = Calendar.getInstance();
                    _ipAddressTrackbackTimes.put(remoteIPAddress, calendar);
                }
            }

            url = url.trim();
            if (!url.toLowerCase().startsWith("http://")) {
                url = "http://" + url;
            }

            if (BlojsomUtils.checkNullOrBlank(title)) {
                title = url;
            } else {
                title = title.trim();
                title = BlojsomUtils.escapeStringSimple(title);
                title = BlojsomUtils.stripLineTerminators(title, " ");
            }

            if (excerpt == null) {
                excerpt = "";
            } else {
                if (excerpt.length() >= 255) {
                    excerpt = excerpt.substring(0, 252);
                    excerpt += "...";
                }

                excerpt = BlojsomUtils.stripLineTerminators(excerpt, " ");
            }

            if (blogName == null) {
                blogName = "";
            } else {
                blogName = blogName.trim();
                blogName = BlojsomUtils.escapeStringSimple(blogName);
                blogName = BlojsomUtils.stripLineTerminators(blogName, " ");
            }

            Entry entryForTrackback = _fetcher.newEntry();
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

                entryForTrackback.setId(entryId);

                _fetcher.loadEntry(blog, entryForTrackback);
                if (_logger.isDebugEnabled()) {
                    _logger.debug("Loaded entry for trackback: " + entryId.toString());
                }

                // Check for a trackback where the number of days between trackback auto-expiration has passed
                String trackbackDaysExpiration = blog.getProperty(TRACKBACK_DAYS_EXPIRATION_IP);
                if (!BlojsomUtils.checkNullOrBlank(trackbackDaysExpiration)) {
                    try {
                        int daysExpiration = Integer.parseInt(trackbackDaysExpiration);
                        int daysBetweenDates = BlojsomUtils.daysBetweenDates(entryForTrackback.getDate(), new Date());
                        if ((daysExpiration > 0) && (daysBetweenDates >= daysExpiration)) {
                            _logger.debug("Trackback period for this entry has expired. Expiration period set at " + daysExpiration + " days. Difference in days: " + daysBetweenDates);

                            return entries;
                        }
                    } catch (NumberFormatException e) {
                        _logger.error("Error in parameter " + TRACKBACK_DAYS_EXPIRATION_IP + ": " + trackbackDaysExpiration);
                    }
                }
            } catch (FetcherException e) {
                _logger.error(e);
            }

            Map trackbackMetaData = new HashMap();

            // Check to see if a previous plugin populated meta-data for the comment
            if (context.containsKey(BLOJSOM_PLUGIN_TRACKBACK_METADATA)) {
                Map metaData = (Map) context.get(BLOJSOM_PLUGIN_TRACKBACK_METADATA);

                Iterator metaDataKeys = metaData.keySet().iterator();
                Object key;
                Object value;
                while (metaDataKeys.hasNext()) {
                    key = metaDataKeys.next();
                    value = metaData.get(key);
                    trackbackMetaData.put(key, value);
                }
            }

            Trackback trackback = _fetcher.newTrackback();
            trackback.setBlogEntryId(entryForTrackback.getId());
            trackback.setEntry(entryForTrackback);

            Integer code = new Integer(1);

            _eventBroadcaster.processEvent(new TrackbackResponseSubmissionEvent(this, new Date(), blog, httpServletRequest, httpServletResponse, blogName, title, url, excerpt, entryForTrackback, trackbackMetaData));

            // Check to see if the trackback should be destroyed (not saved) automatically
            if (!trackbackMetaData.containsKey(BLOJSOM_PLUGIN_TRACKBACK_METADATA_DESTROY)) {
                code = addTrackback(title, excerpt, url, blogName, trackbackMetaData, trackback, blog, context, httpServletRequest);

                // For persisting the Last-Modified time
                context.put(BlojsomConstants.BLOJSOM_LAST_MODIFIED, new Long(new Date().getTime()));
            } else {
                _logger.info("Trackback meta-data contained destroy key. Trackback was not saved");
            }

            context.put(BLOJSOM_TRACKBACK_RETURN_CODE, code);
            if (code.intValue() == 0) {
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, TRACKBACK_SUCCESS_PAGE);

                try {
                    _fetcher.loadEntry(blog, entries[0]);
                    _fetcher.loadEntry(blog, entryForTrackback);
                    _eventBroadcaster.broadcastEvent(new TrackbackAddedEvent(this, new Date(), trackback, blog));
                } catch (FetcherException e) {
                    if (_logger.isErrorEnabled()) {
                        _logger.error(e);
                    }
                }
            } else {
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, TRACKBACK_FAILURE_PAGE);
            }
        }

        return entries;
    }

    /**
     * Add a trackback
     *
     * @param title Title
     * @param excerpt Excerpt
     * @param url URL
     * @param blogName Blog name
     * @param trackbackMetaData Trackback meta-data
     * @param trackback {@link Trackback}
     * @param blog {@link Blog}
     * @param context Context
     * @param httpServletRequest {@link HttpServletRequest}
     * @return <code>0</code> if adding the trackback was successful, <code>1</code> if there was an error
     */
    private Integer addTrackback(String title, String excerpt, String url, String blogName, Map trackbackMetaData, Trackback trackback, Blog blog, Map context, HttpServletRequest httpServletRequest) {
        try {
            excerpt = BlojsomUtils.escapeMetaAndLink(excerpt);
            trackback.setTitle(title);
            trackback.setExcerpt(excerpt);
            trackback.setUrl(url);
            trackback.setBlogName(blogName);
            trackback.setTrackbackDate(new Date());
            trackback.setBlogId(blog.getBlogId());
            trackback.setIp(httpServletRequest.getRemoteAddr());
            trackback.setMetaData(trackbackMetaData);
            trackback.setStatus(ResponseConstants.NEW_STATUS);
            if (trackbackMetaData.containsKey(TrackbackModerationPlugin.BLOJSOM_TRACKBACK_MODERATION_PLUGIN_APPROVED)) {
                trackback.setStatus(ResponseConstants.APPROVED_STATUS);
            }

            _fetcher.saveTrackback(blog, trackback);

            return new Integer(0);
        } catch (FetcherException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }

            context.put(BLOJSOM_TRACKBACK_MESSAGE, e.getMessage());
            return new Integer(1);
        }
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

        email.setFrom(blog.getBlogOwnerEmail(), "Blojsom Trackback");

        String author = entry.getAuthor();
        if (BlojsomUtils.checkNullOrBlank(author)) {
            author = blog.getBlogOwner();
        }

        String authorEmail = blog.getBlogOwnerEmail();

        if (author != null) {
            // XXX
            authorEmail = "";
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
     * @param event {@link org.blojsom.event.Event} to be handled
     */
    public void handleEvent(Event event) {
        if (event instanceof TrackbackAddedEvent) {
            HtmlEmail email = new HtmlEmail();

            TrackbackAddedEvent trackbackAddedEvent = (TrackbackAddedEvent) event;

            if (trackbackAddedEvent.getBlog().getBlogEmailEnabled().booleanValue()) {
                try {
                    setupEmail(trackbackAddedEvent.getBlog(), trackbackAddedEvent.getEntry(), email);

                    Map emailTemplateContext = new HashMap();
                    emailTemplateContext.put(BlojsomConstants.BLOJSOM_BLOG, trackbackAddedEvent.getBlog());
                    emailTemplateContext.put(BLOJSOM_TRACKBACK_PLUGIN_TRACKBACK, trackbackAddedEvent.getTrackback());
                    emailTemplateContext.put(BLOJSOM_TRACKBACK_PLUGIN_BLOG_ENTRY, trackbackAddedEvent.getEntry());

                    String htmlText = mergeTemplate(TRACKBACK_PLUGIN_EMAIL_TEMPLATE_HTML, trackbackAddedEvent.getBlog(), emailTemplateContext);
                    String plainText = mergeTemplate(TRACKBACK_PLUGIN_EMAIL_TEMPLATE_TEXT, trackbackAddedEvent.getBlog(), emailTemplateContext);

                    email = email.setHtmlMsg(htmlText);
                    email = email.setTextMsg(plainText);

                    String emailPrefix = (String) trackbackAddedEvent.getBlog().getProperties().get(TRACKBACK_PREFIX_IP);
                    if (BlojsomUtils.checkNullOrBlank(emailPrefix)) {
                        emailPrefix = DEFAULT_TRACKBACK_PREFIX;
                    }

                    email = (HtmlEmail) email.setSubject(emailPrefix + trackbackAddedEvent.getEntry().getTitle());

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
     * @param event {@link org.blojsom.event.Event} to be handled
     */
    public void processEvent(Event event) {
    }
}
