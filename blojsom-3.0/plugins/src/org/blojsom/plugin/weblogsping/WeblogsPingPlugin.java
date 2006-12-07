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
package org.blojsom.plugin.weblogsping;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;
import org.blojsom.blog.Blog;
import org.blojsom.blog.Entry;
import org.blojsom.event.Event;
import org.blojsom.event.EventBroadcaster;
import org.blojsom.event.Filter;
import org.blojsom.event.Listener;
import org.blojsom.plugin.Plugin;
import org.blojsom.plugin.PluginException;
import org.blojsom.plugin.admin.event.EntryAddedEvent;
import org.blojsom.plugin.admin.event.EntryDeletedEvent;
import org.blojsom.plugin.admin.event.EntryEvent;
import org.blojsom.plugin.admin.event.EntryUpdatedEvent;
import org.blojsom.util.BlojsomConstants;
import org.blojsom.util.BlojsomUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.Vector;

/**
 * WeblogsPingPlugin
 *
 * @author David Czarnecki
 * @version $Id: WeblogsPingPlugin.java,v 1.3 2006-12-07 19:45:18 czarneckid Exp $
 * @since blojsom 3.0
 */
public class WeblogsPingPlugin implements Listener, Plugin {

    private Log _logger = LogFactory.getLog(WeblogsPingPlugin.class);

    private static final String WEBLOGS_PING_METHOD = "weblogUpdates.ping";
    private static final String WEBLOGS_EXTENDED_PING_METHOD = "weblogUpdates.extendedPing";
    private static final String DEFAULT_PREFERRED_SYNDICATION_FLAVOR = "rss2";

    public static final String BLOG_PING_URLS_IP = "blog-ping-urls";
    public static final String NO_PING_WEBLOGS_METADATA = "no-ping-weblogs";

    private EventBroadcaster _eventBroadcaster;

    /**
     * Default constructor
     */
    public WeblogsPingPlugin() {
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
     * @throws PluginException If there is an error initializing the plugin
     */
    public void init() throws PluginException {
        Filter pingEventFilter = new Filter() {
            public boolean processEvent(Event event) {
                return (event instanceof EntryAddedEvent || event instanceof EntryDeletedEvent || event instanceof EntryUpdatedEvent);
            }
        };

        _eventBroadcaster.addListener(this, pingEventFilter);
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
        if (event instanceof EntryEvent) {
            EntryEvent entryEvent = (EntryEvent) event;
            Blog blog = entryEvent.getBlog();
            String syndicationURL = blog.getBlogURL();

            // Check for meta-data indicating a ping should not be sent
            Map metaData = entryEvent.getEntry().getMetaData();
            if (BlojsomUtils.checkMapForKey(metaData, NO_PING_WEBLOGS_METADATA)) {
                return;
            }

            // Check to see if there is a particular flavor the user wants to send with the extended ping
            String preferredSyndicationFlavor = blog.getProperty(BlojsomConstants.PREFERRED_SYNDICATION_FLAVOR);
            if (BlojsomUtils.checkNullOrBlank(preferredSyndicationFlavor)) {
                preferredSyndicationFlavor = DEFAULT_PREFERRED_SYNDICATION_FLAVOR;
            }
            syndicationURL = syndicationURL + "/feed/" + preferredSyndicationFlavor + "/";

            // If they are provided, loop through that list of URLs to ping
            String pingURLsIP = blog.getProperty(BLOG_PING_URLS_IP);
            String[] pingURLs = BlojsomUtils.parseDelimitedList(pingURLsIP, BlojsomConstants.WHITESPACE);
            if (pingURLs != null && pingURLs.length > 0) {
                Vector params = new Vector();
                Vector extendedParams = new Vector();
                params.add(blog.getBlogName());
                extendedParams.add(blog.getBlogName());
                params.add(blog.getBlogURL());
                extendedParams.add(blog.getBlogURL());
                extendedParams.add(blog.getBlogURL());
                extendedParams.add(syndicationURL);

                for (int i = 0; i < pingURLs.length; i++) {
                    String pingURL = pingURLs[i].trim();
                    try {
                        XmlRpcClient weblogsPingClient = new XmlRpcClient(pingURL);
                        // Try sending an extended weblogs ping first followed by the normal weblogs ping if failed
                        try {
                            weblogsPingClient.execute(WEBLOGS_EXTENDED_PING_METHOD, extendedParams);
                        } catch (XmlRpcException e) {
                            _logger.error(e);
                            try {
                                weblogsPingClient.execute(WEBLOGS_PING_METHOD, params);
                            } catch (XmlRpcException e1) {
                                if (_logger.isErrorEnabled()) {
                                    _logger.error(e1);
                                }
                            } catch (IOException e1) {
                                if (_logger.isErrorEnabled()) {
                                    _logger.error(e1);
                                }
                            }
                        } catch (IOException e) {
                            if (_logger.isErrorEnabled()) {
                                _logger.error(e);
                            }
                        }
                    } catch (MalformedURLException e) {
                        if (_logger.isErrorEnabled()) {
                            _logger.error(e);
                        }
                    }
                }

                if (_logger.isDebugEnabled()) {
                    _logger.debug("Pinged notification URLs based on blog entry event");
                }
            } else {
                if (_logger.isDebugEnabled()) {
                    _logger.debug("No ping notification URLs specified");
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
}
