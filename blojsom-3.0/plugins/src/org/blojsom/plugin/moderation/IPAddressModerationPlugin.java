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
package org.blojsom.plugin.moderation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.blojsom.blog.Blog;
import org.blojsom.blog.Entry;
import org.blojsom.event.Event;
import org.blojsom.event.EventBroadcaster;
import org.blojsom.event.Listener;
import org.blojsom.plugin.Plugin;
import org.blojsom.plugin.PluginException;
import org.blojsom.plugin.comment.CommentModerationPlugin;
import org.blojsom.plugin.comment.CommentPlugin;
import org.blojsom.plugin.comment.event.CommentResponseSubmissionEvent;
import org.blojsom.plugin.pingback.PingbackPlugin;
import org.blojsom.plugin.pingback.event.PingbackResponseSubmissionEvent;
import org.blojsom.plugin.response.event.ResponseSubmissionEvent;
import org.blojsom.plugin.trackback.TrackbackModerationPlugin;
import org.blojsom.plugin.trackback.TrackbackPlugin;
import org.blojsom.plugin.trackback.event.TrackbackResponseSubmissionEvent;
import org.blojsom.util.BlojsomUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * IP address moderation plugin
 *
 * @author David Czarnecki
 * @version $Id: IPAddressModerationPlugin.java,v 1.2 2007-01-17 02:35:12 czarneckid Exp $
 * @since blojsom 3.0
 */
public class IPAddressModerationPlugin implements Plugin, Listener {

    private Log _logger = LogFactory.getLog(IPAddressModerationPlugin.class);

    private static final String IP_BLACKLIST_IP = "ip-blacklist";
    private static final String IP_WHITELIST_IP = "ip-whitelist";
    private static final String DELETE_IPSPAM = "delete-ipspam";
    private static final boolean DEFAULT_DELETE_IPSPAM = false;

    private EventBroadcaster _eventBroadcaster;

    /**
     * Create a new instance of the IP address moderation plugin
     */
    public IPAddressModerationPlugin() {
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
     * Initialize this plugin. This method only called when the plugin is instantiated.
     *
     * @throws org.blojsom.plugin.PluginException
     *          If there is an error initializing the plugin
     */
    public void init() throws PluginException {
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
     * Handle an event broadcast from another component
     *
     * @param event {@link org.blojsom.event.Event} to be handled
     */
    public void handleEvent(Event event) {
    }

    /**
     * Load the list of IP addresses from whitelist or blacklist from the blog properties
     *
     * @param blog {@link Blog}
     * @param property Whitelist or Blacklist property
     * @return List of IP addresses
     */
    protected List loadIPList(Blog blog, String property) {
        ArrayList ipAddresses = new ArrayList();
        String ipAddressValues = blog.getProperty(property);

        if (!BlojsomUtils.checkNullOrBlank(ipAddressValues)) {
            try {
                BufferedReader br = new BufferedReader(new StringReader(ipAddressValues));
                String ipAddress;

                while ((ipAddress = br.readLine()) != null) {
                    ipAddresses.add(ipAddress);
                }

                br.close();
            } catch (IOException e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }
            }
        }

        return ipAddresses;
    }

    /**
     * Process an event from another component
     *
     * @param event {@link org.blojsom.event.Event} to be handled
     */
    public void processEvent(Event event) {
        if (event instanceof ResponseSubmissionEvent) {
            ResponseSubmissionEvent responseSubmissionEvent = (ResponseSubmissionEvent) event;
            String requestIPAddress = responseSubmissionEvent.getHttpServletRequest().getRemoteAddr();

            if (!BlojsomUtils.checkNullOrBlank(requestIPAddress)) {
                boolean ipInWhitelist = false;
                List ipAddresses = loadIPList(responseSubmissionEvent.getBlog(), IP_WHITELIST_IP);

                if (ipAddresses.size() > 0) {
                    String ipAddress;
                    Iterator ipIterator = ipAddresses.iterator();

                    while (ipIterator.hasNext()) {
                        ipAddress = (String) ipIterator.next();
                        if (requestIPAddress.matches(ipAddress) || requestIPAddress.indexOf(ipAddress) != -1) {
                            if (_logger.isDebugEnabled()) {
                                _logger.debug("IP address found in whitelist: " + requestIPAddress);
                            }

                            ipInWhitelist = true;
                            break;
                        }
                    }
                } else {
                    if (_logger.isDebugEnabled()) {
                        _logger.debug("IP address whitelist not populated");
                    }
                }

                ipAddresses = loadIPList(responseSubmissionEvent.getBlog(), IP_BLACKLIST_IP);
                if (ipAddresses.size() > 0 && !ipInWhitelist) {
                    String ipAddress;
                    Iterator ipIterator = ipAddresses.iterator();

                    boolean deleteIPSpam = DEFAULT_DELETE_IPSPAM;
                    Map metaData = responseSubmissionEvent.getMetaData();

                    String deleteIPSpamValue = responseSubmissionEvent.getBlog().getProperty(DELETE_IPSPAM);
                    if (!BlojsomUtils.checkNullOrBlank(deleteIPSpamValue)) {
                        deleteIPSpam = Boolean.valueOf(deleteIPSpamValue).booleanValue();
                    }

                    boolean ipSpamFound = false;
                    while (ipIterator.hasNext()) {
                        ipAddress = (String) ipIterator.next();
                        if (requestIPAddress.matches(ipAddress) || requestIPAddress.indexOf(ipAddress) != -1) {
                            if (_logger.isDebugEnabled()) {
                                _logger.debug("IP address found in blacklist: " + requestIPAddress);
                            }

                            ipSpamFound = true;
                            break;
                        }
                    }

                    if (ipSpamFound) {
                        if (!deleteIPSpam) {
                            if (_logger.isDebugEnabled()) {
                                _logger.debug("Marking response for moderation");
                            }
                        } else {
                            if (_logger.isDebugEnabled()) {
                                _logger.debug("Marking response for automatic deletion");
                            }
                        }

                        if (responseSubmissionEvent instanceof CommentResponseSubmissionEvent) {
                            if (!deleteIPSpam) {
                                metaData.put(CommentModerationPlugin.BLOJSOM_COMMENT_MODERATION_PLUGIN_APPROVED, Boolean.FALSE.toString());
                            } else {
                                metaData.put(CommentPlugin.BLOJSOM_PLUGIN_COMMENT_METADATA_DESTROY, Boolean.TRUE);
                            }
                        } else if (responseSubmissionEvent instanceof TrackbackResponseSubmissionEvent) {
                            if (!deleteIPSpam) {
                                metaData.put(TrackbackModerationPlugin.BLOJSOM_TRACKBACK_MODERATION_PLUGIN_APPROVED, Boolean.FALSE.toString());
                            } else {
                                metaData.put(TrackbackPlugin.BLOJSOM_PLUGIN_TRACKBACK_METADATA_DESTROY, Boolean.TRUE);
                            }
                        } else if (responseSubmissionEvent instanceof PingbackResponseSubmissionEvent) {
                            if (deleteIPSpam) {
                                metaData.put(PingbackPlugin.BLOJSOM_PLUGIN_PINGBACK_METADATA_DESTROY, Boolean.TRUE);
                            }
                        }
                    }
                } else {
                    if (_logger.isDebugEnabled()) {
                        _logger.debug("IP address blacklist not populated");
                    }
                }
            } else {
                if (_logger.isDebugEnabled()) {
                    _logger.debug("IP address not available");
                }
            }
        }
    }
}