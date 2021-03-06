/**
 * Copyright (c) 2003-2004, David A. Czarnecki
 * All rights reserved.
 *
 * Portions Copyright (c) 2003-2004 by Mark Lussier
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
package org.blojsom.plugin.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.blojsom.blog.BlogEntry;
import org.blojsom.blog.BlogUser;
import org.blojsom.blog.BlojsomConfiguration;
import org.blojsom.plugin.BlojsomPlugin;
import org.blojsom.plugin.BlojsomPluginException;
import org.blojsom.util.BlojsomUtils;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.util.Map;

/**
 * RSSEnclosurePlugin
 *
 * @author David Czarnecki
 * @version $Id: RSSEnclosurePlugin.java,v 1.1 2004-10-15 22:17:09 czarneckid Exp $
 * @since blojsom 2.20
 */
public class RSSEnclosurePlugin implements BlojsomPlugin {

    private Log _logger = LogFactory.getLog(RSSEnclosurePlugin.class);

    private BlojsomConfiguration _blojsomConfiguration;

    public static final String DEFAULT_MIME_TYPE = "application/octet-stream";
    public static final String METADATA_RSS_ENCLOSURE = "rss-enclosure";

    /**
     * Default constructor
     */
    public RSSEnclosurePlugin() {
    }

    /**
     * Initialize this plugin. This method only called when the plugin is
     * instantiated.
     *
     * @param servletConfig        Servlet config object for the plugin to
     *                             retrieve any initialization parameters
     * @param blojsomConfiguration {@link org.blojsom.blog.BlojsomConfiguration}
     *                             information
     * @throws org.blojsom.plugin.BlojsomPluginException
     *          If there is an error initializing the plugin
     */
    public void init(ServletConfig servletConfig, BlojsomConfiguration
            blojsomConfiguration) throws BlojsomPluginException {
        _blojsomConfiguration = blojsomConfiguration;
        _logger.debug("Initialized RSS enclosures plugin");
    }

    /**
     * Process the blog entries
     *
     * @param httpServletRequest  Request
     * @param httpServletResponse Response
     * @param user                {@link org.blojsom.blog.BlogUser} instance
     * @param context             Context
     * @param entries             Blog entries retrieved for the particular
     *                            request
     * @return Modified set of blog entries
     * @throws org.blojsom.plugin.BlojsomPluginException
     *          If there is an error processing the blog entries
     */
    public BlogEntry[] process(HttpServletRequest httpServletRequest,
                               HttpServletResponse httpServletResponse, BlogUser user, Map context,
                               BlogEntry[] entries) throws BlojsomPluginException {
        for (int i = 0; i < entries.length; i++) {
            BlogEntry entry = entries[i];
            if (BlojsomUtils.checkMapForKey(entry.getMetaData(),
                    METADATA_RSS_ENCLOSURE)) {
                String enclosureName = BlojsomUtils.getFilenameFromPath((String)
                        entry.getMetaData().get(METADATA_RSS_ENCLOSURE));
                File enclosure = new
                        File(_blojsomConfiguration.getInstallationDirectory()
                        + _blojsomConfiguration.getResourceDirectory() +
                        user.getId() + "/" + enclosureName);
                if (enclosure.exists()) {
                    _logger.debug("Adding enclosure to entry for file: " + enclosureName);

                    MimetypesFileTypeMap mimetypesFileTypeMap = new
                            MimetypesFileTypeMap();
                    String type =
                            mimetypesFileTypeMap.getContentType(enclosure);

                    StringBuffer enclosureElement = new StringBuffer();
                    enclosureElement.append("<enclosure url=\"");
                    enclosureElement.append(user.getBlog().getBlogBaseURL());
                    enclosureElement.append(_blojsomConfiguration.getResourceDirectory());
                    enclosureElement.append(user.getId());
                    enclosureElement.append("/");
                    enclosureElement.append(enclosure.getName());
                    enclosureElement.append("\" length=\"");
                    enclosureElement.append(enclosure.length());
                    enclosureElement.append("\" type=\"");
                    if (BlojsomUtils.checkNullOrBlank(type)) {
                        type = DEFAULT_MIME_TYPE;
                    }
                    enclosureElement.append(type);
                    enclosureElement.append("\" />");

                    entry.getMetaData().put(METADATA_RSS_ENCLOSURE,
                            enclosureElement.toString());
                    _logger.debug("Added enclosure: " + enclosureElement.toString());
                }
            }
        }

        return entries;
    }

    /**
     * Perform any cleanup for the plugin. Called after {@link #process}.
     *
     * @throws org.blojsom.plugin.BlojsomPluginException
     *          If there is an error performing cleanup for this plugin
     */
    public void cleanup() throws BlojsomPluginException {
    }

    /**
     * Called when BlojsomServlet is taken out of service
     *
     * @throws org.blojsom.plugin.BlojsomPluginException
     *          If there is an error in finalizing this plugin
     */
    public void destroy() throws BlojsomPluginException {
    }
}

