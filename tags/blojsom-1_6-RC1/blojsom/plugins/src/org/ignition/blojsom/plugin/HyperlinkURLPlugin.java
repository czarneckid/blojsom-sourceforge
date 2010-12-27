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
 * Neither the name of the "David A. Czarnecki" nor the names of
 * its contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
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
package org.ignition.blojsom.plugin;

import org.ignition.blojsom.blog.BlogEntry;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Hyperlink HREFing Plugin
 *
 * @author Mark Lussier
 * @version $Id: HyperlinkURLPlugin.java,v 1.7 2003-03-23 18:31:19 intabulas Exp $
 */
public class HyperlinkURLPlugin implements BlojsomPlugin {

    /**
     * Protocol support for HREF links
     */
    private static final String URL_PROTOCOLS = "((ftp|http|https|gopher|mailto|news|nntp|telnet|wais|file)";

    /**
     * Regular expression to identify URLs in the entry
     */
    private static final String URL_REGEX = "(^|[ t\rn])" + URL_PROTOCOLS
            + ":[A-Za-z0-9/](([A-Za-z0-9$_.+!*(),;/?:@&~=-])|%[A-Fa-f0-9]{2})+)";

    /**
     * The resulting replace string for formatting the href
     */
    private static final String HREF_EXPRESSION = " <a href=\"$2\" target=\"_blank\">$2</a>";

    /**
     * Initialize this plugin. This method only called when the plugin is instantiated.
     *
     * @param servletConfig Servlet config object for the plugin to retrieve any initialization parameters
     * @param blogProperties Read-only properties for the Blog
     * @throws BlojsomPluginException If there is an error initializing the plugin
     */
    public void init(ServletConfig servletConfig, HashMap blogProperties) throws BlojsomPluginException {
    }

    /**
     * Process the blog entries
     *
     * @param httpServletRequest Request
     * @param context Context
     * @param entries Blog entries retrieved for the particular request
     * @return Modified set of blog entries
     * @throws BlojsomPluginException If there is an error processing the blog entries
     */
    public BlogEntry[] process(HttpServletRequest httpServletRequest, Map context, BlogEntry[] entries) throws BlojsomPluginException {
        for (int i = 0; i < entries.length; i++) {
            BlogEntry entry = entries[i];

            entry.setDescription(entry.getDescription().replaceAll(URL_REGEX, HREF_EXPRESSION));
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
}
