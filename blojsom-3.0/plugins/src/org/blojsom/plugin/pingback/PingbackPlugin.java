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
package org.blojsom.plugin.pingback;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.xmlrpc.AsyncCallback;
import org.apache.xmlrpc.XmlRpcClient;
import org.blojsom.blog.Blog;
import org.blojsom.blog.Entry;
import org.blojsom.blog.User;
import org.blojsom.event.Event;
import org.blojsom.event.EventBroadcaster;
import org.blojsom.event.Listener;
import org.blojsom.plugin.Plugin;
import org.blojsom.plugin.PluginException;
import org.blojsom.plugin.admin.event.EntryAddedEvent;
import org.blojsom.plugin.admin.event.EntryEvent;
import org.blojsom.plugin.admin.event.EntryUpdatedEvent;
import org.blojsom.plugin.email.EmailConstants;
import org.blojsom.plugin.pingback.event.PingbackAddedEvent;
import org.blojsom.plugin.velocity.StandaloneVelocityPlugin;
import org.blojsom.util.BlojsomConstants;
import org.blojsom.util.BlojsomUtils;
import org.blojsom.fetcher.FetcherException;
import org.blojsom.fetcher.Fetcher;

import javax.mail.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Pingback plugin implements a pingback client to send pingbacks to any URLs in a blog entry according to the
 * <a href="http://www.hixie.ch/specs/pingback/pingback">Pingback 1.0</a> specification.
 *
 * @author David Czarnecki
 * @version $Id: PingbackPlugin.java,v 1.3 2006-03-23 04:27:26 czarneckid Exp $
 * @since blojsom 3.0
 */
public class PingbackPlugin extends StandaloneVelocityPlugin implements Plugin, Listener {

    private static Log _logger = LogFactory.getLog(PingbackPlugin.class);

    private static final String PINGBACK_PLUGIN_EMAIL_TEMPLATE_HTML = "org/blojsom/plugin/pingback/pingback-plugin-email-template-html.vm";
    private static final String PINGBACK_PLUGIN_EMAIL_TEMPLATE_TEXT = "org/blojsom/plugin/pingback/pingback-plugin-email-template-text.vm";

    private static final String DEFAULT_PINGBACK_PREFIX = "[blojsom] Pingback on: ";
    private static final String PINGBACK_PREFIX_IP = "plugin-pingback-email-prefix";
    private static final String BLOJSOM_PINGBACK_PLUGIN_BLOG_ENTRY = "BLOJSOM_PINGBACK_PLUGIN_BLOG_ENTRY";
    private static final String BLOJSOM_PINGBACK_PLUGIN_PINGBACK = "BLOJSOM_PINGBACK_PLUGIN_PINGBACK";

    private static final String PINGBACK_METHOD = "pingback.ping";
    private static final String X_PINGBACK_HEADER = "X-Pingback";
    private static final String PINGBACK_LINK_REGEX = "<link rel=\"pingback\" href=\"([^\"]+)\" ?/?>";
    private static final String HREF_REGEX = "href\\s*=\\s*\"(.*?)\"";

    /**
     * IP address meta-data
     */
    public static final String BLOJSOM_PINGBACK_PLUGIN_METADATA_IP = "BLOJSOM_PINGBACK_PLUGIN_METADATA_IP";

    public static final String BLOJSOM_PLUGIN_PINGBACK_METADATA_DESTROY = "BLOJSOM_PLUGIN_PINGBACK_METADATA_DESTROY";
    public static final String PINGBACK_PLUGIN_METADATA_SEND_PINGBACKS = "send-pingbacks";

    private String _mailServer;
    private String _mailServerUsername;
    private String _mailServerPassword;
    private Session _session;
    private EventBroadcaster _eventBroadcaster;
    private Fetcher _fetcher;

    private PingbackPluginAsyncCallback _callbackHandler;

    /**
     * Create a new instance of the Pingback plugin
     */
    public PingbackPlugin() {
    }

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

        _callbackHandler = new PingbackPluginAsyncCallback();

        _mailServer = _servletConfig.getInitParameter(EmailConstants.SMTPSERVER_IP);

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
                _mailServerUsername = _servletConfig.getInitParameter(EmailConstants.SMTPSERVER_USERNAME_IP);
                _mailServerPassword = _servletConfig.getInitParameter(EmailConstants.SMTPSERVER_PASSWORD_IP);
            }
        } else {
            throw new PluginException("Missing SMTP servername servlet initialization parameter: " + EmailConstants.SMTPSERVER_IP);
        }

        _eventBroadcaster.addListener(this);

        _logger.debug("Initialized pingback plugin");
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
        return entries;
    }

    /**
     * Perform any cleanup for the plugin. Called after {@link #process}.
     *
     * @throws org.blojsom.plugin.PluginException
     *          If there is an error performing cleanup for this plugin
     */
    public void cleanup() throws PluginException {
    }

    /**
     * Called when BlojsomServlet is taken out of service
     *
     * @throws org.blojsom.plugin.PluginException
     *          If there is an error in finalizing this plugin
     */
    public void destroy() throws PluginException {
    }

    /**
     * Setup the pingback e-mail
     *
     * @param blog  {@link org.blojsom.blog.Blog} information
     * @param email Email message
     * @throws org.apache.commons.mail.EmailException
     *          If there is an error preparing the e-mail message
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

        email.setFrom(blog.getBlogOwnerEmail(), "Blojsom Pingback");

        String author = entry.getAuthor();
        if (BlojsomUtils.checkNullOrBlank(author)) {
            author = blog.getBlogOwner();
        }

        String authorEmail = blog.getBlogOwnerEmail();

        if (author != null) {
            try {
                User user = _fetcher.loadUser(blog, author);

                authorEmail = user.getUserEmail();
                if (BlojsomUtils.checkNullOrBlank(authorEmail)) {
                    authorEmail = blog.getBlogOwnerEmail();
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
        if (event instanceof EntryAddedEvent || event instanceof EntryUpdatedEvent) {
            EntryEvent entryEvent = (EntryEvent) event;

            String text = entryEvent.getEntry().getDescription();
            if (!BlojsomUtils.checkNullOrBlank(text) && BlojsomUtils.checkMapForKey(entryEvent.getEntry().getMetaData(), PINGBACK_PLUGIN_METADATA_SEND_PINGBACKS))
            {
                String pingbackURL;
                StringBuffer sourceURI = new StringBuffer().append(entryEvent.getBlog().getBlogURL()).append(entryEvent.getEntry().getBlogCategory().getName()).append(entryEvent.getEntry().getPostSlug());
                String targetURI;

                Pattern hrefPattern = Pattern.compile(HREF_REGEX, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.UNICODE_CASE | Pattern.DOTALL);
                Matcher hrefMatcher = hrefPattern.matcher(text);
                if (_logger.isDebugEnabled()) {
                    _logger.debug("Checking for href's in entry: " + entryEvent.getEntry().getId());
                }
                while (hrefMatcher.find()) {
                    targetURI = hrefMatcher.group(1);
                    if (_logger.isDebugEnabled()) {
                        _logger.debug("Found potential targetURI: " + targetURI);
                    }

                    // Perform an HTTP request and first see if the X-Pingback header is available
                    try {
                        HttpURLConnection urlConnection = (HttpURLConnection) new URL(targetURI).openConnection();
                        urlConnection.setRequestMethod("GET");
                        urlConnection.connect();
                        pingbackURL = urlConnection.getHeaderField(X_PINGBACK_HEADER);

                        // If the header is not available, look for the link in the URL content
                        if (pingbackURL == null) {
                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), BlojsomConstants.UTF8));
                            StringBuffer content = new StringBuffer();
                            String input;
                            while ((input = bufferedReader.readLine()) != null) {
                                content.append(input).append(BlojsomConstants.LINE_SEPARATOR);
                            }
                            bufferedReader.close();

                            Pattern pingbackLinkPattern = Pattern.compile(PINGBACK_LINK_REGEX, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.UNICODE_CASE | Pattern.DOTALL);
                            Matcher pingbackLinkMatcher = pingbackLinkPattern.matcher(content.toString());
                            if (pingbackLinkMatcher.find()) {
                                pingbackURL = pingbackLinkMatcher.group(1);
                            }
                        }

                        // Finally, send the pingback
                        if (pingbackURL != null && targetURI != null) {
                            Vector parameters = new Vector();
                            parameters.add(sourceURI.toString());
                            parameters.add(targetURI);
                            try {
                                if (_logger.isDebugEnabled()) {
                                    _logger.debug("Sending pingback to: " + pingbackURL + " sourceURI: " + sourceURI + " targetURI: " + targetURI);
                                }
                               
                                XmlRpcClient xmlRpcClient = new XmlRpcClient(pingbackURL);
                                xmlRpcClient.executeAsync(PINGBACK_METHOD, parameters, _callbackHandler);
                            } catch (MalformedURLException e) {
                                if (_logger.isErrorEnabled()) {
                                    _logger.error(e);
                                }
                            }
                        }
                    } catch (IOException e) {
                        if (_logger.isErrorEnabled()) {
                            _logger.error(e);
                        }
                    }
                }
            } else {
                if (_logger.isDebugEnabled()) {
                    _logger.debug("No text in blog entry or " + PINGBACK_PLUGIN_METADATA_SEND_PINGBACKS + " not enabled.");
                }
            }
        } else if (event instanceof PingbackAddedEvent) {
            HtmlEmail email = new HtmlEmail();
            PingbackAddedEvent pingbackAddedEvent = (PingbackAddedEvent) event;

            if (pingbackAddedEvent.getBlog().getBlogEmailEnabled().booleanValue()) {
                try {
                    setupEmail(pingbackAddedEvent.getBlog(), pingbackAddedEvent.getEntry(), email);

                    Map emailTemplateContext = new HashMap();
                    emailTemplateContext.put(BlojsomConstants.BLOJSOM_BLOG, pingbackAddedEvent.getBlog());
                    emailTemplateContext.put(BLOJSOM_PINGBACK_PLUGIN_PINGBACK, pingbackAddedEvent.getPingback());
                    emailTemplateContext.put(BLOJSOM_PINGBACK_PLUGIN_BLOG_ENTRY, pingbackAddedEvent.getEntry());

                    String htmlText = mergeTemplate(PINGBACK_PLUGIN_EMAIL_TEMPLATE_HTML, pingbackAddedEvent.getBlog(), emailTemplateContext);
                    String plainText = mergeTemplate(PINGBACK_PLUGIN_EMAIL_TEMPLATE_TEXT, pingbackAddedEvent.getBlog(), emailTemplateContext);

                    email.setHtmlMsg(htmlText);
                    email.setTextMsg(plainText);

                    String emailPrefix = (String) pingbackAddedEvent.getBlog().getProperties().get(PINGBACK_PREFIX_IP);
                    if (BlojsomUtils.checkNullOrBlank(emailPrefix)) {
                        emailPrefix = DEFAULT_PINGBACK_PREFIX;
                    }

                    email = (HtmlEmail) email.setSubject(emailPrefix + pingbackAddedEvent.getEntry().getTitle());

                    email.send();
                } catch (EmailException e) {
                    if (_logger.isErrorEnabled()) {
                        _logger.error(e);
                    }
                }
            }
        }
    }

    /**
     * Process an event from another component
     *
     * @param event {@link Event} to be handled
     */
    public void processEvent(Event event) {
    }

    /**
     * Asynchronous callback handler for the pingback ping
     */
    private class PingbackPluginAsyncCallback implements AsyncCallback {

        /**
         * Default constructor
         */
        public PingbackPluginAsyncCallback() {
        }

        /**
         * Call went ok, handle result.
         *
         * @param o   Return object
         * @param url URL
         * @param s   String
         */
        public void handleResult(Object o, URL url, String s) {
            if (_logger.isDebugEnabled()) {
                _logger.debug(o.toString());
            }
        }

        /**
         * Something went wrong, handle error.
         *
         * @param e   Exception containing error from XML-RPC call
         * @param url URL
         * @param s   String
         */
        public void handleError(Exception e, URL url, String s) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }
        }
    }
}