package org.ignition.blojsom.plugin.weblogsping;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlrpc.AsyncCallback;
import org.apache.xmlrpc.XmlRpcClient;
import org.ignition.blojsom.BlojsomException;
import org.ignition.blojsom.blog.Blog;
import org.ignition.blojsom.blog.BlogEntry;
import org.ignition.blojsom.plugin.BlojsomPlugin;
import org.ignition.blojsom.plugin.BlojsomPluginException;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Vector;

/**
 * WeblogsPingPlugin
 *
 * @author David Czarnecki
 * @since blojsom 1.9.2
 * @version $Id: WeblogsPingPlugin.java,v 1.1 2003-06-18 04:13:35 czarneckid Exp $
 */
public class WeblogsPingPlugin implements BlojsomPlugin {

    private Log _logger = LogFactory.getLog(WeblogsPingPlugin.class);

    private static final String DEFAULT_WEBLOGS_URL = "http://rpc.weblogs.com:80/RPC2";

    private static final String WEBLOGS_PING_METADATA = "weblogs-ping";
    private static final String WEBLOGS_PING_VALUE = "true";
    private Blog _blog;
    private WeblogsPingPluginAsyncCallback _callbackHandler;

    /**
     * Default constructor
     */
    public WeblogsPingPlugin() {
    }

    /**
     * Initialize this plugin. This method only called when the plugin is instantiated.
     *
     * @param servletConfig Servlet config object for the plugin to retrieve any initialization parameters
     * @param blog {@link Blog} instance
     * @throws BlojsomPluginException If there is an error initializing the plugin
     */
    public void init(ServletConfig servletConfig, Blog blog) throws BlojsomPluginException {
        _blog = blog;
        _callbackHandler = new WeblogsPingPluginAsyncCallback();
    }

    /**
     * Process the blog entries
     *
     * @param httpServletRequest Request
     * @param httpServletResponse Response
     * @param context Context
     * @param entries Blog entries retrieved for the particular request
     * @return Modified set of blog entries
     * @throws BlojsomPluginException If there is an error processing the blog entries
     */
    public BlogEntry[] process(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                               Map context, BlogEntry[] entries) throws BlojsomPluginException {

        boolean shouldPingWeblogs = false;
        for (int i = 0; i < entries.length; i++) {
            BlogEntry entry = entries[i];
            Map entryMetaData = entry.getMetaData();
            if (!entryMetaData.containsKey(WEBLOGS_PING_METADATA)) {
                shouldPingWeblogs = true;
                entryMetaData.put(WEBLOGS_PING_METADATA, WEBLOGS_PING_VALUE);
                try {
                    entry.save(_blog);
                } catch (BlojsomException e) {
                    _logger.error("Error saving blog entry after adding weblogs-ping meta-data.", e);
                }
            }

            if (shouldPingWeblogs) {
                try {
                    XmlRpcClient client = new XmlRpcClient(DEFAULT_WEBLOGS_URL);
                    Vector params = new Vector();
                    params.add(_blog.getBlogName());
                    params.add(_blog.getBlogURL());
                    client.executeAsync("weblogUpdates.ping", params, _callbackHandler);
                } catch (IOException e) {
                    _logger.error(e);
                }
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
