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
package org.ignition.blojsom.plugin;

import org.ignition.blojsom.blog.BlogEntry;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;
import java.util.Map;

/**
 * ShowMeMorePlugin
 *
 * @author David Czarnecki
 * @version $Id: ShowMeMorePlugin.java,v 1.5 2003-03-23 19:27:47 czarneckid Exp $
 */
public class ShowMeMorePlugin implements BlojsomPlugin {

    private static final String SHOW_ME_MORE_CONFIG_IP = "plugin-showmemore";
    private static final String ENTRY_LENGTH_CUTOFF = "entry-length-cutoff";
    private static final String SHOW_ME_MORE_TEXT = "show-me-more-text";
    private static final String SHOW_ME_MORE_PARAM = "smm";

    private int _cutoff;
    private String _moreText;

    /**
     * Default constructor
     */
    public ShowMeMorePlugin() {
    }

    /**
     * Initialize this plugin. This method only called when the plugin is instantiated.
     *
     * @param servletConfig Servlet config object for the plugin to retrieve any initialization parameters
     * @param blogProperties Read-only properties for the Blog
     * @throws BlojsomPluginException If there is an error initializing the plugin
     */
    public void init(ServletConfig servletConfig, HashMap blogProperties) throws BlojsomPluginException {
        String showMeMoreConfiguration = servletConfig.getInitParameter(SHOW_ME_MORE_CONFIG_IP);
        if (showMeMoreConfiguration == null || "".equals(showMeMoreConfiguration)) {
            throw new BlojsomPluginException("No value given for: " + SHOW_ME_MORE_CONFIG_IP + " configuration parameter");
        }

        Properties showMeMoreProperties = new Properties();
        InputStream is = servletConfig.getServletContext().getResourceAsStream(showMeMoreConfiguration);
        try {
            showMeMoreProperties.load(is);
            _moreText = showMeMoreProperties.getProperty(SHOW_ME_MORE_TEXT);
            _cutoff = Integer.parseInt(showMeMoreProperties.getProperty(ENTRY_LENGTH_CUTOFF));
        } catch (IOException e) {
            throw new BlojsomPluginException(e);
        }
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
        String wantsToSeeMore = httpServletRequest.getParameter(SHOW_ME_MORE_PARAM);
        if ("y".equalsIgnoreCase(wantsToSeeMore)) {
            return entries;
        } else {
            for (int i = 0; i < entries.length; i++) {
                BlogEntry entry = entries[i];
                String description = entry.getDescription();
                if (description.length() > _cutoff) {
                    StringBuffer partialDescription = new StringBuffer(description.substring(0, _cutoff));
                    partialDescription.append("&nbsp; <a href=\"");
                    partialDescription.append(entry.getLink());
                    partialDescription.append("&");
                    partialDescription.append(SHOW_ME_MORE_PARAM);
                    partialDescription.append("=y\">");
                    partialDescription.append(_moreText);
                    partialDescription.append("</a>");
                    entry.setDescription(partialDescription.toString());
                }
            }
            return entries;
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
