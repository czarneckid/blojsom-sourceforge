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
package org.blojsom.plugin.weblogsping;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlrpc.AsyncCallback;
import org.apache.xmlrpc.XmlRpcClient;
import org.blojsom.blog.Blog;
import org.blojsom.blog.BlogEntry;
import org.blojsom.blog.BlogUser;
import org.blojsom.blog.BlojsomConfiguration;
import org.blojsom.plugin.BlojsomPlugin;
import org.blojsom.plugin.BlojsomPluginException;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Map;
import java.util.Vector;
import java.util.HashMap;

/**
 * WeblogsPingPlugin
 *
 * @author David Czarnecki
 * @since blojsom 1.9.2
 * @version $Id: WeblogsPingPlugin.java,v 1.5 2003-11-11 03:28:45 czarneckid Exp $
 */
public class WeblogsPingPlugin implements BlojsomPlugin {

    private Log _logger = LogFactory.getLog(WeblogsPingPlugin.class);

    private static final String WEBLOGS_PING_URL = "http://rpc.weblogs.com:80/RPC2";
    private static final String WEBLO_GS_PING_URL = "http://ping.blo.gs/";
    private static final String WEBLOGS_PING_METHOD = "weblogUpdates.ping";

    private WeblogsPingPluginAsyncCallback _callbackHandler;

    private Map _userLastPingMap;

    /**
     * Default constructor
     */
    public WeblogsPingPlugin() {
    }

    /**
     * Initialize this plugin. This method only called when the plugin is instantiated.
     *
     * @param servletConfig Servlet config object for the plugin to retrieve any initialization parameters
     * @param blojsomConfiguration {@link org.blojsom.blog.BlojsomConfiguration} information
     * @throws BlojsomPluginException If there is an error initializing the plugin
     */
    public void init(ServletConfig servletConfig, BlojsomConfiguration blojsomConfiguration) throws BlojsomPluginException {
        _callbackHandler = new WeblogsPingPluginAsyncCallback();
        _userLastPingMap = new HashMap(5);
        String user;
        String[] users = blojsomConfiguration.getBlojsomUsers();
        for (int i = 0; i < users.length; i++) {
            user = blojsomConfiguration.getBlojsomUsers()[i];
            _userLastPingMap.put(user, new Date());
        }
    }

    /**
     * Process the blog entries
     *
     * @param httpServletRequest Request
     * @param httpServletResponse Response
     * @param user {@link BlogUser} instance
     * @param context Context
     * @param entries Blog entries retrieved for the particular request
     * @return Modified set of blog entries
     * @throws BlojsomPluginException If there is an error processing the blog entries
     */
    public BlogEntry[] process(HttpServletRequest httpServletRequest,
                               HttpServletResponse httpServletResponse,
                               BlogUser user,
                               Map context,
                               BlogEntry[] entries) throws BlojsomPluginException {
        Blog blog = user.getBlog();

        // If there are no entries return
        if (entries.length <= 0) {
            return entries;
        } else {
            // Pull the latest entry, check its date to see if its newer than the lastPingDate, and
            // if so, ping weblogs.com
            BlogEntry entry = entries[0];
            Date lastPingDate = (Date) _userLastPingMap.get(user.getId());
            if (lastPingDate.before(entry.getDate())) {
                lastPingDate = entry.getDate();
                _userLastPingMap.put(user.getId(), lastPingDate);

                try {
                    XmlRpcClient weblogsComclient = new XmlRpcClient(WEBLOGS_PING_URL);
                    Vector params = new Vector();
                    params.add(blog.getBlogName());
                    params.add(blog.getBlogURL());

                    // Ping weblogs.com
                    weblogsComclient.executeAsync(WEBLOGS_PING_METHOD, params, _callbackHandler);

                    // Ping weblo.gs
                    XmlRpcClient weblogsClient = new XmlRpcClient(WEBLO_GS_PING_URL);
                    weblogsClient.executeAsync(WEBLOGS_PING_METHOD, params, _callbackHandler);
                } catch (IOException e) {
                    _logger.error(e);
                }
            } else {
                _logger.debug("Latest entry date occurs before latest ping date.");
            }
        }

        return entries;
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
     * Asynchronous callback handler for the weblogs.com ping
     */
    private class WeblogsPingPluginAsyncCallback implements AsyncCallback {

        /**
         * Default constructor
         */
        public WeblogsPingPluginAsyncCallback() {
        }

        /**
         * Call went ok, handle result.
         *
         * @param o Return object
         * @param url URL
         * @param s String
         */
        public void handleResult(Object o, URL url, String s) {
            _logger.debug(o.toString());
        }

        /**
         * Something went wrong, handle error.
         *
         * @param e Exception containing error from XML-RPC call
         * @param url URL
         * @param s String
         */
        public void handleError(Exception e, URL url, String s) {
            _logger.error(e);
        }
    }
}
