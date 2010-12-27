/**
 * Copyright (c) 2003, David A. Czarnecki
 * All rights reserved.
 *
 * Portions Copyright (c) 2003 by Nick Chalko
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
package org.ignition.blojsom.plugin.limiter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ignition.blojsom.blog.Blog;
import org.ignition.blojsom.blog.BlogEntry;
import org.ignition.blojsom.plugin.BlojsomPlugin;
import org.ignition.blojsom.plugin.BlojsomPluginException;
import org.ignition.blojsom.util.BlojsomUtils;
import org.ignition.blojsom.BlojsomException;

/**
 * Hide preview entries from normal display.
 * Preview entires have the word PREVIEW (by default) at the start of the title.
 * To see the preview items along with normal entries add the parameter
 * preview="true"
 *
 * @author <a href="http://nick.chalko.com"/>Nick Chalko</a>
 * @author David Czarnecki
 * @since blojsom 1.9
 * @version $Id: PreviewPlugin.java,v 1.1 2003-05-24 17:42:24 czarneckid Exp $
 */
public class PreviewPlugin implements BlojsomPlugin {

    private Log _logger = LogFactory.getLog(PreviewPlugin.class);

    /**
     * The initialize parameter for the name af the property file for this plugin.
     */
    private static final String BLOG_PREVIEW_CONFIGURATION_IP = "plugin-preview";

    /**
     * The Title Prefix that signifies a preview entry.  Defaults to <code>PREVIEW</code>
     */
    private static final String TITLE_PREFIX = "TITLE_PREFIX";

    private String _prefix = "PREVIEW";

    /**
     * The value to set the request parameter <code>preview</code> to make preview entries visible.
     * Defaults to <code>true</code>
     */
    private static final String PREVIEW_PASSWORD = "PREVIEW_PASSWORD";

    private String _password = "true";

    /**
     * Request parameter for the "preview"
     */
    private static final String PREVIEW_PARAM = "preview";

    /**
     * Default constructor
     */
    public PreviewPlugin() {
    }

    /**
     * Initialize this plugin. This method only called when the plugin is instantiated.
     *
     * @param servletConfig Servlet config object for the plugin to retrieve any initialization parameters
     * @param blog {@link Blog} instance
     * @throws BlojsomPluginException If there is an error initializing the plugin
     */
    public void init(ServletConfig servletConfig, Blog blog)
            throws BlojsomPluginException {
        try {
            Properties prop =
                    BlojsomUtils.loadProperties(servletConfig, BLOG_PREVIEW_CONFIGURATION_IP, false);

            if (prop.containsKey(TITLE_PREFIX)) {
                _prefix = prop.getProperty(TITLE_PREFIX, _prefix);
            }
            if (prop.containsKey(PREVIEW_PASSWORD)) {
                _password = prop.getProperty(PREVIEW_PASSWORD, _password);
            }
        } catch (BlojsomException e) {
            throw new BlojsomPluginException(e);
        }
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
    public BlogEntry[] process(
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse,
            Map context,
            BlogEntry[] entries)
            throws BlojsomPluginException {

        // Determine if the user wants to preview posts
        String previewParam = httpServletRequest.getParameter(PREVIEW_PARAM);
        if (previewParam != null && previewParam.equals(_password)) {
            return entries;
        } else {
            List postedEntries = new ArrayList(entries.length);
            for (int i = 0; i < entries.length; i++) {
                BlogEntry entry = entries[i];
                if (!entry.getTitle().startsWith(_prefix)) {
                    postedEntries.add(entry);
                }
            }
            return (BlogEntry[]) postedEntries.toArray(
                    new BlogEntry[postedEntries.size()]);
        }
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
