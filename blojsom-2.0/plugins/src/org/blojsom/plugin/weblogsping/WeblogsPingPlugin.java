/**
 * Copyright (c) 2003-2005, David A. Czarnecki
 * All rights reserved.
 *
 * Portions Copyright (c) 2003-2005 by Mark Lussier
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
package org.blojsom.plugin.weblogsping;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;
import org.blojsom.blog.Blog;
import org.blojsom.blog.BlogEntry;
import org.blojsom.blog.BlogUser;
import org.blojsom.blog.BlojsomConfiguration;
import org.blojsom.event.BlojsomEvent;
import org.blojsom.event.BlojsomListener;
import org.blojsom.plugin.BlojsomPlugin;
import org.blojsom.plugin.BlojsomPluginException;
import org.blojsom.plugin.admin.event.BlogEntryEvent;
import org.blojsom.util.BlojsomConstants;
import org.blojsom.util.BlojsomUtils;

import javax.servlet.ServletConfig;
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
 * @version $Id: WeblogsPingPlugin.java,v 1.18 2005-04-28 15:02:22 czarneckid Exp $
 * @since blojsom 1.9.2
 */
public class WeblogsPingPlugin implements BlojsomListener, BlojsomPlugin, BlojsomConstants {

    private Log _logger = LogFactory.getLog(WeblogsPingPlugin.class);

    private static final String WEBLOGS_PING_METHOD = "weblogUpdates.ping";
    private static final String WEBLOGS_EXTENDED_PING_METHOD = "weblogUpdates.extendedPing";
    private static final String DEFAULT_PREFERRED_SYNDICATION_FLAVOR = "rss2";

    public static final String BLOG_PING_URLS_IP = "blog-ping-urls";
    public static final String NO_PING_WEBLOGS_METADATA = "no-ping-weblogs";

    /**
     * Default constructor
     */
    public WeblogsPingPlugin() {
    }

    /**
     * Initialize this plugin. This method only called when the plugin is instantiated.
     *
     * @param servletConfig        Servlet config object for the plugin to retrieve any initialization parameters
     * @param blojsomConfiguration {@link org.blojsom.blog.BlojsomConfiguration} information
     * @throws BlojsomPluginException If there is an error initializing the plugin
     */
    public void init(ServletConfig servletConfig, BlojsomConfiguration blojsomConfiguration) throws BlojsomPluginException {
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
        return entries;
    }

    /**
     * Handle an event broadcast from another component
     *
     * @param event {@link org.blojsom.event.BlojsomEvent} to be handled
     */
    public void handleEvent(BlojsomEvent event) {
        if (event instanceof BlogEntryEvent) {
            BlogEntryEvent entryEvent = (BlogEntryEvent) event;
            Blog blog = entryEvent.getBlogUser().getBlog();
            String syndicationURL = blog.getBlogURL();

            // Check for meta-data indicating a ping should not be sent
            Map metaData = entryEvent.getBlogEntry().getMetaData();
            if (BlojsomUtils.checkMapForKey(metaData, NO_PING_WEBLOGS_METADATA)) {
                return;
            }

            // Check to see if there is a particular flavor the user wants to send with the extended ping
            String preferredSyndicationFlavor = blog.getBlogProperty(PREFERRED_SYNDICATION_FLAVOR);
            if (BlojsomUtils.checkNullOrBlank(preferredSyndicationFlavor)) {
                preferredSyndicationFlavor = DEFAULT_PREFERRED_SYNDICATION_FLAVOR;
            }
            syndicationURL = syndicationURL + "?flavor=" + preferredSyndicationFlavor;

            // If they are provided, loop through that list of URLs to ping
            String pingURLsIP = blog.getBlogProperty(BLOG_PING_URLS_IP);
            String[] pingURLs = BlojsomUtils.parseDelimitedList(pingURLsIP, WHITESPACE);
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
                    String pingURL = pingURLs[i];
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
                                _logger.error(e1);
                            } catch (IOException e1) {
                                _logger.error(e1);
                            }
                        } catch (IOException e) {
                            _logger.error(e);
                        }
                    } catch (MalformedURLException e) {
                        _logger.error(e);
                    }
                }
            }

            _logger.debug("Pinged notification URLs based on add/update blog entry event");
        }
    }

    /**
     * Process an event from another component
     *
     * @param event {@link BlojsomEvent} to be handled
     * @since blojsom 2.24
     */
    public void processEvent(BlojsomEvent event) {
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
}
