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
package org.blojsom.plugin.limiter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.blojsom.blog.BlogEntry;
import org.blojsom.blog.BlogUser;
import org.blojsom.blog.BlojsomConfiguration;
import org.blojsom.plugin.BlojsomPlugin;
import org.blojsom.plugin.BlojsomPluginException;
import org.blojsom.util.BlojsomConstants;
import org.blojsom.util.BlojsomUtils;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * ConditionalGetPlugin
 * 
 * @author czarnecki
 * @version $Id: ConditionalGetPlugin.java,v 1.1 2004-01-05 01:10:39 czarneckid Exp $
 */
public class ConditionalGetPlugin implements BlojsomPlugin, BlojsomConstants {

    private Log _logger = LogFactory.getLog(ConditionalGetPlugin.class);

    private static final String CONDITIONAL_GET_CONFIGURATION_IP = "plugin-conditional-get";
    private static final String IF_MODIFIED_SINCE_HEADER = "If-Modified-Since";

    private Map _ifModifiedSinceMap;
    private Map _defaultConditionalGetFlavors;
    private Map _conditionalGetFlavorsPerUser;

    /**
     *
     */
    public ConditionalGetPlugin() {
    }

    /**
     * Initialize this plugin. This method only called when the plugin is instantiated.
     *
     * @param servletConfig        Servlet config object for the plugin to retrieve any initialization parameters
     * @param blojsomConfiguration {@link org.blojsom.blog.BlojsomConfiguration} information
     * @throws org.blojsom.plugin.BlojsomPluginException
     *          If there is an error initializing the plugin
     */
    public void init(ServletConfig servletConfig, BlojsomConfiguration blojsomConfiguration) throws BlojsomPluginException {
        // Setup a map for each user for the If-Modified-Since header check
        _ifModifiedSinceMap = new HashMap(blojsomConfiguration.getBlojsomUsers().length);
        _conditionalGetFlavorsPerUser = new HashMap(blojsomConfiguration.getBlojsomUsers().length);

        for (int i = 0; i < blojsomConfiguration.getBlojsomUsers().length; i++) {
            String user = blojsomConfiguration.getBlojsomUsers()[i];
            _ifModifiedSinceMap.put(user, null);
        }

        // Setup a map for the default flavors to use conditional get (the syndication flavors)
        _defaultConditionalGetFlavors = new HashMap();
        _defaultConditionalGetFlavors.put("rdf", "rdf");
        _defaultConditionalGetFlavors.put("rss", "rss");
        _defaultConditionalGetFlavors.put("rss2", "rss2");
        _defaultConditionalGetFlavors.put("atom", "atom");
    }

    /**
     * Process the blog entries
     *
     * @param httpServletRequest  Request
     * @param httpServletResponse Response
     * @param user                {@link org.blojsom.blog.BlogUser} instance
     * @param context             Context
     * @param entries             Blog entries retrieved for the particular request
     * @return Modified set of blog entries
     * @throws org.blojsom.plugin.BlojsomPluginException
     *          If there is an error processing the blog entries
     */
    public BlogEntry[] process(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, BlogUser user, Map context, BlogEntry[] entries) throws BlojsomPluginException {
        if (_ifModifiedSinceMap.containsKey(user.getId()) && entries.length > 0) {
            String flavor = BlojsomUtils.getRequestValue(FLAVOR_PARAM, httpServletRequest);
            if (BlojsomUtils.checkNullOrBlank(flavor)) {
                flavor = user.getBlog().getBlogDefaultFlavor();
            }

            if (_defaultConditionalGetFlavors.containsKey(flavor)) {
                Date latestEntryDate = entries[0].getDate();
                try {
                    Date ifModifiedSinceDate = new Date(httpServletRequest.getDateHeader(IF_MODIFIED_SINCE_HEADER));
                    if (latestEntryDate.compareTo(ifModifiedSinceDate) <= 0) {
                        _logger.debug("Returning 304 response for flavor: " + flavor);
                        httpServletResponse.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                    }
                } catch (IllegalArgumentException e) {
                    _logger.error(e);
                }
            } else {
                _logger.debug("Requested flavor is not a conditional get flavor: " + flavor);
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
