/**
 * Copyright (c) 2003-2009, David A. Czarnecki
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
package org.blojsom.plugin.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.blojsom.blog.Blog;
import org.blojsom.blog.Entry;
import org.blojsom.plugin.PluginException;
import org.blojsom.util.BlojsomConstants;
import org.blojsom.util.BlojsomUtils;
import org.springframework.context.ApplicationContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * List Web Admin Plugins Plugin
 *
 * @author David Czarnecki
 * @version $Id: ListWebAdminPluginsPlugin.java,v 1.3 2008-07-07 19:54:12 czarneckid Exp $
 * @since blojsom 3.0
 */
public class ListWebAdminPluginsPlugin extends BaseAdminPlugin {

    private static Log _logger = LogFactory.getLog(ListWebAdminPluginsPlugin.class);

    private static final String BLOJSOM_PLUGIN_WEB_ADMIN_PLUGINS_LIST = "BLOJSOM_PLUGIN_WEB_ADMIN_PLUGINS_LIST";
    private static final String LIST_WEB_ADMIN_PLUGINS_PAGE = "/org/blojsom/plugin/admin/templates/admin-list-web-admin-plugins";

    /**
     * Default constructor
     */
    public ListWebAdminPluginsPlugin() {
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
        entries = super.process(httpServletRequest, httpServletResponse, blog, context, entries);

        String page = BlojsomUtils.getRequestValue(BlojsomConstants.PAGE_PARAM, httpServletRequest);

        if (ADMIN_LOGIN_PAGE.equals(page)) {
            return entries;
        } else {
            Map plugins = new HashMap();
            ApplicationContext applicationContext = (ApplicationContext) _servletConfig.getServletContext().getAttribute(BlojsomConstants.BLOJSOM_APPLICATION_CONTEXT);
            if (applicationContext != null) {
                plugins = new TreeMap(applicationContext.getBeansOfType(WebAdminPlugin.class));
            }

            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, LIST_WEB_ADMIN_PLUGINS_PAGE);
            context.put(BLOJSOM_PLUGIN_WEB_ADMIN_PLUGINS_LIST, plugins);
        }

        return entries;
    }
}