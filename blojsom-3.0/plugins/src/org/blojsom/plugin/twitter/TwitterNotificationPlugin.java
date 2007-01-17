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
package org.blojsom.plugin.twitter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.blojsom.blog.Blog;
import org.blojsom.blog.Entry;
import org.blojsom.blog.User;
import org.blojsom.event.Event;
import org.blojsom.event.EventBroadcaster;
import org.blojsom.event.Listener;
import org.blojsom.plugin.Plugin;
import org.blojsom.plugin.PluginException;
import org.blojsom.plugin.admin.event.EntryEvent;
import org.blojsom.plugin.admin.event.EntryAddedEvent;
import org.blojsom.plugin.admin.event.EntryUpdatedEvent;
import org.blojsom.util.BlojsomUtils;
import org.blojsom.fetcher.Fetcher;
import org.blojsom.fetcher.FetcherException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.text.MessageFormat;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Twitter notification plugin for the <a href="http://twitter.com/">Twitter</a> service
 *
 * @author David Czarnecki
 * @since blojsom 3.1
 * @version $Id: TwitterNotificationPlugin.java,v 1.5 2007-01-17 02:35:15 czarneckid Exp $
 */
public class TwitterNotificationPlugin implements Plugin, Listener {

    private Log _logger = LogFactory.getLog(TwitterNotificationPlugin.class);
    
    private static final String TWITTER_STATUS_UPDATE_URL = "http://twitter.com/statuses/update.xml";
    private static final String TWITTER_STATUS_PARAMETER = "status";
    private static final String TWITTER_DEFAULT_STATUS_UPDATE_TEXT = "Currently blogging {0}";
    
    private static final String TWITTER_SIGN_IN_IP = "plugin-twitter-sign-in";
    private static final String TWITTER_PASSWORD_IP = "plugin-twitter-password";
    private static final String TWITTER_UPDATE_ON_ENTRY_ADDED_IP = "plugin-twitter-update-on-entry-added";
    private static final String TWITTER_UPDATE_ON_ENTRY_UPDATED_IP = "plugin-twitter-update-on-entry-updated";
    private static final String TWITTER_STATUS_UPDATE_TEXT_IP = "plugin-twitter-update-text";

    private EventBroadcaster _eventBroadcaster;
    private Fetcher _fetcher;
    
    private String _twitterUpdateURL = TWITTER_STATUS_UPDATE_URL;

    /**
     * Create a new instance of the Twitter notification plugin
     */
    public TwitterNotificationPlugin() {
    }

    /**
     * Set the {@link EventBroadcaster}
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
     * Set the Twitter update URL
     *
     * @param updateURL Twitter update URL
     */
    public void setTwitterStatusUpdateURL(String updateURL) {
        _twitterUpdateURL = updateURL;
    }

    /**
     * Initialize this plugin. This method only called when the plugin is instantiated.
     *
     * @throws PluginException If there is an error initializing the plugin
     */
    public void init() throws PluginException {
        _eventBroadcaster.addListener(this);

        if (_logger.isDebugEnabled()) {
            _logger.debug("Initialized Twitter notification plugin");
        }
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
     * Handle an event broadcast from another component
     *
     * @param event {@link Event} to be handled
     */
    public void handleEvent(Event event) {
        if (!BlojsomUtils.checkNullOrBlank(_twitterUpdateURL)) {
            if ((event instanceof EntryAddedEvent) || (event instanceof EntryUpdatedEvent)) {
                EntryEvent entryEvent = (EntryEvent) event;

                Blog blog = entryEvent.getBlog();
                String author = entryEvent.getEntry().getAuthor();
                User user;

                try {
                    user = _fetcher.loadUser(blog, author);
                } catch (FetcherException e) {
                    if (_logger.isErrorEnabled()) {
                        _logger.error("Error loading User object to retrieve Twitter properties", e);
                    }

                    return;
                }

                if (!BlojsomUtils.checkNullOrBlank((String) user.getMetaData().get(TWITTER_SIGN_IN_IP)) &&
                        !BlojsomUtils.checkNullOrBlank((String) user.getMetaData().get(TWITTER_PASSWORD_IP))) {

                    String signIn = (String) user.getMetaData().get(TWITTER_SIGN_IN_IP);
                    String password = (String) user.getMetaData().get(TWITTER_PASSWORD_IP);
                    String updateText = (String) user.getMetaData().get(TWITTER_STATUS_UPDATE_TEXT_IP);

                    if (BlojsomUtils.checkNullOrBlank(updateText)) {
                        updateText = TWITTER_DEFAULT_STATUS_UPDATE_TEXT;
                    }
                    
                    if (("true".equals(user.getMetaData().get(TWITTER_UPDATE_ON_ENTRY_ADDED_IP))) ||
                            ("true".equals(user.getMetaData().get(TWITTER_UPDATE_ON_ENTRY_UPDATED_IP)))) {
                        String title = entryEvent.getEntry().getTitle();
                        String twitterUpdate = BlojsomUtils.urlEncode(
                                BlojsomUtils.escapeString(MessageFormat.format(updateText, new Object[] {title})));

                        Authenticator.setDefault(new TwitterAuthenticator(signIn, password));

                        try {
                            URL url = new URL(_twitterUpdateURL);
                            URLConnection urlConnection = url.openConnection();
                            urlConnection.setUseCaches(false);
                            urlConnection.setDoInput(true);
                            urlConnection.setDoOutput(true);
                            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                            String twitterData = TWITTER_STATUS_PARAMETER + "=" + twitterUpdate;
                            OutputStreamWriter twitterWriter = new OutputStreamWriter(urlConnection.getOutputStream());
                            twitterWriter.write(twitterData);
                            twitterWriter.flush();
                            twitterWriter.close();

                            // Read all the text returned by the server
                            BufferedReader twitterReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                            StringBuffer twitterReply = new StringBuffer();
                            String input;

                            while ((input = twitterReader.readLine()) != null) {
                                twitterReply.append(input);
                            }

                            twitterReader.close();

                            if (BlojsomUtils.checkNullOrBlank(twitterReply.toString())) {
                                if (_logger.isErrorEnabled()) {
                                    _logger.error("Error communicating update to Twitter");
                                }
                            } else {
                                if (_logger.isDebugEnabled()) {
                                    _logger.debug("Successfully sent update to Twitter");
                                }
                            }
                        } catch (IOException e) {
                            if (_logger.isErrorEnabled()) {
                                _logger.error(e);
                            }
                        }
                    } else {
                        if (_logger.isDebugEnabled()) {
                            _logger.debug("Twitter notification update not enabled for either add or update entry events");
                        }
                    }
                } else {
                    if (_logger.isDebugEnabled()) {
                        _logger.debug("Twitter sign in and/or password is null or blank");
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

    public class TwitterAuthenticator extends Authenticator {

        private String _username;
        private String _password;

        /**
         * Create a new instance of the Twitter authenticator
         *
         * @param username Twitter username
         * @param password Twitter password
         */
        public TwitterAuthenticator(String username, String password) {
            _username = username;
            _password = password;

        }

        /**
         * Return a {@link PasswordAuthentication} with the username and password for Twitter
         *
         * @return {@link PasswordAuthentication} with the username and password for Twitter
         */
        protected PasswordAuthentication getPasswordAuthentication() {
            // Return the information
            return new PasswordAuthentication(_username, _password.toCharArray());
        }
    }
}
