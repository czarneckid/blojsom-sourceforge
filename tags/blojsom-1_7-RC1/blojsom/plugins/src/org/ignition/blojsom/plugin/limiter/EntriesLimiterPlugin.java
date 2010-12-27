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
package org.ignition.blojsom.plugin.limiter;

import org.ignition.blojsom.plugin.BlojsomPlugin;
import org.ignition.blojsom.plugin.BlojsomPluginException;
import org.ignition.blojsom.blog.BlogEntry;
import org.ignition.blojsom.util.BlojsomConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * EntriesLimiterPlugin
 *
 * @author David Czarnecki
 * @version $Id: EntriesLimiterPlugin.java,v 1.2 2003-03-31 03:47:53 czarneckid Exp $
 */
public class EntriesLimiterPlugin implements BlojsomPlugin {

    private Log _logger = LogFactory.getLog(EntriesLimiterPlugin.class);
    private Integer _blogDisplayEntries;

    /**
     * Request parameter for the "entries"
     */
    public static final String ENTRIES_PARAM = "entries";

    /**
     * Default constructor
     */
    public EntriesLimiterPlugin() {
    }

    /**
     * Initialize this plugin. This method only called when the plugin is instantiated.
     *
     * @param servletConfig Servlet config object for the plugin to retrieve any initialization parameters
     * @param blogProperties Read-only properties for the Blog
     * @throws BlojsomPluginException If there is an error initializing the plugin
     */
    public void init(ServletConfig servletConfig, HashMap blogProperties) throws BlojsomPluginException {
        _blogDisplayEntries = (Integer) blogProperties.get(BlojsomConstants.BLOG_ENTRIES_DISPLAY_IP);
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

        Integer blogDisplayEntries = new Integer(_blogDisplayEntries.intValue());

        // Determine if the user wants to override the number of displayed entries
        String entriesParam = httpServletRequest.getParameter(ENTRIES_PARAM);
        if (entriesParam != null || !"".equals(entriesParam)) {
            try {
                blogDisplayEntries = new Integer(Integer.parseInt(entriesParam));
                if ((blogDisplayEntries.intValue() <= 0) || (blogDisplayEntries.intValue() > entries.length)) {
                    _logger.debug("Display entries value out of range: " + blogDisplayEntries);
                    blogDisplayEntries = new Integer(_blogDisplayEntries.intValue());
                }
                _logger.debug("Overriding display entries: " + blogDisplayEntries);
            } catch (NumberFormatException e) {
                _logger.debug("Invalid display entries value or no value supplied. Using: " + blogDisplayEntries);
            }
        }

        if (blogDisplayEntries.intValue() == -1) {
            return entries;
        } else {
            BlogEntry[] filteredEntries = new BlogEntry[blogDisplayEntries.intValue()];
            for (int i = 0; i < blogDisplayEntries.intValue(); i++) {
                filteredEntries[i] = entries[i];
            }

            return filteredEntries;
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
