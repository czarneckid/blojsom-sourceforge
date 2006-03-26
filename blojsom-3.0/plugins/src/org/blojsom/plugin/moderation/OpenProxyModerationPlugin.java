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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

/**
 * Open proxy check plugin for comments and trackbacks. This plugin queries the <a href="http://dsbl.org/main">Distributed
 * Sender Blackhole List</a> if a comment or trackback is submitted. If the IP address of the requesting
 * host is on the blacklist, the comment or trackback is marked for moderation if moderation is enabled,
 * otherwise it is destroyed.
 * This plugin can work in conjunction with other moderation plugins as it looks for the comment or
 * trackback metadata.
 *
 * @author David Czarnecki
 * @version $Id: OpenProxyModerationPlugin.java,v 1.1 2006-03-26 21:36:28 czarneckid Exp $
 * @since blojsom 3.0
 */
public class OpenProxyModerationPlugin implements Plugin, Listener {

    private Log _logger = LogFactory.getLog(OpenProxyModerationPlugin.class);

    private static final String DELETE_OPENPROXY_SPAM_IP = "delete-openproxy-spam";

    private EventBroadcaster _eventBroadcaster;

    /**
     * Create a new instance of the open proxy comment check plugin
     */
    public OpenProxyModerationPlugin() {
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
     * Handle an event broadcast from another component
     *
     * @param event {@link Event} to be handled
     */
    public void handleEvent(Event event) {
    }

    /**
     * Process an event from another component
     *
     * @param event {@link Event} to be handled
     */
    public void processEvent(Event event) {
        if (event instanceof ResponseSubmissionEvent) {
            ResponseSubmissionEvent responseSubmissionEvent = (ResponseSubmissionEvent) event;

            String remoteIP = responseSubmissionEvent.getHttpServletRequest().getRemoteAddr();
            String[] ipAddress = remoteIP.split("\\.");
            StringBuffer reversedAddress = new StringBuffer();
            reversedAddress.append(ipAddress[3]).append(".").append(ipAddress[2]).append(".").append(ipAddress[1]).append(".").append(ipAddress[0]);

            try {
                InetAddress inetAddress = InetAddress.getByName(reversedAddress + ".list.dsbl.org");
                Map metaData = responseSubmissionEvent.getMetaData();

                String deleteOpenProxySpamPropertyValue = responseSubmissionEvent.getBlog().getProperty(DELETE_OPENPROXY_SPAM_IP);
                boolean deleteOpenProxySpam = Boolean.valueOf(deleteOpenProxySpamPropertyValue).booleanValue();

                if (responseSubmissionEvent instanceof CommentResponseSubmissionEvent) {
                    if (!deleteOpenProxySpam) {
                        metaData.put(CommentModerationPlugin.BLOJSOM_COMMENT_MODERATION_PLUGIN_APPROVED, Boolean.FALSE.toString());
                    } else {
                        metaData.put(CommentPlugin.BLOJSOM_PLUGIN_COMMENT_METADATA_DESTROY, Boolean.TRUE);
                    }
                } else if (responseSubmissionEvent instanceof TrackbackResponseSubmissionEvent) {
                    if (!deleteOpenProxySpam) {
                        metaData.put(TrackbackModerationPlugin.BLOJSOM_TRACKBACK_MODERATION_PLUGIN_APPROVED, Boolean.FALSE.toString());
                    } else {
                        metaData.put(TrackbackPlugin.BLOJSOM_PLUGIN_TRACKBACK_METADATA_DESTROY, Boolean.TRUE);
                    }
                } else if (responseSubmissionEvent instanceof PingbackResponseSubmissionEvent) {
                    if (deleteOpenProxySpam) {
                        metaData.put(PingbackPlugin.BLOJSOM_PLUGIN_PINGBACK_METADATA_DESTROY, Boolean.TRUE);
                    }
                }

                if (_logger.isDebugEnabled()) {
                    _logger.debug("Failed open proxy check for response submission for IP: " + inetAddress.getHostAddress() + "/" + inetAddress.getHostName());
                }
            } catch (UnknownHostException e) {
                // The IP address is unknown to the DSBL server
            }
        }
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
}